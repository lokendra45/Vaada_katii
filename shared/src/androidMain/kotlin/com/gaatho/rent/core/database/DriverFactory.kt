package com.gaatho.rent.core.database

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
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

    // Matches SecureDatabasePassphraseManager's actual floor (API 23). StrongBox is attempted
    // opportunistically on 28+ inside the manager -- it isn't a hard requirement here.
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

        // SupportOpenHelperFactory keeps a direct reference to `passphrase` -- it does not
        // copy it, and unlike the legacy net.sqlcipher.database.SupportFactory this library
        // has no clearPassphrase flag to auto-wipe it for you. The database also opens lazily
        // (on first real access), so don't wipe the array here -- wipe it in postKey() below,
        // which fires exactly once, right after the connection is actually keyed with it.
        val hook = object : SQLiteDatabaseHook {
            override fun preKey(connection: SQLiteConnection) {}

            override fun postKey(connection: SQLiteConnection) {
                // Set-form pragmas return no row (SQLITE_DONE) -- must use execute(), not
                // executeForString()/executeForLong(), which require a SQLITE_ROW result and throw
                // SQLiteDoneException otherwise. executeForString/Long are only for query-form pragmas
                // that return a value, e.g. "PRAGMA cipher_version;".
                connection.execute("PRAGMA temp_store = MEMORY;", null, null)
                connection.execute("PRAGMA cipher_memory_security = ON;", null, null)
                connection.execute("PRAGMA foreign_keys = ON;", null, null)
                SecureDatabasePassphraseManager.wipe(passphrase)
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
            // Eagerly verify that the database file on disk can actually be opened and decrypted
            // with the current passphrase. If the file on disk is plain-text SQLite (from an older build)
            // or was encrypted with a different key before passphrase sync, SQLCipher throws
            // SQLiteNotADatabaseException (code 26).
            driver.executeQuery(null, "SELECT 1;", { cursor -> cursor.next() }, 0)
            driver
        } catch (e: Throwable) {
            val msg = (e.message ?: "") + " " + (e.cause?.message ?: "")
            val isNotADatabase = e is net.zetetic.database.sqlcipher.SQLiteNotADatabaseException ||
                msg.contains("file is not a database", ignoreCase = true) ||
                msg.contains("not a database", ignoreCase = true) ||
                msg.contains("code 26")

            if (isNotADatabase || (dbFile.exists() && result.wasRegenerated)) {
                AppLogger.database.e { "Database file on disk ($DB_NAME) cannot be opened/decrypted with current passphrase ($msg). Deleting corrupted/incompatible database and regenerating fresh driver." }
                try { driver.close() } catch (_: Exception) {}
                context.deleteDatabase(DB_NAME)

                // Also reset passphrase manager if the existing passphrase couldn't open the existing DB
                if (!result.wasRegenerated) {
                    try {
                        context.getSharedPreferences("rentmanager_secure_db_prefs", Context.MODE_PRIVATE).edit(
                            commit = true
                        ) { clear() }
                    } catch (_: Exception) {}
                }

                return createDriver()
            }
            throw e
        }
    }
}
