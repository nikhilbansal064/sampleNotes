package com.example.myapp.ui.todonote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.databinding.FragmentTodoNoteBinding
import com.example.myapp.ui.note.SavedStateViewModelFactory

class TodoNoteFragment : Fragment() {

    private var _binding: FragmentTodoNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TodoNoteViewModel
    private lateinit var todoAdapter: TodoAdapter
    private val args: TodoNoteFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodoNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, SavedStateViewModelFactory(requireActivity().application, this, arguments))
            .get(TodoNoteViewModel::class.java)

        setupRecyclerView()

        viewModel.currentNote.observe(viewLifecycleOwner) { note ->
            note?.let {
                binding.todoNoteNameEditText.setText(it.name)
            }
        }

        viewModel.todoItemsLiveData.observe(viewLifecycleOwner) { items ->
            todoAdapter.updateData(items)
        }

        binding.addTodoItemButton.setOnClickListener {
            val newItemText = binding.newTodoItemEditText.text.toString().trim()
            if (newItemText.isNotEmpty()) {
                viewModel.addTodoItem(newItemText)
                binding.newTodoItemEditText.text.clear()
            }
        }

        binding.saveTodoNoteButton.setOnClickListener {
            val todoNoteName = binding.todoNoteNameEditText.text.toString().trim()
            if (todoNoteName.isNotEmpty()) {
                viewModel.saveTodoNote(todoNoteName)
                findNavController().navigateUp()
            } else {
                binding.todoNoteNameEditText.error = "Title cannot be empty"
            }
        }
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(mutableListOf()) { item, position ->
            viewModel.updateTodoItem(item, position)
        }
        binding.todoItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todoAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
