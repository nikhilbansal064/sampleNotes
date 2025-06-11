package com.example.myapp.ui.todonote

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import com.example.myapp.data.NoteRepository
import com.example.myapp.data.model.Note
import com.example.myapp.data.model.TodoItem
import com.example.myapp.util.MainCoroutineRule
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.junit.Assert.*

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class TodoNoteViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var viewModel: TodoNoteViewModel
    private lateinit var repository: NoteRepository
    private lateinit var application: Application
    private lateinit var savedStateHandle: SavedStateHandle
    private val gson = Gson()

    @Before
    fun setup() {
        repository = mock(NoteRepository::class.java)
        application = mock(Application::class.java)
    }

    private fun setupViewModel(noteId: Int?) {
        savedStateHandle = if (noteId != null && noteId != -1) {
            SavedStateHandle().apply { set("noteId", noteId) }
        } else {
            SavedStateHandle()
        }
        viewModel = TodoNoteViewModel(application, repository, savedStateHandle)
    }

    @Test
    fun `addTodoItem should add item to todoItemsLiveData`() {
        setupViewModel(null) // New note context
        val initialItems = viewModel.todoItemsLiveData.value?.size ?: 0

        viewModel.addTodoItem("New Todo")

        val newItems = viewModel.todoItemsLiveData.value
        assertNotNull(newItems)
        assertEquals(initialItems + 1, newItems!!.size)
        assertEquals("New Todo", newItems.last().text)
        assertFalse(newItems.last().isChecked)
    }

    @Test
    fun `saveTodoNote for new list should call insertNote with correct JSON`() = coroutineRule.runBlockingTest {
        setupViewModel(null)
        viewModel.addTodoItem("Todo 1")
        viewModel.addTodoItem("Todo 2")

        viewModel.saveTodoNote("My Todo List")

        val noteCaptor = ArgumentCaptor.forClass(Note::class.java)
        verify(repository).insertNote(noteCaptor.capture())

        val capturedNote = noteCaptor.value
        assertEquals("My Todo List", capturedNote.name)

        val expectedJson = gson.toJson(listOf(TodoItem("Todo 1", false), TodoItem("Todo 2", false)))
        assertEquals(expectedJson, capturedNote.content)
    }

    @Test
    fun `saveTodoNote for existing list should call updateNote with correct JSON`() = coroutineRule.runBlockingTest {
        val existingNoteId = 1
        val initialItems = listOf(TodoItem("Old item", true))
        val initialJson = gson.toJson(initialItems)
        val initialNote = Note(existingNoteId, "Old List Name", initialJson, System.currentTimeMillis() - 1000)

        `when`(repository.getNoteById(existingNoteId)).thenReturn(flowOf(initialNote))
        setupViewModel(existingNoteId) // This triggers loadNote via init
        coroutineRule.dispatcher.scheduler.advanceUntilIdle() // Ensure loadNote completes

        // Modify the list
        viewModel.addTodoItem("New Item Added")
        val newListName = "Updated List Name"
        viewModel.saveTodoNote(newListName)

        val noteCaptor = ArgumentCaptor.forClass(Note::class.java)
        verify(repository).updateNote(noteCaptor.capture())

        val capturedNote = noteCaptor.value
        assertEquals(existingNoteId, capturedNote.noteId)
        assertEquals(newListName, capturedNote.name)

        val expectedItems = listOf(TodoItem("Old item", true), TodoItem("New Item Added", false))
        val expectedJson = gson.toJson(expectedItems)
        assertEquals(expectedJson, capturedNote.content)
    }


    @Test
    fun `loadNote with valid noteId and JSON content should update todoItemsLiveData`() = coroutineRule.runBlockingTest {
        val testNoteId = 1
        val todoList = listOf(TodoItem("Loaded Todo 1", true), TodoItem("Loaded Todo 2", false))
        val jsonContent = gson.toJson(todoList)
        val testNote = Note(testNoteId, "Test Todo List", jsonContent, System.currentTimeMillis())

        `when`(repository.getNoteById(testNoteId)).thenReturn(flowOf(testNote))
        setupViewModel(testNoteId) // Triggers loadNote in init

        val observer = Observer<MutableList<TodoItem>> {}
        try {
            viewModel.todoItemsLiveData.observeForever(observer)
            coroutineRule.dispatcher.scheduler.advanceUntilIdle() // Ensure flow collection and LiveData update

            val loadedItems = viewModel.todoItemsLiveData.value
            assertNotNull(loadedItems)
            assertEquals(2, loadedItems!!.size)
            assertEquals("Loaded Todo 1", loadedItems[0].text)
            assertTrue(loadedItems[0].isChecked)
            assertEquals("Loaded Todo 2", loadedItems[1].text)
            assertFalse(loadedItems[1].isChecked)
        } finally {
            viewModel.todoItemsLiveData.removeObserver(observer)
        }
    }

    @Test
    fun `loadNote with empty content should result in empty todoItemsLiveData`() = coroutineRule.runBlockingTest {
        val testNoteId = 2
        val testNote = Note(testNoteId, "Empty List", "", System.currentTimeMillis())
        `when`(repository.getNoteById(testNoteId)).thenReturn(flowOf(testNote))
        setupViewModel(testNoteId)

        val observer = Observer<MutableList<TodoItem>> {}
        try {
            viewModel.todoItemsLiveData.observeForever(observer)
            coroutineRule.dispatcher.scheduler.advanceUntilIdle()
            assertTrue(viewModel.todoItemsLiveData.value?.isEmpty() ?: false)
        } finally {
            viewModel.todoItemsLiveData.removeObserver(observer)
        }
    }
}
