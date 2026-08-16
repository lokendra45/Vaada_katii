package com.gaatho.rent.features.tenant.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.gaatho.rent.core.logging.AppLogger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.request.SelectRequestBuilder
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * A [PagingSource] that pages over a Supabase PostgREST table using **keyset**
 * (cursor-based) pagination instead of `OFFSET`.
 *
 * Postgres `OFFSET` pagination rescans and discards rows on every page, so it
 * degrades on deep pages. Keyset pagination instead carries the last row's
 * sort-key (`(orderColumn, id)`) forward and filters with `<` / `>` on that key,
 * so every page is a constant-cost index seek. Requires a stable ORDER BY on
 * [orderColumn] (e.g. `created_at`/`date`) with `id` as a tiebreaker.
 *
 * The [select] block runs inside the PostgREST request builder so callers can
 * add `ilike`, `eq`, `select(...)` etc. — the source appends the keyset cursor
 * filter, the ORDER BY (column + id) and the LIMIT itself.
 */
class SupabasePagingSource<T : Any>(
    private val client: SupabaseClient,
    private val table: String,
    private val json: Json,
    private val serializer: KSerializer<T>,
    private val orderColumn: String,
    private val orderDirection: Order = Order.DESCENDING,
    private val cursorOf: (T) -> String?,
    private val idOf: (T) -> String,
    private val block: SupabasePagingSource.QueryBuilder<T>.() -> Unit
) : PagingSource<KeysetCursor, T>() {

    override suspend fun load(params: LoadParams<KeysetCursor>): LoadResult<KeysetCursor, T> {
        return try {
            val pageSize = params.loadSize

            val queryBuilder = SupabasePagingSource.QueryBuilder<T>(
                client = client,
                table = table,
                json = json,
                serializer = serializer,
                orderColumn = orderColumn,
                orderDirection = orderDirection,
                cursor = params.key,
                pageSize = pageSize
            )
            block(queryBuilder)
            val rows = queryBuilder.execute()
            val last = rows.lastOrNull()
            val nextKey = if (rows.size >= pageSize && last != null) {
                KeysetCursor(cursorOf(last) ?: "", idOf(last))
            } else {
                null
            }

            LoadResult.Page(
                data = rows,
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            AppLogger.network.e(e) { "Supabase paging source failed for table $table" }
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<KeysetCursor, T>): KeysetCursor? = null

    class QueryBuilder<T : Any>(
        private val client: SupabaseClient,
        private val table: String,
        private val json: Json,
        private val serializer: KSerializer<T>,
        private val orderColumn: String,
        private val orderDirection: Order,
        private val cursor: KeysetCursor?,
        private val pageSize: Int
    ) {
        private var request: (suspend () -> PostgrestResult)? = null

        /**
         * Builds the PostgREST select request. The receiver lambda is the request
         * builder DSL (filters, select columns). The keyset cursor filter, ORDER BY
         * (column + id) and LIMIT are applied by the source.
         */
        fun select(columns: Columns = Columns.ALL, block: SelectRequestBuilder.() -> Unit) {
            request = {
                client.postgrest[table].select(columns) {
                    block()
                    val c = cursor
                    if (c != null) {
                        filter {
                            if (orderDirection == Order.DESCENDING) {
                                or {
                                    lt(orderColumn, c.orderValue)
                                    and {
                                        eq(orderColumn, c.orderValue)
                                        lt("id", c.id)
                                    }
                                }
                            } else {
                                or {
                                    gt(orderColumn, c.orderValue)
                                    and {
                                        eq(orderColumn, c.orderValue)
                                        gt("id", c.id)
                                    }
                                }
                            }
                        }
                    }
                    order(orderColumn, orderDirection)
                    order("id", orderDirection)
                    limit(pageSize.toLong())
                }
            }
        }

        suspend fun execute(): List<T> {
            val call = request ?: error("No select() call was configured for the paging source")
            val result = call()
            return json.decodeFromString(ListSerializer(serializer), result.data)
        }
    }
}

/**
 * Cursor identifying a row's position in the keyset ordering. [orderValue] is the
 * value of the sort column (e.g. `created_at`/`date`) and [id] is the row id,
 * used as a deterministic tiebreaker for rows sharing the same sort value.
 */
data class KeysetCursor(
    val orderValue: String,
    val id: String
)
