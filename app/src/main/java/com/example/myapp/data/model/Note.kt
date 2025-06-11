package com.example.myapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val noteId: Int = 0,
    var name: String,
    var content: String,

    @ColumnInfo(name = "last_modified_date")
    var lastModifiedDate: Long
)
