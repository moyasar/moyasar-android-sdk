package com.moyasar.android.sdk.core.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer


fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> = MediatorLiveData<T>().also { mediator ->
    mediator.addSource(this, object : Observer<T> {
        private var isInitialized = false
        private var previousValue: T? = null

        override fun onChanged(newValue: T) {
            val wasInitialized = isInitialized
            if (!isInitialized) {
                isInitialized = true
            }
            if(!wasInitialized || newValue != previousValue) {
                previousValue = newValue
                mediator.postValue(newValue)
            }
        }
    })
}