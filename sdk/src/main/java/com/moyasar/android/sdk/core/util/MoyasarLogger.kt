package com.moyasar.android.sdk.core.util

import android.util.Log
import com.moyasar.android.sdk.BuildConfig

/**
 * Created by Mahmoud Ashraf on 22,September,2024
 */
object MoyasarLogger {
    fun log(key: String, value: String){
        if (BuildConfig.DEBUG)
            Log.d(key,value)
    }
}