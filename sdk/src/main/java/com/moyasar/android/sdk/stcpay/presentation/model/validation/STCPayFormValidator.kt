package com.moyasar.android.sdk.stcpay.presentation.model.validation

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.extensions.default
import com.moyasar.android.sdk.core.util.LiveDataValidator
import com.moyasar.android.sdk.stcpay.presentation.model.validator.STCPayOTPValidator
import com.moyasar.android.sdk.stcpay.presentation.model.validator.SaudiPhoneNumberValidator

/**
 * Created by Mahmoud Ashraf on 06,October,2024
 */
class STCPayFormValidator(application: Application) {
    // STC pay fields
    val mobileNumber = MutableLiveData<String>().default("")
    val stcPayOTP = MutableLiveData<String>().default("")
    internal val _isSTCPayFormValid = MediatorLiveData<Boolean>().default(false)

    val mobileNumberValidator = LiveDataValidator(mobileNumber).apply {
        addRule(application.getString(R.string.mobile_number_is_required)) { it.isNullOrBlank() }
        addRule(application.getString(R.string.invalid_mobile_number)) {
            !SaudiPhoneNumberValidator.isValidSaudiPhoneNumber(
                it ?: ""
            )
        }
    }

    val stcPayOTPValidator = LiveDataValidator(stcPayOTP).apply {
        addRule(application.getString(R.string.invalid_stc_pay_otp)) {
            !STCPayOTPValidator.isValidOtp(
                it ?: ""
            )
        }
    }

    fun validateSTCMobile(isShowError: Boolean = true): Boolean {
        val validators = listOf(mobileNumberValidator)
        return if (isShowError) {
            validators.all { it.isValid() }.also {
                _isSTCPayFormValid.value = it
            }
        } else {
            validators.all { it.isValidWithoutErrorMessage() }.also { _isSTCPayFormValid.value = it }
        }
    }

    fun validateSTCOTP(isShowError: Boolean = true): Boolean {
        val validators = listOf(stcPayOTPValidator)
        return if (isShowError) {
            validators.all { it.isValid() }.also { _isSTCPayFormValid.value = it }
        } else {
            validators.all { it.isValidWithoutErrorMessage() }.also { _isSTCPayFormValid.value = it }
        }
    }
}