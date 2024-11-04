package com.moyasar.android.sdk.creditcard.presentation.view.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.extensions.gone
import com.moyasar.android.sdk.core.extensions.show
import com.moyasar.android.sdk.creditcard.presentation.model.InputCreditCardUIModel

class MoyasarInputCardField @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var labelTextView: TextView
    internal var inputEditTextCardNumber: EditText
    internal var inputEditTextCardExpiryDate: EditText
    internal var inputEditTextCardCvc: EditText
    private var errorTextView: TextView
    private var linearLayoutContainer: LinearLayout
    internal var imgVisa: ImageView
    internal var imgMaster: ImageView
    internal var imgMada: ImageView
    internal var imgAmex: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_moyasar_custom_credit_card_field, this, true)
        orientation = VERTICAL

        labelTextView = findViewById(R.id.labelTextView)
        inputEditTextCardNumber = findViewById(R.id.inputEditTextCardNumber)
        inputEditTextCardExpiryDate = findViewById(R.id.inputEditTextCardExpiryDate)
        inputEditTextCardCvc = findViewById(R.id.inputEditTextCardCvc)
        errorTextView = findViewById(R.id.errorTextView)
        linearLayoutContainer = findViewById(R.id.llContainer)
        imgVisa = findViewById(R.id.img_visa)
        imgMaster= findViewById(R.id.img_master)
        imgMada = findViewById(R.id.img_mada)
        imgAmex = findViewById(R.id.img_amex)
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
        linearLayoutContainer.setBackgroundResource(R.drawable.bg_moyasar_edittext_background_error)
        labelTextView.gone()
    }

    private fun hideError() {
        errorTextView.visibility = View.GONE
        linearLayoutContainer.setBackgroundResource(R.drawable.bg_moyasar_edittext_background)
        labelTextView.show()
    }

    fun setLabelText(label: String) {
        labelTextView.text = label
        labelTextView.show()
    }

    fun setHintText(hint: InputCreditCardUIModel) {
      inputEditTextCardNumber.hint = hint.numberHint
      inputEditTextCardExpiryDate.hint = hint.expiryDateHint
      inputEditTextCardCvc.hint = hint.cvcHint
    }

    fun setInputType(inputType: InputCreditCardUIModel){
        inputEditTextCardNumber.inputType = inputType.numberType
        inputEditTextCardExpiryDate.inputType = inputType.expiryDateType
        inputEditTextCardCvc.inputType = inputType.cvcType
    }




    fun setError(errorMsg: String?) {
        errorTextView.text = errorMsg
        if (errorMsg.isNullOrEmpty().not()) showError()
        else hideError()
    }
}