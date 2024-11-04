package com.moyasar.android.sdk.creditcard.presentation.view.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.extensions.gone
import com.moyasar.android.sdk.core.extensions.show

class MoyasarInputField @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var labelTextView: TextView
    internal var inputEditText: EditText
    private var errorTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_moyasar_custom_input_field, this, true)
        orientation = VERTICAL

        labelTextView = findViewById(R.id.labelTextView)
        inputEditText = findViewById(R.id.inputEditText)
        errorTextView = findViewById(R.id.errorTextView)
    }

    private fun validateInput(text: String) {
        if (text.isEmpty()) {
            showError()
        } else {
            hideError()
        }
    }

    private fun showError() {
        errorTextView.visibility = View.VISIBLE
        inputEditText.setBackgroundResource(R.drawable.bg_moyasar_edittext_background_error)
        labelTextView.gone()
    }

    private fun hideError() {
        errorTextView.visibility = View.GONE
        inputEditText.setBackgroundResource(R.drawable.bg_moyasar_edittext_background)
        labelTextView.show()
    }

    fun setLabelText(label: String) {
        labelTextView.text = label
        labelTextView.show()
    }

    fun setHintText(hint: String) {
        inputEditText.hint = hint
    }

    fun setInputType(inputType: Int){
        inputEditText.inputType = inputType
    }

    fun getInputText(): String {
        return inputEditText.text.toString()
    }


    fun setError(errorMsg: String?) {
        errorTextView.text = errorMsg
        if (errorMsg.isNullOrEmpty().not()) showError()
        else hideError()
    }
}