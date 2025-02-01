package com.example.aicourse

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.aicourse.database.DatabaseHelper
import com.example.aicourse.databinding.ActivityUpdateCourseBinding
import com.example.aicourse.model.Course
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlinx.coroutines.*
import java.net.HttpURLConnection

class UpdateCourseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateCourseBinding
    private lateinit var dbHelper: DatabaseHelper
    private var selectedImageUri: Uri? = null
    private var courseId: Int = -1
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                loadImagePreview(uri.toString())
                binding.etUpdateImageUrl.setText("")
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permission required to select images", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Update Course"

        dbHelper = DatabaseHelper(this)
        courseId = intent.getIntExtra("course_id", -1)

        if (courseId == -1) {
            Toast.makeText(this, "Invalid course ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadCourseData()
        setupImageSelection()
        setupImageUrlValidation()
        setupSaveButton()
    }

    private fun loadCourseData() {
        val course = dbHelper.getCourse(courseId)
        course?.let {
            binding.etUpdateTitle.setText(it.title)
            binding.etUpdateDescription.setText(it.description)
            binding.etUpdateDuration.setText(it.duration)
            binding.etUpdateImageUrl.setText(it.imageUrl)
            loadImagePreview(it.imageUrl)
        } ?: run {
            Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupImageSelection() {
        binding.fabSelectImage.setOnClickListener {
            checkAndRequestPermission()
        }
    }

    private fun checkAndRequestPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openImagePicker()
                    }
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) -> {
                        Toast.makeText(
                            this,
                            "Permission needed to access images",
                            Toast.LENGTH_SHORT
                        ).show()
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }
            }
            else -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openImagePicker()
                    }
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) -> {
                        Toast.makeText(
                            this,
                            "Permission needed to access images",
                            Toast.LENGTH_SHORT
                        ).show()
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun setupImageUrlValidation() {
        binding.btnValidateUrl.setOnClickListener {
            val url = binding.etUpdateImageUrl.text.toString().trim()
            if (url.isEmpty()) {
                binding.etUpdateImageUrl.error = "Please enter a URL"
                return@setOnClickListener
            }
            validateImageUrl(url)
        }

        binding.etUpdateImageUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    selectedImageUri = null
                }
            }
        })
    }

    private fun validateImageUrl(url: String) {
        if (!URLUtil.isValidUrl(url)) {
            binding.etUpdateImageUrl.error = "Invalid URL format"
            return
        }

        coroutineScope.launch {
            try {
                binding.btnValidateUrl.isEnabled = false
                binding.btnValidateUrl.text = "Checking..."

                val isValid = withContext(Dispatchers.IO) {
                    try {
                        val connection = URL(url).openConnection() as HttpURLConnection
                        connection.requestMethod = "HEAD"
                        connection.connect()
                        
                        val contentType = connection.contentType?.lowercase() ?: ""
                        val responseCode = connection.responseCode
                        
                        connection.disconnect()
                        
                        responseCode == HttpURLConnection.HTTP_OK && contentType.startsWith("image/")
                    } catch (e: Exception) {
                        false
                    }
                }

                if (isValid) {
                    binding.etUpdateImageUrl.error = null
                    loadImagePreview(url)
                    Toast.makeText(this@UpdateCourseActivity, "Valid image URL", Toast.LENGTH_SHORT).show()
                } else {
                    binding.etUpdateImageUrl.error = "Invalid image URL or image not accessible"
                }
            } catch (e: Exception) {
                binding.etUpdateImageUrl.error = "Error validating URL: ${e.message}"
            } finally {
                binding.btnValidateUrl.isEnabled = true
                binding.btnValidateUrl.text = "Validate"
            }
        }
    }

    private fun loadImagePreview(url: String?) {
        if (!url.isNullOrBlank()) {
            try {
                Glide.with(this)
                    .load(url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.placeholder)
                    .into(binding.ivUpdatePreview)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.ivUpdatePreview.setImageResource(R.drawable.placeholder)
            }
        } else {
            binding.ivUpdatePreview.setImageResource(R.drawable.placeholder)
        }
    }

    private fun setupSaveButton() {
        binding.btnUpdateSave.setOnClickListener {
            val title = binding.etUpdateTitle.text.toString().trim()
            val description = binding.etUpdateDescription.text.toString().trim()
            val duration = binding.etUpdateDuration.text.toString().trim()
            val imageUrl = binding.etUpdateImageUrl.text.toString().trim()

            if (validateInput(title, description, duration)) {
                try {
                    val finalImageUrl = when {
                        selectedImageUri != null -> saveImageToInternalStorage(selectedImageUri!!)
                        imageUrl.isNotBlank() -> imageUrl
                        else -> null
                    }

                    if (dbHelper.updateCourse(courseId, title, description, duration, finalImageUrl)) {
                        Toast.makeText(this, "Course updated successfully", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to update course", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error updating course: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open input stream for URI: $uri")
            
        val file = File(filesDir, "course_images")
        if (!file.exists()) {
            file.mkdir()
        }

        val imageFile = File(file, "img_${System.currentTimeMillis()}.jpg")
        FileOutputStream(imageFile).use { outputStream ->
            inputStream.use { input ->
                input.copyTo(outputStream)
            }
        }

        return imageFile.absolutePath
    }

    private fun validateInput(title: String, description: String, duration: String): Boolean {
        if (title.isEmpty()) {
            binding.etUpdateTitle.error = "Title is required"
            return false
        }
        if (description.isEmpty()) {
            binding.etUpdateDescription.error = "Description is required"
            return false
        }
        if (duration.isEmpty()) {
            binding.etUpdateDuration.error = "Duration is required"
            return false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        dbHelper.close()
    }
}
