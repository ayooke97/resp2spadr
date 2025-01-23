package com.example.aicourse

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.aicourse.database.DatabaseHelper

class CourseDetailActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        dbHelper = DatabaseHelper(this)
        
        val courseId = intent.getIntExtra("course_id", -1)
        if (courseId != -1) {
            val course = dbHelper.getCourse(courseId)
            course?.let {
                findViewById<TextView>(R.id.tvTitle).text = it.title
                findViewById<TextView>(R.id.tvDescription).text = it.description
                findViewById<TextView>(R.id.tvDuration).text = it.duration
                
                val imageView = findViewById<ImageView>(R.id.ivCourse)
                Glide.with(this)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(imageView)
            }
        }
    }
}
