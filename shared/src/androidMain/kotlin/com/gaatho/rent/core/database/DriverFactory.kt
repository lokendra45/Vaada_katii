package com.gaatho.rent.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.gaatho.rent.core.database.security.SecureDatabasePassphraseManager
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.database.RentManagerDatabase
import net.zetetic.database.sqlcipher.SQLiteConnection
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import androidx.core.content.edit

private const val DB_NAME = "rentmanager.db"

actual class DriverFactory(private val context: Context) {

    /**
     * Guard flag to prevent infinite recursion during recovery.
     * If `createDriver()` → verify fails → deleteDatabase → `createDriver()` again → that also
     * fails, we must NOT call `createDriver()` a third time. Instead, throw and let the app
     * surface the error to the user.
     */
    private var recoveryAttempted = false

    actual fun createDriver(): SqlDriver {
        System.loadLibrary("sqlcipher")

        val result = SecureDatabasePassphraseManager.getOrCreatePassphrase(context)
        val passphrase = result.passphrase

        val dbFile = context.getDatabasePath(DB_NAME)
        if (result.wasRegenerated && !result.isFirstRun && dbFile.exists()) {
            // The old passphrase is gone -- the file on disk can't be opened with the new
            // one. Delete deliberately here instead of letting SQLCipher fail deep inside
            // the first query with a cryptic "file is not a database" error.
            AppLogger.database.e { "Passphrase was regenerated; deleting unreadable local database." }
            context.deleteDatabase(DB_NAME)
        }

        val hook = object : SQLiteDatabaseHook {
            override fun preKey(connection: SQLiteConnection) {}

            override fun postKey(connection: SQLiteConnection) {
                // Set-form pragmas return no row (SQLITE_DONE) -- must use execute(), not
                // executeForString()/executeForLong(), which require a SQLITE_ROW result.
                connection.execute("PRAGMA temp_store = MEMORY;", null, null)
                connection.execute("PRAGMA cipher_memory_security = ON;", null, null)
                connection.execute("PRAGMA foreign_keys = ON;", null, null)

                // NOTE: Do NOT wipe the passphrase here. With WAL mode enabled,
                // SupportOpenHelperFactory holds a direct reference to the passphrase
                // ByteArray and reuses it to key additional pool connections for concurrent
                // reads. Wiping it after the first connection causes all subsequent pool
                // connections to fail with "file is not a database" (code 26).
                //
                // cipher_memory_security = ON (above) already ensures SQLCipher zeroes its
                // own internal copies of key material when connections are closed.
            }
        }

        val factory = SupportOpenHelperFactory(
            passphrase,
            hook,
            /* enableWriteAheadLogging = */ true,
        )

        val driver = AndroidSqliteDriver(
            schema = RentManagerDatabase.Schema,
            context = context,
            name = DB_NAME,
            factory = factory,
        )

        return try {
            // Eagerly verify that the database file on disk can actually be opened and
            // decrypted with the current passphrase.
            driver.executeQuery(null, "SELECT 1;", { cursor -> cursor.next() }, 0)
            driver
        } catch (e: Throwable) {
            if (recoveryAttempted) {
                // Already tried recovery once — do NOT recurse again. Throw to surface the
                // real error instead of StackOverflowError.
                AppLogger.database.e { "Database recovery already attempted. Refusing to recurse. Error: ${e.message}" }
                try { driver.close() } catch (_: Exception) {}
                throw IllegalStateException(
                    "Database could not be opened even after recovery. " +
                        "Please clear app data or reinstall.", e
                )
            }

            val msg = (e.message ?: "") + " " + (e.cause?.message ?: "")
            val isNotADatabase = e is net.zetetic.database.sqlcipher.SQLiteNotADatabaseException ||
                msg.contains("file is not a database", ignoreCase = true) ||
                msg.contains("not a database", ignoreCase = true) ||
                msg.contains("code 26")

            if (isNotADatabase || (dbFile.exists() && result.wasRegenerated)) {
                AppLogger.database.e {
                    "Database file ($DB_NAME) cannot be opened with current passphrase ($msg). " +
                        "Deleting and regenerating fresh driver."
                }
                try { driver.close() } catch (_: Exception) {}
                context.deleteDatabase(DB_NAME)

                // Also reset passphrase manager if the existing passphrase couldn't open the DB
                if (!result.wasRegenerated) {
                    try {
                        context.getSharedPreferences("rentmanager_secure_db_prefs", Context.MODE_PRIVATE)
                            .edit(commit = true) { clear() }
                    } catch (_: Exception) {}
                }

                recoveryAttempted = true
                return createDriver()
            }
            throw e
        }
    }
}
