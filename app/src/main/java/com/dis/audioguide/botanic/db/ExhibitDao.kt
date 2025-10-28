package com.dis.audioguide.botanic.db

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

    @Query("DELETE FROM exhibit_table")
    fun deleteExhibits()
}