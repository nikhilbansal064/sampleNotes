package com.example.myapp.ui.noteslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.R
import com.example.myapp.data.model.NoteType
import com.example.myapp.databinding.FragmentNotesListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NotesListFragment : Fragment() {

    private var _binding: FragmentNotesListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotesListViewModel by viewModels()
    private lateinit var noteListAdapter: NoteListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.allNotes.observe(viewLifecycleOwner) { notes ->
            notes?.let {
                noteListAdapter.submitList(it)
            }
        }

        binding.addNoteFab.setOnClickListener {
            showCreateNewNoteDialog()
        }
    }

    private fun showCreateNewNoteDialog() {
        val options = arrayOf(
            getString(R.string.new_text_note_option),
            getString(R.string.new_todo_list_option)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.create_new_note_dialog_title))
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> { // New Text Note
                        val action = NotesListFragmentDirections.actionNotesListFragmentToNoteFragment()
                        findNavController().navigate(action)
                    }
                    1 -> { // New Todo List
                        val action = NotesListFragmentDirections.actionNotesListFragmentToTodoNoteFragment()
                        findNavController().navigate(action)
                    }
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun setupRecyclerView() {
        noteListAdapter = NoteListAdapter { note ->
            val action = when (note.noteType) {
                NoteType.TEXT -> NotesListFragmentDirections.actionNotesListFragmentToNoteFragment(note.noteId)
                NoteType.TODO -> NotesListFragmentDirections.actionNotesListFragmentToTodoNoteFragment(note.noteId)
            }
            findNavController().navigate(action)
        }
        binding.notesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = noteListAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
