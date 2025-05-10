package com.moyasar.android.sdk.core.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


typealias Predicate = (value: String?) -> Boolean

class LiveDataValidator(private val liveData: LiveData<String>) {
    private val rules = mutableListOf<ValidationRule>()

    val error = MutableLiveData<String?>()

    fun isValid(): Boolean {
        for (rule in rules) {
            if (rule.predicate(liveData.value)) {
                error.value = rule.error
                MoyasarLogger.log("setOnFocus","errMsg "+rule.error)
                return false
            }
        }

        error.value = null
        return true
    }

    fun isValidWithoutErrorMessage(): Boolean {
        for (rule in rules) {
            if (rule.predicate(liveData.value)) {
                return false
            }
        }

        return true
    }

    fun addRule(message: String, predicate: Predicate) {
        rules.add(ValidationRule(predicate, message))
    }

    fun onFieldFocusChange(hasFocus: Boolean) {
        when (hasFocus) {
            true -> error.value = null
            false -> isValid()
        }
        MoyasarLogger.log("setOnFocus","isValid "+isValid())
    }

    data class ValidationRule(val predicate: Predicate, val error: String)
}