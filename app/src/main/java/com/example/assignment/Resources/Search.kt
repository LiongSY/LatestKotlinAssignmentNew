package com.example.assignment.Resources

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R

class Search : AppCompatActivity() {

    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var db: SQLiteHelper
    private lateinit var adapter: ResourcesAdapter
    private lateinit var resourceArray : ArrayList<ResourceModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        resourceArray = arrayListOf<ResourceModel>()
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        searchRecyclerView = findViewById(R.id.searchRecyclerView)

        db = SQLiteHelper(this)
        adapter = ResourcesAdapter()

        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.adapter = adapter


        autoCompleteTextView.addTextChangedListener(object:TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()

//                val results = db.searchResources(query)

//                adapter.setData(results)
            }


        })
    }


}