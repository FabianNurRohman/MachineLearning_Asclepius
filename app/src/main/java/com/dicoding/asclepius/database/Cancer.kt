package com.dicoding.asclepius.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Cancer(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @ColumnInfo(name = "image")
    var mediaCover: String? = null,
    @ColumnInfo(name = "result")
    var event_name: String? = null,
    @ColumnInfo(name = "score")
    var event_owner: Int? = null

) : Parcelable