package com.ssrlab.audioguide.botanic.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ssrlab.audioguide.botanic.helpers.MapTypeConverter

@Database(entities = [ExhibitObject::class], version = 4, exportSchema = false)
@TypeConverters(MapTypeConverter::class)
abstract class ExhibitDatabase : RoomDatabase() {
    abstract fun exhibitDao(): ExhibitDao
}
