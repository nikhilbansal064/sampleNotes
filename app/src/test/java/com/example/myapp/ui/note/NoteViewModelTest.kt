package com.example.myapp.ui.note

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.myapp.data.NoteRepository
import com.example.myapp.data.model.Note
import com.example.myapp.util.MainCoroutineRule
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
class NoteViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var viewModel: NoteViewModel
    private lateinit var repository: NoteRepository
    private lateinit var application: Application
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        repository = mock(NoteRepository::class.java)
        application = mock(Application::class.java)
        // Mock SavedStateHandle for each test, or provide specific values
    }

    private fun setupViewModel(noteId: Int?) {
        savedStateHandle = if (noteId != null && noteId != -1) {
            SavedStateHandle().apply { set("noteId", noteId) }
        } else {
            SavedStateHandle() // No noteId or it's for a new note
        }
        viewModel = NoteViewModel(application, repository, savedStateHandle)
    }

    @Test
    fun `saveNote for new note should call insertNote on repository`() = coroutineRule.runBlockingTest {
        setupViewModel(null) // New note scenario, noteId is null or -1
        val noteName = "New Note"
        val noteContent = "Content of new note"

        viewModel.saveNote(noteName, noteContent)

        val noteCaptor = ArgumentCaptor.forClass(Note::class.java)
        verify(repository).insertNote(noteCaptor.capture())
        assertEquals(noteName, noteCaptor.value.name)
        assertEquals(noteContent, noteCaptor.value.content)
        assertTrue(noteCaptor.value.noteId == 0) // Assuming 0 for new notes before DB generates ID
    }

    @Test
    fun `saveNote for existing note should call updateNote on repository`() = coroutineRule.runBlockingTest {
        val existingNoteId = 1
        val initialNote = Note(existingNoteId, "Old Name", "Old Content", System.currentTimeMillis() - 1000)
        setupViewModel(existingNoteId)

        // Simulate loading the note into _currentNote
        `when`(repository.getNoteById(existingNoteId)).thenReturn(flowOf(initialNote))
        viewModel.currentNote.observeForever { } // Trigger loading if not already done by init
        // Ensure the flow collection happens, advance time if necessary
        coroutineRule.dispatcher.scheduler.advanceUntilIdle()


        val updatedName = "Updated Name"
        val updatedContent = "Updated Content"
        viewModel.saveNote(updatedName, updatedContent)

        val noteCaptor = ArgumentCaptor.forClass(Note::class.java)
        verify(repository).updateNote(noteCaptor.capture())
        assertEquals(existingNoteId, noteCaptor.value.noteId)
        assertEquals(updatedName, noteCaptor.value.name)
        assertEquals(updatedContent, noteCaptor.value.content)
    }

    @Test
    fun `loadNote with valid noteId should update currentNote LiveData`() = coroutineRule.runBlockingTest {
        val testNoteId = 1
        val testNote = Note(testNoteId, "Test Note", "Content", System.currentTimeMillis())
        `when`(repository.getNoteById(testNoteId)).thenReturn(flowOf(testNote))

        setupViewModel(testNoteId) // This will trigger the init block and load the note

        val observer = androidx.lifecycle.Observer<Note?> {}
        try {
            viewModel.currentNote.observeForever(observer)
            // Advance dispatcher to ensure coroutine in init block completes
            coroutineRule.dispatcher.scheduler.advanceUntilIdle()
            assertNotNull(viewModel.currentNote.value)
            assertEquals(testNote, viewModel.currentNote.value)
        } finally {
            viewModel.currentNote.removeObserver(observer)
        }
    }

    @Test
    fun `loadNote with invalid noteId (-1) should result in null currentNote LiveData initially`() = coroutineRule.runBlockingTest {
        setupViewModel(-1) // Or null, depending on how new notes are represented by noteId

        val observer = androidx.lifecycle.Observer<Note?> {}
        try {
            viewModel.currentNote.observeForever(observer)
            coroutineRule.dispatcher.scheduler.advanceUntilIdle() // Allow init to run
            assertNull(viewModel.currentNote.value) // No note should be loaded for noteId -1
        } finally {
            viewModel.currentNote.removeObserver(observer)
        }
    }
}
