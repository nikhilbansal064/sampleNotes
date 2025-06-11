package com.example.myapp.data.local

import androidx.room.*
import com.example.myapp.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note): Int

    @Delete
    suspend fun deleteNote(note: Note): Int

    @Query("SELECT * FROM notes ORDER BY last_modified_date DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE noteId = :noteId")
    fun getNoteById(noteId: Int): Flow<Note?>
}
