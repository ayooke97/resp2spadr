package com.example.aicourse
import android.util.Log
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
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.aicourse.database.DatabaseHelper
import com.example.aicourse.databinding.ActivityAddCourseBinding
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.MalformedURLException

class AddCourseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCourseBinding
    private var dbHelper: DatabaseHelper? = null
    private var selectedImageUri: Uri? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                loadImagePreview(uri.toString())
                // Clear URL field when image is selected
                binding.etImageUrl?.setText("")
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
        try {
            super.onCreate(savedInstanceState)
            binding = ActivityAddCourseBinding.inflate(layoutInflater)
            setContentView(binding.root)

            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Add New Course"

            dbHelper = DatabaseHelper(this)

            setupImageSelection()
            setupImageUrlValidation()
            setupSaveButton()
        } catch (e: Exception) {
            Log.e("AddCourseActivity", "onCreate failed", e)
            finish()
        }
    }

    private fun setupImageSelection() {
        binding.fabSelectImage?.setOnClickListener {
            checkAndRequestPermission()
        }
    }

    private fun checkAndRequestPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // For Android 13 and above
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
                // For Android 12 and below
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
        binding.btnValidateUrl?.setOnClickListener {
            try {
                val url = binding.etImageUrl?.text?.toString()?.trim()
                if (url.isNullOrEmpty()) {
                    binding.etImageUrl?.error = "Please enter a URL"
                    return@setOnClickListener
                }
                validateImageUrl(url)
            } catch (e: Exception) {
                Log.e("AddCourseActivity", "Error validating URL", e)
                Toast.makeText(this, "Error validating URL", Toast.LENGTH_SHORT).show()
            }
        }

        binding.etImageUrl?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Clear selected image when URL is entered
                if (!s.isNullOrBlank()) {
                    selectedImageUri = null
                }
            }
        })
    }

    private fun validateImageUrl(url: String) {
        if (!URLUtil.isValidUrl(url)) {
            binding.etImageUrl?.error = "Invalid URL format"
            return
        }

        coroutineScope.launch {
            try {
                binding.btnValidateUrl?.isEnabled = false
                binding.btnValidateUrl?.text = "Checking..."

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
                    binding.etImageUrl?.error = null
                    loadImagePreview(url)
                    Toast.makeText(this@AddCourseActivity, "Valid image URL", Toast.LENGTH_SHORT).show()
                } else {
                    binding.etImageUrl?.error = "Invalid image URL or image not accessible"
                }
            } catch (e: Exception) {
                binding.etImageUrl?.error = "Error validating URL: ${e.message}"
            } finally {
                binding.btnValidateUrl?.isEnabled = true
                binding.btnValidateUrl?.text = "Validate"
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
                    .into(binding.ivPreview)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.ivPreview.setImageResource(R.drawable.placeholder)
            }
        } else {
            binding.ivPreview.setImageResource(R.drawable.placeholder)
        }
    }

    private fun setupSaveButton() {
        binding.btnSave?.setOnClickListener {
            try {
                val title = binding.etTitle?.text?.toString()?.trim() ?: ""
                val description = binding.etDescription?.text?.toString()?.trim() ?: ""
                val duration = binding.etDuration?.text?.toString()?.trim() ?: ""
                
                if (!validateInput(title, description, duration)) return@setOnClickListener
                
                dbHelper?.addCourse(
                    title,
                    description,
                    duration,
                    selectedImageUri?.toString() ?: ""
                )
                Toast.makeText(this, "Course saved", Toast.LENGTH_SHORT).show()
                finish()
                
            } catch (e: Exception) {
                Log.e("AddCourseActivity", "Error saving course", e)
                Toast.makeText(this, "Save failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
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
            binding.etTitle?.error = "Title is required"
            return false
        }
        if (description.isEmpty()) {
            binding.etDescription?.error = "Description is required"
            return false
        }
        val durationPattern = Regex("""^\d+\s*(hours|hrs|minutes|mins)$""")
        if (!durationPattern.matches(duration)) {
            binding.etDuration?.error = "Invalid duration format"
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
        dbHelper?.close()
        super.onDestroy()
        coroutineScope.cancel()
    }
}
