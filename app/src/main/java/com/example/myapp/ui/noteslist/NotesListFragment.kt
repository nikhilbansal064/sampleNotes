package com.example.myapp.ui.noteslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
// import com.example.myapp.R // Not strictly needed if using NavDirections
import com.example.myapp.databinding.FragmentNotesListBinding

class NotesListFragment : Fragment() {

    private var _binding: FragmentNotesListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotesListViewModel
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

        viewModel = ViewModelProvider(this).get(NotesListViewModel::class.java)

        setupRecyclerView()

        viewModel.allNotes.observe(viewLifecycleOwner) { notes ->
            notes?.let {
                noteListAdapter.submitList(it)
            }
        }

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.myapp.R // Required for string resources

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
            // CURRENT BEHAVIOR: Still navigates to TodoNoteFragment for any item click.
            // This needs to be addressed in a future step by implementing a way to
            // distinguish note types (e.g., by adding a 'type' field to the Note entity
            // or using a naming convention) and then navigating to the appropriate fragment.
            val action = NotesListFragmentDirections.actionNotesListFragmentToTodoNoteFragment(note.noteId)
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
