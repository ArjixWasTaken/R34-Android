package me.devoxin.rule34.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.bugsnag.android.Bugsnag
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import me.devoxin.rule34.R
import me.devoxin.rule34.adapters.AutoCompleteAdapter

class MainActivity : AppCompatActivity() {
    private val tagList = mutableListOf<AutoCompleteAdapter.TagSuggestion>()

    private fun addTag(tagSuggestion: AutoCompleteAdapter.TagSuggestion): Boolean {
        return if (tagList.contains(tagSuggestion)) {
            val chips = findViewById<ChipGroup>(R.id.tagList)
            val index = tagList.indexOf(tagSuggestion)
            val chip = chips.getChildAt(index) as Chip

            tagList[index] = tagSuggestion
            chip.text = tagSuggestion.value
            chip.tag = tagSuggestion
            chip.setTextColor(if (tagSuggestion.exclude) Color.RED else Color.BLACK)
            false
        } else {
            tagList.add(tagSuggestion)
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        setContentView(R.layout.activity_search)

        val chips = findViewById<ChipGroup>(R.id.tagList)
        val tagSearch = findViewById<AutoCompleteTextView>(R.id.tagSearch)
        tagSearch.setAdapter(AutoCompleteAdapter(this))

        tagSearch.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
                val tagSuggestion = adapterView.getItemAtPosition(position) as AutoCompleteAdapter.TagSuggestion
                if (!addTag(tagSuggestion)) return tagSearch.text.clear()
                //if (!tagList.add(tagSuggestion)) return tagSearch.text.clear()

                val chip = Chip(this@MainActivity).apply {
                    tag = tagSuggestion
                    text = tagSuggestion.value
                    isCloseIconVisible = true

                    if (tagSuggestion.exclude) {
                        setTextColor(Color.RED)
                    }

                    setOnClickListener {
                        val tag = it.tag as AutoCompleteAdapter.TagSuggestion
                        tagList.remove(tag)
                        chips.removeView(it)
                        tagSearch.setText(tag.formattedTag)
                        tagSearch.setSelection(tag.formattedTag.length)
                    }
                    setOnCloseIconClickListener {
                        tagList.remove(it.tag)
                        chips.removeView(it)
                    }
                }

                chips.addView(chip)
                tagSearch.text.clear()
            }
        }

        tagSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
            override fun afterTextChanged(s: Editable) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty() || start >= s.length || start < 0) return

                val content = s.trim().takeIf { it.isNotEmpty() }?.toString()

                if (content == null) {
                    if (s[start] == '\n') {
                        findViewById<Button>(R.id.search).performClick()
                    }

                    return tagSearch.text.clear()
                }
            }
        })
    }

    fun onSearchClick(v: View) {
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("tagList", tagList.map { it.formattedTag }.toTypedArray())
        this.startActivity(intent)
    }
}
