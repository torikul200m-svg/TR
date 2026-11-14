package com.example.trdial.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        ContactEntity::class,
        CallHistoryEntity::class,
        SipAccountEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TrDialDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun contactDao(): ContactDao
    abstract fun callHistoryDao(): CallHistoryDao
    abstract fun sipAccountDao(): SipAccountDao

    companion object {
        @Volatile
        private var INSTANCE: TrDialDatabase? = null

        fun getInstance(context: Context): TrDialDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TrDialDatabase::class.java,
                    "tr_dial_sip.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
