package com.moyasar.android.sdk.core.util

import android.util.Log

/**
 * Created by Mahmoud Ashraf on 22,September,2024
 */
object MoyasarLogger {
    private val isTestModelEnabled = true
    fun log(key: String, value: String){
        if (isTestModelEnabled)
            Log.d(key,value)
    }
}