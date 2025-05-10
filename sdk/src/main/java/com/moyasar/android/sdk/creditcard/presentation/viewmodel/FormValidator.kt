//package com.moyasar.android.sdk.creditcard.presentation.viewmodel
//
//import android.app.Application
//import androidx.lifecycle.MediatorLiveData
//import androidx.lifecycle.MutableLiveData
//import com.moyasar.android.sdk.R
//import com.moyasar.android.sdk.core.extensions.default
//import com.moyasar.android.sdk.core.util.LiveDataValidator
//import com.moyasar.android.sdk.core.util.cleanSpaces
//import com.moyasar.android.sdk.core.util.isValidLuhnNumber
//import com.moyasar.android.sdk.core.util.parseExpiry
//import com.moyasar.android.sdk.creditcard.data.models.CreditCardNetwork
//import com.moyasar.android.sdk.creditcard.data.models.getNetwork
//import com.moyasar.android.sdk.creditcard.data.models.isCreditAllowed
//import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
//
///***************
// * This only a wrapper to wrap Moyasar form data Models And validation Rules
// **************/
//class FormValidator(application: Application) {
//
//    val name = MutableLiveData<String>().default("")
//    val number = MutableLiveData<String>().default("")
//    val cvc = MutableLiveData<String>().default("")
//    val expiry = MutableLiveData<String>().default("")
//
//    internal val _isFormValid = MediatorLiveData<Boolean>().default(false)
//
//
//    val nameValidator = LiveDataValidator(name).apply {
//        val latinRegex = Regex("^[a-zA-Z\\-\\s]+\$")
//        val nameRegex = Regex("^[a-zA-Z\\-]+\\s+?([a-zA-Z\\-]+\\s?)+\$")
//
//        addRule(application.getString(R.string.name_is_required)) { it.isNullOrBlank() }
//        addRule(application.getString(R.string.only_english_alpha)) {
//            !latinRegex.matches(
//                it ?: ""
//            )
//        }
//        addRule(application.getString(R.string.both_names_required)) {
//            !nameRegex.matches(
//                it ?: ""
//            )
//        }
//    }
//
//    val numberValidator = LiveDataValidator(number).apply {
//        addRule(application.getString(R.string.card_number_required)) { it.isNullOrBlank() }
//        addRule(application.getString(R.string.invalid_card_number)) {
//            !isValidLuhnNumber(
//                it ?: ""
//            ) || it?.cleanSpaces().orEmpty().length < 15
//        }
//        addRule(application.getString(R.string.unsupported_network)) {
//            getNetwork(
//                number = it ?: ""
//            ) == CreditCardNetwork.Unknown || !isCreditAllowed(
//                number = it ?: "",
//                allowedNetwork = MoyasarAppContainer.paymentRequest.allowedNetworks
//            )
//        }
//    }
//
//
//    val cvcValidator = LiveDataValidator(cvc).apply {
//        addRule(application.getString(R.string.cvc_required)) { it.isNullOrBlank() }
//        addRule(application.getString(R.string.invalid_cvc)) {
//            when (getNetwork(
//                number = number.value ?: ""
//            )) {
//                CreditCardNetwork.Amex -> (it?.length ?: 0) < 4
//                else -> (it?.length ?: 0) < 3
//            }
//        }
//    }
//
//    val expiryValidator = LiveDataValidator(expiry).apply {
//        addRule(application.getString(R.string.expiry_is_required)) { it.isNullOrBlank() }
//        addRule(application.getString(R.string.invalid_expiry)) {
//            parseExpiry(it ?: "")?.isInvalid() ?: true
//        }
//        addRule(application.getString(R.string.expired_card)) {
//            parseExpiry(it ?: "")?.expired() ?: false
//        }
//    }
//
//    fun validate(isShowError: Boolean = true): Boolean {
//        val validators = listOf(nameValidator, numberValidator, cvcValidator, expiryValidator)
//        return if (isShowError) {
//            validators.all { it.isValid() }.also { _isFormValid.value = it }
//        } else {
//            validators.all { it.isValidWithoutErrorMessage() }.also { _isFormValid.value = it }
//        }
//    }
//
//}