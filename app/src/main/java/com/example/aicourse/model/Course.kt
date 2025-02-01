package com.example.aicourse.model

data class Course(
    val id: Int = 0,
    val title: String,
    val description: String,
    val duration: String,
    val imageUrl: String? = null
)
