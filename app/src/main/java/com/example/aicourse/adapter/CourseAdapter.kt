package com.example.aicourse.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aicourse.R
import com.example.aicourse.model.Course

class CourseAdapter(
    private val courses: List<Course>,
    private val onItemClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.tvTitle)
        val descriptionTextView: TextView = view.findViewById(R.id.tvDescription)
        val durationTextView: TextView = view.findViewById(R.id.tvDuration)
        val courseImageView: ImageView = view.findViewById(R.id.ivCourse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.titleTextView.text = course.title
        holder.descriptionTextView.text = course.description
        holder.durationTextView.text = course.duration

        Glide.with(holder.itemView.context)
            .load(course.imageUrl)
            .placeholder(R.drawable.placeholder)
            .into(holder.courseImageView)

        holder.itemView.setOnClickListener { onItemClick(course) }
    }

    override fun getItemCount() = courses.size
}
