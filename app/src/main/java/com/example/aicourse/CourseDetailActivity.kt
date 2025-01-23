package com.example.aicourse

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.aicourse.database.DatabaseHelper

class CourseDetailActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var courseId: Int = -1
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvDuration: TextView
    private lateinit var ivCourse: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Course Details"

        dbHelper = DatabaseHelper(this)
        initializeViews()
        
        courseId = intent.getIntExtra("course_id", -1)
        if (courseId != -1) {
            loadCourseDetails()
            setupButtons()
        } else {
            Toast.makeText(this, "Error: Course not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initializeViews() {
        tvTitle = findViewById(R.id.tvTitle)
        tvDescription = findViewById(R.id.tvDescription)
        tvDuration = findViewById(R.id.tvDuration)
        ivCourse = findViewById(R.id.ivCourse)
    }

    private fun loadCourseDetails() {
        try {
            val course = dbHelper.getCourse(courseId)
            course?.let {
                tvTitle.text = it.title
                tvDescription.text = it.description
                tvDuration.text = it.duration
                
                Glide.with(this)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(ivCourse)
            } ?: run {
                Toast.makeText(this, "Error: Could not load course details", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading course details", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            finish()
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            val intent = Intent(this, UpdateCourseActivity::class.java).apply {
                putExtra("course_id", courseId)
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Course")
            .setMessage("Are you sure you want to delete this course?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteCourse()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteCourse() {
        try {
            if (dbHelper.deleteCourse(courseId)) {
                Toast.makeText(this, "Course deleted successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Failed to delete course", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error deleting course", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload course details when returning from update
        if (courseId != -1) {
            loadCourseDetails()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}
