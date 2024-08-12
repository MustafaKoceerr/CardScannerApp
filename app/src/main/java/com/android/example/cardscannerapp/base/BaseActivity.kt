package com.android.example.cardscannerapp.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.android.example.cardscannerapp.R

abstract class BaseActivity<B: ViewBinding> : AppCompatActivity(){
    protected lateinit var binding: B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = getActivityViewBinding(layoutInflater)
        with(binding.root){
            setContentView(this)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(this.id)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }





    }

    abstract fun getActivityViewBinding(inflater: LayoutInflater) : B

}