package com.example.myapp.ui.noteslist

import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.data.model.Note
import com.example.myapp.data.model.NoteType
import com.example.myapp.util.TodoPreviewParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NoteListAdapter(private val onClickListener: (Note) -> Unit) :
    ListAdapter<Note, NoteListAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
        holder.itemView.setOnClickListener {
            onClickListener(note)
        }
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.noteNameTextView)
        private val nameTypeView: TextView = itemView.findViewById(R.id.noteTypeTextView)
        private val contentPreviewTextView: TextView = itemView.findViewById(R.id.noteContentPreviewTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.noteDateTextView)
        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        private val todoPreviewParser = TodoPreviewParser()
        var parsingJob: Job? = null

        fun bind(note: Note) {
            nameTypeView.text = note.noteType.name
            nameTextView.text = note.name
            parsingJob?.cancel()
            parsingJob = null
            if (note.noteType == NoteType.TODO) {
                contentPreviewTextView.text = "Loading Preview..."
                parsingJob = CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val spannablePreview = todoPreviewParser.parseAndFormat(note.content)
                        // Check if ViewHolder is still bound to the same item,
                        // though with ListAdapter this is generally safer.
                        if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                            contentPreviewTextView.text = spannablePreview
                        }
                    } catch (e: Exception) {
                    // This catch is for coroutine cancellation or unexpected errors during launch/await
                        if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                            contentPreviewTextView.text = note.content.take(100)
                        }
                    }
                }
            } else {
                contentPreviewTextView.text = note.content.take(100)
            }
            dateTextView.text = dateFormatter.format(Date(note.lastModifiedDate))
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.noteId == newItem.noteId
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}
