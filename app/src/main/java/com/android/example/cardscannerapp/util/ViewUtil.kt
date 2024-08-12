package com.android.example.cardscannerapp.util

import android.view.View
import com.google.android.material.snackbar.Snackbar

// Extension function to show a Snackbar message
fun View.showSnackbar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}