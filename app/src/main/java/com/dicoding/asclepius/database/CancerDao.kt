package com.dicoding.asclepius.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CancerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(cancer: Cancer)
    @Query("SELECT * from cancer ORDER BY id ASC")
    fun getAllCancer(): LiveData<List<Cancer>>
}