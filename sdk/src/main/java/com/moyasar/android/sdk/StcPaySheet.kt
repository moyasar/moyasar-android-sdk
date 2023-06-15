package com.moyasar.android.sdk

import androidx.activity.ComponentActivity
import com.moyasar.android.sdk.ui.StcPaySheetContract

class StcPaySheet (
    private val context: ComponentActivity,
    private val callback: PaymentSheetResultCallback,
    private val config: PaymentConfig
) {
    private val sheetActivity = context.registerForActivityResult(StcPaySheetContract()) {
        callback.onResult(it)
    }

    fun present() {
        val configError = config.validate();
        if (configError.any()) {
            throw InvalidConfigException(configError)
        }

        sheetActivity.launch(config)
    }
}