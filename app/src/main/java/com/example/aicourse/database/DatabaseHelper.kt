package com.example.aicourse.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.aicourse.model.Course

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "CourseDB"
        private const val DATABASE_VERSION = 1
        private const val TABLE_COURSES = "courses"
        
        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DURATION = "duration"
        private const val KEY_IMAGE_URL = "image_url"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE " + TABLE_COURSES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITLE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DURATION + " TEXT,"
                + KEY_IMAGE_URL + " TEXT" + ")")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COURSES")
        onCreate(db)
    }

    fun addCourse(course: Course): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_TITLE, course.title)
        values.put(KEY_DESCRIPTION, course.description)
        values.put(KEY_DURATION, course.duration)
        values.put(KEY_IMAGE_URL, course.imageUrl)
        
        val id = db.insert(TABLE_COURSES, null, values)
        db.close()
        return id
    }

    fun getAllCourses(): ArrayList<Course> {
        val courses = ArrayList<Course>()
        val selectQuery = "SELECT * FROM $TABLE_COURSES"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        
        if (cursor.moveToFirst()) {
            do {
                val course = Course(
                    id = cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                    title = cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                    description = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                    duration = cursor.getString(cursor.getColumnIndex(KEY_DURATION)),
                    imageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL))
                )
                courses.add(course)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return courses
    }

    fun getCourse(id: Int): Course? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_COURSES,
            arrayOf(KEY_ID, KEY_TITLE, KEY_DESCRIPTION, KEY_DURATION, KEY_IMAGE_URL),
            "$KEY_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        
        return if (cursor.moveToFirst()) {
            val course = Course(
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                title = cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                description = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                duration = cursor.getString(cursor.getColumnIndex(KEY_DURATION)),
                imageUrl = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL))
            )
            cursor.close()
            db.close()
            course
        } else {
            null
        }
    }
}
