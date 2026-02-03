package com.moyasar.android.sdk.creditcard.presentation.utils

import androidx.lifecycle.MutableLiveData
import com.moyasar.android.sdk.core.domain.entities.ValidationRule
import com.moyasar.android.sdk.creditcard.presentation.model.FormErrorMessage
import com.moyasar.android.sdk.creditcard.presentation.model.InputFieldsUIModel

/**
 * Created by Mahmoud Ashraf on 11,May,2025
 */

internal fun List<ValidationRule>.isValidName(
    value: String,
    inputFieldsValidatorLiveData: MutableLiveData<InputFieldsUIModel>,
    isShowError: Boolean = true,
): Boolean {
    val rules = this
    for (rule in rules) {
        if (rule.predicate(value)) {
            if (isShowError)
                inputFieldsValidatorLiveData.value =
                    inputFieldsValidatorLiveData.value?.copy(
                        errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                            nameErrorMsg = rule.error
                        ) ?: FormErrorMessage(nameErrorMsg = rule.error)
                    )
            return false
        }
    }

    return true
}


internal fun List<ValidationRule>.isValidNumber(
    value: String,
    inputFieldsValidatorLiveData: MutableLiveData<InputFieldsUIModel>,
    isShowError: Boolean = true,
): Boolean {
    val rules = this
    for (rule in rules) {
        if (rule.predicate(value)) {
            if (isShowError)
                inputFieldsValidatorLiveData.value =
                    inputFieldsValidatorLiveData.value?.copy(
                        errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                            numberErrorMsg = rule.error
                        ) ?: FormErrorMessage(numberErrorMsg = rule.error)
                    )
            return false
        }
    }

    return true
}

internal fun List<ValidationRule>.isValidCvc(
    value: String,
    inputFieldsValidatorLiveData: MutableLiveData<InputFieldsUIModel>,
    isShowError: Boolean = true,
): Boolean {
    val rules = this
    for (rule in rules) {
        if (rule.predicate(value)) {
            if (isShowError)
                inputFieldsValidatorLiveData.value =
                    inputFieldsValidatorLiveData.value?.copy(
                        errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                            cvcErrorMsg = rule.error
                        ) ?: FormErrorMessage(cvcErrorMsg = rule.error)
                    )
            return false
        }
    }

    return true
}

internal fun List<ValidationRule>.isValidExpiryDate(
    value: String,
    inputFieldsValidatorLiveData: MutableLiveData<InputFieldsUIModel>,
    isShowError: Boolean = true,
): Boolean {
    val rules = this
    for (rule in rules) {
        if (rule.predicate(value)) {
            if (isShowError)
                inputFieldsValidatorLiveData.value =
                    inputFieldsValidatorLiveData.value?.copy(
                        errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                            expiryDateErrorMsg = rule.error
                        ) ?: FormErrorMessage(expiryDateErrorMsg = rule.error)
                    )
            return false
        }
    }

    return true
}