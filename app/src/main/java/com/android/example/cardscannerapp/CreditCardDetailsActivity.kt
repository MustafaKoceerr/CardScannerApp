package com.android.example.cardscannerapp

import android.os.Bundle
import android.view.LayoutInflater
import com.android.example.cardscannerapp.base.BaseActivity
import com.android.example.cardscannerapp.databinding.ActivityCreditCardDetailsBinding

class CreditCardDetailsActivity : BaseActivity<ActivityCreditCardDetailsBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEvents()

        val bundle = intent.getBundleExtra("mapBundle")
        val map = bundle?.let {
            it.keySet().associateWith { key -> it.getString(key) }
        }
        bindDataWithComponents(map)
    }

    private fun initEvents() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun bindDataWithComponents(map: Map<String, String?>?) {
      map?.let {mapp->
          with(binding) {
              mapp.get("cardNumber")?.let {
                  textViewCardNumber.text = it
              }
              mapp.get("cvc")?.let {
                  textViewCvcNumber.text = "CVC: " + it
              }
              mapp.get("expiryDate")?.let {
                  textViewValidDate.text = "VALID THRU: " + it
              }
          }
      }
    }

    override fun getActivityViewBinding(inflater: LayoutInflater): ActivityCreditCardDetailsBinding {
        return ActivityCreditCardDetailsBinding.inflate(inflater)
    }
}