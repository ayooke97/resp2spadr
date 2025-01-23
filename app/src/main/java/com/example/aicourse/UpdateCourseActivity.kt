package com.example.aicourse

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aicourse.database.DatabaseHelper
import com.example.aicourse.model.Course

class UpdateCourseActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDuration: EditText
    private lateinit var etImageUrl: EditText
    private var courseId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_course)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Update Course"

        dbHelper = DatabaseHelper(this)
        initializeViews()

        courseId = intent.getIntExtra("course_id", -1)
        if (courseId != -1) {
            loadCourseDetails()
        } else {
            Toast.makeText(this, "Error: Course not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.btnSaveUpdate).setOnClickListener {
            if (validateInputs()) {
                updateCourse()
            }
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
        etTitle = findViewById(R.id.etUpdateTitle)
        etDescription = findViewById(R.id.etUpdateDescription)
        etDuration = findViewById(R.id.etUpdateDuration)
        etImageUrl = findViewById(R.id.etUpdateImageUrl)
    }

    private fun loadCourseDetails() {
        val course = dbHelper.getCourse(courseId)
        course?.let {
            etTitle.setText(it.title)
            etDescription.setText(it.description)
            etDuration.setText(it.duration)
            etImageUrl.setText(it.imageUrl)
        } ?: run {
            Toast.makeText(this, "Error: Could not load course details", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val duration = etDuration.text.toString().trim()
        val imageUrl = etImageUrl.text.toString().trim()

        when {
            title.isEmpty() -> {
                etTitle.error = "Title is required"
                return false
            }
            description.isEmpty() -> {
                etDescription.error = "Description is required"
                return false
            }
            duration.isEmpty() -> {
                etDuration.error = "Duration is required"
                return false
            }
            imageUrl.isEmpty() -> {
                etImageUrl.error = "Image URL is required"
                return false
            }
        }
        return true
    }

    private fun updateCourse() {
        val updatedCourse = Course(
            id = courseId,
            title = etTitle.text.toString().trim(),
            description = etDescription.text.toString().trim(),
            duration = etDuration.text.toString().trim(),
            imageUrl = etImageUrl.text.toString().trim()
        )

        if (dbHelper.updateCourse(updatedCourse)) {
            Toast.makeText(this, "Course updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to update course", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
        dbHelper.close()
    }
}
