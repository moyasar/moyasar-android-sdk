package com.moyasar.android.sdk

import android.widget.Toast
import androidx.activity.ComponentActivity
import com.moyasar.android.sdk.ui.PaymentSheetActivityResultContract

class PaymentSheet(
    private val context: ComponentActivity,
    private val callback: PaymentSheetResultCallback,
    private val config: PaymentConfig
) {
    fun present() {
        val configError = config.validate();
        if (configError.any()) {
            throw InvalidConfigException(configError)
        }

        val sheetActivity = context.registerForActivityResult(PaymentSheetActivityResultContract()) {
            context.runOnUiThread {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        sheetActivity.launch(config)
    }
}
