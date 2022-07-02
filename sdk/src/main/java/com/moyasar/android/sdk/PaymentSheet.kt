package com.moyasar.android.sdk

import android.widget.Toast
import androidx.activity.ComponentActivity
import com.moyasar.android.sdk.ui.PaymentSheetContract

class PaymentSheet(
    private val context: ComponentActivity,
    private val callback: PaymentSheetResultCallback,
    private val config: PaymentConfig
) {
    private val sheetActivity = context.registerForActivityResult(PaymentSheetContract()) {
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
