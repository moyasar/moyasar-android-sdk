package com.moyasar.android.sdk.core.util

import android.util.Log

/**
 * Created by Mahmoud Ashraf on 22,September,2024
 */
object MoyasarLogger {
    private const val IS_TEST_MODE_ENABLED = true
    fun log(key: String, value: String){
        if (IS_TEST_MODE_ENABLED)
            Log.d(key,value)
    }
}