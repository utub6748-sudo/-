package com.astro.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TransactionEntity::class, HabitEntity::class, GoalEntity::class, CategoryEntity::class], version = 1, exportSchema = false)
abstract class AstroDatabase : RoomDatabase() {
    abstract fun dao(): AstroDao
    companion object {
        @Volatile private var INSTANCE: AstroDatabase? = null
        fun get(context: Context): AstroDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context, AstroDatabase::class.java, "astro.db").build().also { INSTANCE = it }
        }
    }
}
