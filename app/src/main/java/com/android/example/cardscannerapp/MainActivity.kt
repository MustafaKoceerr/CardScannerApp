package com.android.example.cardscannerapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.android.example.cardscannerapp.base.BaseActivity
import com.android.example.cardscannerapp.databinding.ActivityMainBinding
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/*
        Preview görmek için, bir preview nesnesini oluşturacağız, configuration edip build edeceğiz.
        Sonra CameraX lifecycle'ına bağlayacağız.
 */
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // handle permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false // eğer herhangi bir izin reddedilmişse false'a çevir
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }

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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        // ProcessCameraProvider ile CameraX'in ait olduğu component'in lifecycle-aware olmasını sağlıyor.


        // Parameters:
        //listener - the listener to run when the computation is complete
        //executor - the executor to run the listener in
        cameraProviderFuture.addListener(Runnable{
            // Used to Bind the Lifecycle of Cameras to the Lifecycle Owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview (On izleme)
            // burada nesnemizi build ediyoruz sonra CameraX lifecycle'ına bağlayacağız.
            // Build ederken configration'larını değiştirebiliriz, also ile de xml'imize bağladık.
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select Back Camera As A Default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind Use Cases Before Rebinding
                cameraProvider.unbindAll()
                // Bind Use Cases to Camera
                cameraProvider.bindToLifecycle(
                    this@MainActivity,
                    cameraSelector,
                    preview
                )
            }catch (ex:Exception){
                Log.e(TAG, "Use case binding failed", ex)
            }
        },ContextCompat.getMainExecutor(this@MainActivity))

    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

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

    private fun takePhoto(view: View) {

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