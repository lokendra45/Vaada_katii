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

private const val DB_NAME = "rentmanager.db"

actual class DriverFactory(private val context: Context) {

    // Matches SecureDatabasePassphraseManager's actual floor (API 23). StrongBox is attempted
    // opportunistically on 28+ inside the manager -- it isn't a hard requirement here.
    @RequiresApi(Build.VERSION_CODES.M)
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
                // execute() throws on PRAGMAs that return a row on current versions of this
                // library -- use executeForString/executeForLong instead.
                connection.executeForString("PRAGMA temp_store = MEMORY;", null, null)
                connection.executeForString("PRAGMA cipher_memory_security = ON;", null, null)
                connection.executeForString("PRAGMA foreign_keys = ON;", null, null)
                SecureDatabasePassphraseManager.wipe(passphrase)
            }
        }

        val factory = SupportOpenHelperFactory(
            passphrase,
            hook,
            /* enableWriteAheadLogging = */ true,
        )

        return AndroidSqliteDriver(
            schema = RentManagerDatabase.Schema,
            context = context,
            name = DB_NAME,
            factory = factory,
        )
    }
}
