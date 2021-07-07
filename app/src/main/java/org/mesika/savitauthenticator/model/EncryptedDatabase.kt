package org.mesika.savitauthenticator.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import org.mesika.savitauthenticator.model.useraccounts.UserAccount
import org.mesika.savitauthenticator.model.useraccounts.UserAccountDao
import org.mesika.savitauthenticator.utils.PreferenceProvider

@Database(
    entities = [UserAccount::class], version = 1
)
abstract class EncryptedDatabase : RoomDatabase() {
    abstract fun getUSerAccountDao(): UserAccountDao

    companion object{
        @Volatile
        private var instance: EncryptedDatabase? = null
        private val LOCK = Any()
        operator fun invoke(keyService: KeyService, context: Context) = instance?: synchronized(LOCK){

            val pref = PreferenceProvider(context)
            val s = pref.passcode()
            val passphrase = SQLiteDatabase.getBytes(s!!)
            val factory = SupportFactory(passphrase)
            instance?:buildDatabase(keyService.getKey(), context, factory).also {
                instance = it
            }
        }




        private fun buildDatabase(
            paswword: CharArray,
            context: Context,
            supportFactory: SupportFactory
        ) =

            Room.databaseBuilder(
                context.applicationContext,
                EncryptedDatabase::class.java,
                "MyDatabase.Db"
            )
                .openHelperFactory(supportFactory)
                .build()
    }

}