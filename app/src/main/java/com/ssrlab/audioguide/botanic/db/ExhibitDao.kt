package com.ssrlab.audioguide.botanic.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExhibitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(exhibit: ExhibitObject)

    @Query("SELECT * FROM exhibit_table")
    fun getAllExhibits(): List<ExhibitObject>
}