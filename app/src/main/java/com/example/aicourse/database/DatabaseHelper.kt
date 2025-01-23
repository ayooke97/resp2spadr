package com.example.aicourse.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
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
        var cursor: Cursor? = null
        
        try {
            cursor = db.rawQuery(selectQuery, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    do {
                        val course = Course(
                            id = it.getInt(it.getColumnIndexOrThrow(KEY_ID)),
                            title = it.getString(it.getColumnIndexOrThrow(KEY_TITLE)),
                            description = it.getString(it.getColumnIndexOrThrow(KEY_DESCRIPTION)),
                            duration = it.getString(it.getColumnIndexOrThrow(KEY_DURATION)),
                            imageUrl = it.getString(it.getColumnIndexOrThrow(KEY_IMAGE_URL))
                        )
                        courses.add(course)
                    } while (it.moveToNext())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return courses
    }

    fun getCourse(id: Int): Course? {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var course: Course? = null
        
        try {
            cursor = db.query(
                TABLE_COURSES,
                arrayOf(KEY_ID, KEY_TITLE, KEY_DESCRIPTION, KEY_DURATION, KEY_IMAGE_URL),
                "$KEY_ID=?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )
            
            cursor?.let {
                if (it.moveToFirst()) {
                    course = Course(
                        id = it.getInt(it.getColumnIndexOrThrow(KEY_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(KEY_TITLE)),
                        description = it.getString(it.getColumnIndexOrThrow(KEY_DESCRIPTION)),
                        duration = it.getString(it.getColumnIndexOrThrow(KEY_DURATION)),
                        imageUrl = it.getString(it.getColumnIndexOrThrow(KEY_IMAGE_URL))
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return course
    }

    fun updateCourse(course: Course): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_TITLE, course.title)
        values.put(KEY_DESCRIPTION, course.description)
        values.put(KEY_DURATION, course.duration)
        values.put(KEY_IMAGE_URL, course.imageUrl)
        
        val success = db.update(
            TABLE_COURSES,
            values,
            "$KEY_ID=?",
            arrayOf(course.id.toString())
        ) > 0
        
        db.close()
        return success
    }

    fun deleteCourse(id: Int): Boolean {
        val db = this.writableDatabase
        val success = db.delete(
            TABLE_COURSES,
            "$KEY_ID=?",
            arrayOf(id.toString())
        ) > 0
        
        db.close()
        return success
    }
}
