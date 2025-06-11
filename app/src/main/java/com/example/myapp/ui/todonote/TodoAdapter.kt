package com.example.myapp.ui.todonote

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.data.model.TodoItem
import com.example.myapp.databinding.ItemTodoBinding

class TodoAdapter(
    private val todoItems: MutableList<TodoItem>,
    private val onItemChanged: (TodoItem, Int) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val item = todoItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = todoItems.size

    fun updateData(newItems: List<TodoItem>) {
        todoItems.clear()
        todoItems.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class TodoViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TodoItem) {
            binding.todoTextView.text = item.text
            binding.todoCheckBox.isChecked = item.isChecked
            updateTextStyle(binding.todoTextView, item.isChecked)

            binding.todoCheckBox.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
                updateTextStyle(binding.todoTextView, isChecked)
                onItemChanged(item, adapterPosition)
            }
        }

        private fun updateTextStyle(textView: TextView, isChecked: Boolean) {
            if (isChecked) {
                textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }
}
