package com.example.aicourse

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.aicourse.adapter.CourseAdapter
import com.example.aicourse.database.DatabaseHelper
import com.example.aicourse.model.Course

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        val fab: FloatingActionButton = findViewById(R.id.fabAdd)
        fab.setOnClickListener {
            startActivity(Intent(this, AddCourseActivity::class.java))
        }
        
        loadCourses()
    }

    override fun onResume() {
        super.onResume()
        loadCourses()
    }

    private fun loadCourses() {
        val courses = dbHelper.getAllCourses()
        courseAdapter = CourseAdapter(courses) { course ->
            val intent = Intent(this, CourseDetailActivity::class.java)
            intent.putExtra("course_id", course.id)
            startActivity(intent)
        }
        recyclerView.adapter = courseAdapter
    }
}
