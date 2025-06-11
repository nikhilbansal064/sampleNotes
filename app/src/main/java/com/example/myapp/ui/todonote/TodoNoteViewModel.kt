package com.example.myapp.ui.todonote

import android.app.Application
import androidx.lifecycle.*
import com.example.myapp.data.NoteRepository
import com.example.myapp.data.local.NoteDatabase
import com.example.myapp.data.model.Note
import com.example.myapp.data.model.NoteType
import com.example.myapp.data.model.TodoItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class TodoNoteViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val repository: NoteRepository = NoteRepository(NoteDatabase.getDatabase(application).noteDao())
    private val gson = Gson()

    private val _todoItemsLiveData = MutableLiveData<MutableList<TodoItem>>(mutableListOf())
    val todoItemsLiveData: LiveData<MutableList<TodoItem>> = _todoItemsLiveData

    private val _currentNote = MutableLiveData<Note?>()
    val currentNote: LiveData<Note?> = _currentNote

    var noteId: Int? = null

    // Renamed to avoid conflict with any external call if needed, and to clarify it's the init-path loading
    fun loadNote(id: Int) {
        viewModelScope.launch {
            if (id == -1) return@launch
            noteId = id
            repository.getNoteById(id).collect { note ->
                _currentNote.postValue(note)
                if (note != null && note.content.isNotBlank()) {
                    try {
                        val type = object : TypeToken<List<TodoItem>>() {}.type
                        val items: List<TodoItem> = gson.fromJson(note.content, type)
                        _todoItemsLiveData.postValue(items.toMutableList())
                    } catch (e: Exception) {
                        _todoItemsLiveData.postValue(mutableListOf()) // Default to empty on error
                    }
                } else {
                    _todoItemsLiveData.postValue(mutableListOf()) // Default to empty if no content
                }
            }
        }
    }

    fun addTodoItem(text: String) {
        if (text.isBlank()) return
        val newItem = TodoItem(text, false)
        val currentList = _todoItemsLiveData.value ?: mutableListOf()
        currentList.add(newItem)
        _todoItemsLiveData.postValue(currentList) // Triggers LiveData update
    }

    fun updateTodoItem(item: TodoItem, position: Int) {
        val currentList = _todoItemsLiveData.value ?: mutableListOf()
        if (position >= 0 && position < currentList.size) {
            currentList[position] = item
            _todoItemsLiveData.postValue(currentList) // Triggers LiveData update
        }
    }

    fun saveTodoNote(name: String) {
        val itemsJson = gson.toJson(_todoItemsLiveData.value ?: emptyList<TodoItem>())
        viewModelScope.launch {
            val currentLoadedNote = _currentNote.value // Use the note loaded via noteId
            if (currentLoadedNote != null && currentLoadedNote.noteId == noteId && noteId != null) {
                // Update existing note being edited
                val updatedNote = currentLoadedNote.copy(
                    name = name,
                    content = itemsJson,
                    lastModifiedDate = System.currentTimeMillis(),
                    noteType = NoteType.TODO
                )
                repository.updateNote(updatedNote)
            } else {
                // Create new note
                val newNote = Note(
                    name = name,
                    content = itemsJson,
                    lastModifiedDate = System.currentTimeMillis(),
                    noteType = NoteType.TODO
                )
                repository.insertNote(newNote)
            }
        }
    }
}
