package com.android.example.cardscannerapp

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.android.example.cardscannerapp.MainActivity.Companion.TAG
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class CreditCardImageAnalyzer(private val listener: AnalysisListener) : ImageAnalysis.Analyzer {

    private val rateLimiter = RateLimiter(500L) // yarim saniye aralikla analiz yapacak
    // todo timer ekle
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        if (image.image != null && rateLimiter.shouldProceed()) {
            val inputImage =
                InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)
            // MLKit doğrudan cameraX image: ImageProxy ile etkileşemiyor
            // inputImage'e çevirmemiz gerekli.
            // Perform image analysis here.
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(inputImage)
                // asenkron çalışır, bundan dolayı image.close'u ayrı ayrı verdim.
                .addOnSuccessListener { visionText ->
                    val resultText = visionText.text
                    listener.onAnalysisComleted(resultText)
                }
                .addOnFailureListener { exception ->
                    val errorMessage = "Text recognition failed"
                    Log.d(TAG, errorMessage, exception)
                }.addOnCompleteListener {
                    image.close()
                }
        } else {
            image.close()
        }

    }


}