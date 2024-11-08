package com.dicoding.asclepius.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.asclepius.MainViewModel
import com.dicoding.asclepius.R
import com.dicoding.asclepius.database.Cancer
import com.dicoding.asclepius.database.CancerDao
import com.dicoding.asclepius.database.CancerRoomDatabase
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.getImageUri
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private lateinit var cancerRoomDatabase: CancerRoomDatabase
    private lateinit var cancerDao: CancerDao
    private var currentImageUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showToast("Camera permission granted")
        } else {
            showToast("Camera permission denied")
        }
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.currentImageUri.observe(this) { uri ->
            uri?.let {
                binding.previewImageView.setImageURI(it)
                binding.previewImageView.tag = it
            }
        }

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        initializeComponents()
    }

    private fun initializeComponents() {
        cancerRoomDatabase = CancerRoomDatabase.getDatabase(this)
        cancerDao = cancerRoomDatabase.noteDao()

        imageClassifierHelper = ImageClassifierHelper(this)

        binding.cameraButton.setOnClickListener { startCamera() }
        binding.galleryButton.setOnClickListener { startGallery() }

        binding.analyzeButton.setOnClickListener {
            val imageUriToAnalyze = viewModel.currentImageUri.value ?: binding.previewImageView.tag as? Uri
            imageUriToAnalyze?.let {
                analyzeImage(it)
            } ?: run {
                showToast(getString(R.string.empty_image_warning))
            }
        }

        binding.historyButton.setOnClickListener {
            val intent = Intent(this, CancerListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startCamera() {
        if (allPermissionsGranted()) {
            currentImageUri = getImageUri(this)
            currentImageUri?.let {
                launcherIntentCamera.launch(it)
            } ?: showToast("Camera URI not available")
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            showToast("Failed to capture image")
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { startCrop(it) } ?: showToast("No media selected")
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
            viewModel.currentImageUri.value = it
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "croppedImage_${UUID.randomUUID()}.jpg"))

        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(4f, 4f)
            .withMaxResultSize(1080, 1920)
            .start(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            if (resultUri != null) {
                viewModel.currentImageUri.value = resultUri
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            cropError?.let {
                showToast("Crop error: ${it.message}")
            }
            viewModel.currentImageUri.value = null
        } else if (resultCode == RESULT_CANCELED && requestCode == UCrop.REQUEST_CROP) {
            showToast("Crop cancelled")
            viewModel.currentImageUri.value = null
        }
    }

    private fun analyzeImage(imageUri: Uri) {
        binding.progressIndicator.visibility = View.VISIBLE

        imageClassifierHelper.classifyStaticImage(imageUri) { result ->
            binding.progressIndicator.visibility = View.GONE

            val (resultLabel, resultScore) = result
            val resultPercent = (resultScore * 100).toInt()

            val cancer = Cancer(
                mediaCover = imageUri.toString(),
                event_name = resultLabel,
                event_owner = resultPercent
            )

            Thread {
                try {
                    cancerDao.insert(cancer)
                } catch (e: Exception) {
                    Log.e("Database Error", "Failed to insert data: ${e.message}")
                }
            }.start()

            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, imageUri.toString())
            intent.putExtra(ResultActivity.EXTRA_RESULT_LABEL, resultLabel)
            intent.putExtra(ResultActivity.EXTRA_RESULT_PERCENT, resultPercent)
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        imageClassifierHelper.close()
    }
}