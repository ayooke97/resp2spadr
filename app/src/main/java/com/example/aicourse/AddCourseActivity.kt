package com.example.aicourse

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aicourse.database.DatabaseHelper
import com.example.aicourse.model.Course

class AddCourseActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course)

        dbHelper = DatabaseHelper(this)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etDuration = findViewById<EditText>(R.id.etDuration)
        val etImageUrl = findViewById<EditText>(R.id.etImageUrl)
        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val description = etDescription.text.toString()
            val duration = etDuration.text.toString()
            val imageUrl = etImageUrl.text.toString()

            if (title.isNotEmpty() && description.isNotEmpty() && duration.isNotEmpty()) {
                val course = Course(
                    title = title,
                    description = description,
                    duration = duration,
                    imageUrl = imageUrl
                )
                
                dbHelper.addCourse(course)
                Toast.makeText(this, "Course added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
