package com.android.example.cardscannerapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.android.example.cardscannerapp.base.BaseActivity
import com.android.example.cardscannerapp.databinding.ActivityMainBinding
import com.android.example.cardscannerapp.util.showSnackbar
import com.google.mlkit.vision.text.Text
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

/*
        Preview görmek için, bir preview nesnesini oluşturacağız, configuration edip build edeceğiz.
        Sonra CameraX lifecycle'ına bağlayacağız.
        Diğer use case'ler de preview gibi çalışıyorlar, usecase'nin nesnesini oluşturuyorsun ve bindlifecycle'da bağlıyorsun.

 */
class MainActivity : BaseActivity<ActivityMainBinding>(), AnalysisListener { // END OF MAIN ACTIVITY
    private var imageCapture: ImageCapture? =
        null // Bu sınıf, kamera aracılığıyla fotoğraf çekme işlemini yönetir.

    private val imageAnalyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview

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
                binding.main.showSnackbar("Permission request denied")
            } else {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request Camera Permissions
        if (allPermissionGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        // todo ne yaptığını araştır
    }


    private fun extractCardInfo(text: String): Map<String, String?>? {
        val cardInfo = mutableMapOf<String, String?>()

        // Regex patterns
        val cardNumberPattern = Pattern.compile("\\b\\d{4} \\d{4} \\d{4} \\d{4}\\b")
        val cvcPattern = Pattern.compile("\\b\\d{3}\\b")
        val expiryDatePattern = Pattern.compile("\\b\\d{2}/\\d{2}\\b")
        // Find matches
        val cardNumberMatcher = cardNumberPattern.matcher(text)
        val cvcMatcher = cvcPattern.matcher(text)
        val expiryDateMatcher = expiryDatePattern.matcher(text)

        if (cardNumberMatcher.find()) {
            cardInfo["cardNumber"] = formatCardNumber(cardNumberMatcher.group())
        }
        if (cvcMatcher.find()) {
            cardInfo["cvc"] = cvcMatcher.group()
        }
        if (expiryDateMatcher.find()) {
            cardInfo["expiryDate"] = expiryDateMatcher.group()
        }

        if (cardInfo["cardNumber"] == null || cardInfo["expiryDate"] == null) {
            return null
        }
        return cardInfo
    }

    private fun formatCardNumber(cardNumber: String?): String {
        // Format the card number to be in groups of 4
        cardNumber?.let {
            return cardNumber.replace(" ", "").chunked(4).joinToString(" ")
        }
        return ""
    }

    private fun goToSecondActivity(cardInfo: Map<String, String?>) {
        val bundle = Bundle()
        for ((key, value) in cardInfo) {
            bundle.putString(key, value)
        }
        Intent(this, CreditCardDetailsActivity::class.java).also {
            it.putExtra("mapBundle", bundle)
            startActivity(it)

        }

    }


    private fun processRecognizedText(visionText: Text): String? {
        if (visionText.textBlocks.isEmpty()) {
            // Metin tanıma işlemi başarılı oldu, ancak metin bulunamadı
            val errorMessage = "No text found try again"
            Log.e(TAG, errorMessage)
            binding.main.showSnackbar(errorMessage)
            return null
        }
        val stringBuilder = StringBuilder()

        visionText.textBlocks.flatMap { it.lines }
            .flatMap { it.elements }
            .forEach { element ->
                // Metni al ve işleyin
                val text = element.text
                // Burada metni bir StringBuilder'a ekleyebilir veya başka işlemler yapabilirsiniz
                stringBuilder.append(text).append(" ")
            }

        return stringBuilder.toString()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        // ProcessCameraProvider ile CameraX'in ait olduğu component'in lifecycle-aware olmasını sağlıyor.

        // Parameters:
        //listener - the listener to run when the computation is complete
        //executor - the executor to run the listener in
        cameraProviderFuture.addListener(Runnable {
            // Used to Bind the Lifecycle of Cameras to the Lifecycle Owner
            cameraProvider = cameraProviderFuture.get()

            // Preview (On izleme)
            // burada nesnemizi build ediyoruz sonra CameraX lifecycle'ına bağlayacağız.
            // Build ederken configration'larını değiştirebiliriz, also ile de xml'imize bağladık.
            binding.viewFinder.scaleType = PreviewView.ScaleType.FILL_CENTER
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            // Preview gibi, imageCapture'yi build ediyoruz ve aşağıda bindlifecycle ile bağlayacağız
            //imageCapture = ImageCapture.Builder().build()

            // ImageAnalysis
            imageAnalyzer.setAnalyzer(cameraExecutor, CreditCardImageAnalyzer(this@MainActivity))



            try {
                bindCamera()
            } catch (ex: Exception) {
                Log.e(TAG, "Use case binding failed", ex)
            }
        }, ContextCompat.getMainExecutor(this@MainActivity))
    }

    private fun bindCamera(){
        // Select Back Camera As A Default
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        // Unbind Use Cases Before Rebinding
        cameraProvider.unbindAll()
        // Bind Use Cases to Camera
        cameraProvider.bindToLifecycle(
            this@MainActivity,
            cameraSelector,
            preview,
            imageAnalyzer,

        )
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

    }

    override fun onResume() {
        super.onResume()
        imageAnalyzer.setAnalyzer(cameraExecutor, CreditCardImageAnalyzer(this@MainActivity))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        const val TAG = "CameraXApp"
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

    override fun onAnalysisComleted(result: String) {
        // bana okunan text'i verdi. şimdi bunu işlemem gerek.
//        Log.i(TAG, "analiz ediliyor text: $result")
        if (result.isNotEmpty()) {
            val cardInfo = extractCardInfo(result)
            if (cardInfo == null) {
                binding.main.showSnackbar("Card can't be read. Please try again!")
            } else {
                imageAnalyzer.clearAnalyzer()
                goToSecondActivity(cardInfo)
                // todo preview'i unbind etmediğim için hata veriyor.
            }
        }
    }

}

