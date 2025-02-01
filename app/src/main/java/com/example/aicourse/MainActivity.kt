package com.example.aicourse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.aicourse.adapter.CourseAdapter
import com.example.aicourse.database.DatabaseHelper
import com.example.aicourse.model.Course
import android.util.Log

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvNoCourses: TextView

    private val courseDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadCourses()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        
        recyclerView = findViewById(R.id.recyclerView)
        tvNoCourses = findViewById(R.id.tvNoCourses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        val fab: FloatingActionButton = findViewById(R.id.fabAdd)
        fab.setOnClickListener {
            try {
                startActivity(Intent(this, AddCourseActivity::class.java))
            } catch (e: Exception) {
                Log.e("MainActivity", "Error starting AddCourseActivity", e)
                Toast.makeText(this, "Error opening course creator", Toast.LENGTH_SHORT).show()
            }
        }
        
        loadCourses()
    }

    override fun onResume() {
        super.onResume()
        loadCourses()
    }

    private fun loadCourses() {
        try {
            val courses = dbHelper.getAllCourses()
            
            if (courses.isEmpty()) {
                recyclerView.visibility = View.GONE
                tvNoCourses.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                tvNoCourses.visibility = View.GONE
                
                courseAdapter = CourseAdapter(courses) { course ->
                    val intent = Intent(this, CourseDetailActivity::class.java)
                    intent.putExtra("course_id", course.id)
                    courseDetailLauncher.launch(intent)
                }
                recyclerView.adapter = courseAdapter
            }
        } catch (e: Exception) {
            e.printStackTrace()
            recyclerView.visibility = View.GONE
            tvNoCourses.visibility = View.VISIBLE
            tvNoCourses.text = "Error loading courses"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}
