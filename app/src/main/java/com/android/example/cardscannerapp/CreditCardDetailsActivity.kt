package com.android.example.cardscannerapp

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.android.example.cardscannerapp.base.BaseActivity
import com.android.example.cardscannerapp.databinding.ActivityCreditCardDetailsBinding
import com.android.example.cardscannerapp.CreditCardDetailsContants.CARD_NUMBER
import com.android.example.cardscannerapp.CreditCardDetailsContants.CVC_NUMBER
import com.android.example.cardscannerapp.CreditCardDetailsContants.VALID_MONTH
import com.android.example.cardscannerapp.CreditCardDetailsContants.VALID_YEAR
import com.android.example.cardscannerapp.CreditCardDetailsContants.CARD_LENGTH
import com.android.example.cardscannerapp.CreditCardDetailsContants.CVC_LENGTH
import com.android.example.cardscannerapp.CreditCardDetailsContants.VALID_MONTH_LENGTH
import com.android.example.cardscannerapp.CreditCardDetailsContants.VALID_YEAR_LENGTH

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
        updateButtonState()

        with(binding) {
            btnBack.setOnClickListener {
                finish()
            }

            listOf(
                editTextCardNumber,
                editTextValidMonth,
                editTextValidYear,
                editTextCvcNumber
            ).forEach {
                edtTxtControlWatcher(it)
            }
//            edtTxtControlWatcher(editTextCardNumber)
//            edtTxtControlWatcher(editTextValidMonth)
//            edtTxtControlWatcher(editTextValidYear)
//            edtTxtControlWatcher(editTextCvcNumber)

            btnUpdate.setOnClickListener(::updateCard)
        }
    }

    private fun updateCard(view: View) {
        val editTextLengthList = getEditTextLengthList()

        with(binding) {
            if (editTextLengthList[CARD_NUMBER] == CARD_LENGTH) {
                textViewCardNumber.text = editTextCardNumber.text.toString()
                editTextCardNumber.setText("")
            }
            if (editTextLengthList[CVC_NUMBER] == CVC_LENGTH) {
                textViewCvcNumber.text = "CVC: ${editTextCvcNumber.text}"
                editTextCvcNumber.setText("")
            }
            if (editTextLengthList[VALID_MONTH] == VALID_MONTH_LENGTH) {
                // index 12 ve 13
                val takeLastThree = textViewValidDate.text.toString().takeLast(3)
                textViewValidDate.text = "VALID THRU: ${editTextValidMonth.text}$takeLastThree" +
                        editTextValidMonth.setText("")
            }
            if (editTextLengthList[CreditCardDetailsContants.VALID_YEAR] == VALID_YEAR_LENGTH) {
                val takeFirstFiftheen = textViewValidDate.text.toString().substring(0, 15)
                textViewValidDate.text = takeFirstFiftheen + editTextValidYear.text.toString()
                editTextValidYear.setText("")
            }

        }
    }

    private fun getEditTextLengthList(): IntArray {
        with(binding) {
            return intArrayOf(
                editTextCardNumber.text.toString().length,
                editTextCvcNumber.text.toString().length,
                editTextValidMonth.text.toString().length,
                editTextValidYear.text.toString().length
            )
        }
    }

    private fun updateButtonState() {
        val editTextLenghtList = getEditTextLengthList()
        with(binding.btnUpdate) {
            isEnabled = (
                    editTextLenghtList[CARD_NUMBER] == CARD_LENGTH ||
                            editTextLenghtList[CVC_NUMBER] == CVC_LENGTH ||
                            editTextLenghtList[VALID_MONTH] == VALID_MONTH_LENGTH ||
                            editTextLenghtList[VALID_YEAR] == VALID_YEAR_LENGTH
                    )
            alpha = if (isEnabled) 1F else 0.65F

        }
    }

    private fun edtTxtControlWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val editTextLengthList = getEditTextLengthList()
                with(binding) {
                    when (editText) {
                        editTextCvcNumber -> {
                            if (editTextLengthList[CVC_NUMBER] == CVC_LENGTH) {
                                hideKeyboard(this@CreditCardDetailsActivity, editTextCvcNumber)
                            }
                        }

                        editTextValidMonth -> {
                            if (editTextLengthList[VALID_MONTH] == VALID_MONTH_LENGTH) {
                                hideKeyboard(this@CreditCardDetailsActivity, editTextValidMonth)
                            }
                        }

                        editTextValidYear -> {
                            if (editTextLengthList[VALID_YEAR] == VALID_YEAR_LENGTH) {
                                hideKeyboard(this@CreditCardDetailsActivity, editTextValidYear)
                            }
                        }

                        editTextCardNumber -> {
                            if (isFormatting) return

                            isFormatting = true
                            val formattedText = buildString {
                                s.toString()
                                    .replace(" ", "")
                                    .chunked(4)
                                    .forEach {
                                        append(it)
                                        append(" ")
                                    }
                            }.trimEnd()
                            editText.apply {
                                setText(formattedText)
                                setSelection(formattedText.length)
                            }
                            isFormatting = false

                            if (editTextLengthList[CARD_NUMBER] == CARD_LENGTH) {
                                hideKeyboard(this@CreditCardDetailsActivity, editTextCardNumber)
                            }
                        }
                    }
                }

                updateButtonState()
            }

        })
    }


    private fun bindDataWithComponents(map: Map<String, String?>?) {
        map?.let { mapp ->
            with(binding) {
                mapp.get("cardNumber")?.let {
                    textViewCardNumber.text = it
                }
                mapp.get("cvc")?.let {
                    textViewCvcNumber.text = "CVC: $it"
                }
                mapp.get("expiryDate")?.let {
                    textViewValidDate.text = "VALID THRU: $it"
                }
            }
        }
    }

    override fun getActivityViewBinding(inflater: LayoutInflater): ActivityCreditCardDetailsBinding {
        return ActivityCreditCardDetailsBinding.inflate(inflater)
    }

    private fun hideKeyboard(context: Context, view: View) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}