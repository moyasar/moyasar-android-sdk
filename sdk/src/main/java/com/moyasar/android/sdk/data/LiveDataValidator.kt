package com.moyasar.android.sdk.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.view.View

typealias Predicate = (value: String?) -> Boolean

class LiveDataValidator(private val liveData: LiveData<String>) {
    private val rules = mutableListOf<ValidationRule>()

    private val error = MutableLiveData<String?>()

    fun isValid(): Boolean {
        for (rule in rules) {
            if (rule.predicate(liveData.value)) {
                error.value = rule.error
                return false
            }
        }

        error.value = null
        return true
    }

    fun addRule(message: String, predicate: Predicate) {
        rules.add(ValidationRule(predicate, message))
    }

    fun onFieldFocusChange(view: View, hasFocus: Boolean) {
        when (hasFocus) {
            true -> error.value = null
            false -> isValid()
        }
    }

    data class ValidationRule(val predicate: Predicate, val error: String)
}
