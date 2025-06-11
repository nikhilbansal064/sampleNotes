package com.example.myapp.util
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import com.example.myapp.data.model.TodoItem // Ensure this import is correct based on your project structure
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TodoPreviewParser {

    private val gson = Gson()
    private val maxPreviewItems = 3

    suspend fun parseAndFormat(jsonContent: String): SpannableString = withContext(Dispatchers.Default) {
        if (jsonContent.isBlank()) {
            return@withContext SpannableString("") // Or specific text like "[Empty Todo List]" if preferred for blank
        }
        try {
            val todoItemListType = object : TypeToken<List<TodoItem>>() {}.type
            val todoItems: List<TodoItem> = gson.fromJson(jsonContent, todoItemListType)

            if (todoItems.isEmpty()) {
                return@withContext SpannableString("[Empty Todo List]")
            }

            val spannableStringBuilder = SpannableStringBuilder()
            val itemsToShow = todoItems.take(maxPreviewItems)

            itemsToShow.forEachIndexed { index, item ->
                if (index > 0) {
                    spannableStringBuilder.append("\n\n")
                }
                val start = spannableStringBuilder.length
                spannableStringBuilder.append(item.text.trim()) // Trim item text
                if (item.isChecked) {
                    spannableStringBuilder.setSpan(StrikethroughSpan(), start, spannableStringBuilder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            if (todoItems.size > maxPreviewItems) {
                if (spannableStringBuilder.isNotEmpty()) {
                    spannableStringBuilder.append("\n")
                }
            }
            // SpannableStringBuilder is a CharSequence and can be directly set on TextView.
            // Casting to SpannableString is fine but not strictly necessary if the return type is CharSequence.
            // For explicit return type as SpannableString, this is okay.
            return@withContext SpannableString(spannableStringBuilder)

        } catch (e: JsonSyntaxException) {
            // Log.e("TodoPreviewParser", "JSON syntax error parsing todo content", e) // Consider logging
            return@withContext SpannableString("[Todo List - Malformed Content]")
        } catch (e: Exception) {
            // Log.e("TodoPreviewParser", "Unexpected error parsing/formatting todo content", e) // Consider logging
            return@withContext SpannableString("[Error in preview]")
        }
    }
}
