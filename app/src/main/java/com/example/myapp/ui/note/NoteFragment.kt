package com.example.myapp.ui.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapp.databinding.FragmentNoteBinding

class NoteFragment : Fragment() {

    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteViewModel by viewModels()
    private val args: NoteFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadNoteById(args.noteId)

        viewModel.currentNote.observe(viewLifecycleOwner) { note ->
            note?.let {
                binding.noteNameEditText.setText(it.name)
                binding.noteContentEditText.setText(it.content)
            }
        }

        binding.saveNoteButton.setOnClickListener {
            val noteName = binding.noteNameEditText.text.toString().trim()
            val noteContent = binding.noteContentEditText.text.toString().trim()

            if (noteName.isNotEmpty()) { // Basic validation
                viewModel.saveNote(noteName, noteContent)
                findNavController().navigateUp() // Go back to NotesListFragment
            } else {
                // Optionally, show an error message if the title is empty
                binding.noteNameEditText.error = "Title cannot be empty"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
