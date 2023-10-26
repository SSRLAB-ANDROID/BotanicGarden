package com.ssrlab.audioguide.botanic.db

import androidx.room.*

@Dao
interface ExhibitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(exhibit: ExhibitObject)

    @Query("SELECT * FROM exhibit_table")
    fun getAllExhibits(): List<ExhibitObject>

    @Query("DELETE FROM exhibit_table")
    fun deleteExhibits()
}