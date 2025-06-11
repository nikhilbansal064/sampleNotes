package com.example.myapp.ui.noteslist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.myapp.data.NoteRepository
import com.example.myapp.data.local.NoteDatabase
import com.example.myapp.data.model.Note

// Primary constructor for production, secondary for testing
class NotesListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository = NoteRepository(NoteDatabase.getDatabase(application).noteDao())
    val allNotes: LiveData<List<Note>> = repository.allNotes.asLiveData()
}
