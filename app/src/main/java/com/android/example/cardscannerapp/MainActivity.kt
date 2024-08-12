package com.android.example.cardscannerapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import com.android.example.cardscannerapp.base.BaseActivity
import com.android.example.cardscannerapp.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request Camera Permission
        if (allPermissionGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }


        cameraExecutor = Executors.newSingleThreadExecutor()
        // todo ne yaptığını araştır
    }


    private fun takePhoto() {}

    private fun captureVideo() {}

    private fun startCamera() {}

    private fun requestPermissions() {}

    private fun allPermissionGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            // all function Returns true if all elements match the given
            ContextCompat.checkSelfPermission(
                // ContextCompat is a utility class that allows you to work with the Context object in a backward-compatible way, supporting older Android versions.
                // By using ContextCompat, you can ensure your code works consistently across different Android versions.
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }

        // Set up the listeners for take photo button
        binding.imageCaptureButton.setOnClickListener(::takePhoto)
    }

    private fun takePhoto(view:View){

    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {// p = 28
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
        //.toTypedArray() fonskiyonu ile Primitive-Ryped bir array'i Object-Typed bir array'e dönüştürebilirsiniz.
        // yani java ArrayList'i olur, performanssız çalışır ama parametre olarak bunu istediği için dönüştürdük.
    }

    override fun getActivityViewBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)

    }
}