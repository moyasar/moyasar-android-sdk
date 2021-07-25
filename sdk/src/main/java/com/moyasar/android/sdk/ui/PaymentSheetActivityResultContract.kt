package com.moyasar.android.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.moyasar.android.sdk.PaymentConfig

class PaymentSheetActivityResultContract : ActivityResultContract<PaymentConfig, String>() {
    override fun createIntent(context: Context, input: PaymentConfig?): Intent {
        return Intent(context, PaymentSheetActivity::class.java).apply {
            putExtra(EXTRA_ARGS, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String {
        return when (resultCode) {
            Activity.RESULT_OK -> intent?.getStringExtra(EXTRA_RESULT) ?: "Empty result"
            else -> "No result"
        }
    }

    companion object {
        internal const val EXTRA_ARGS = "com.moyasar.android.sdk.ui.PaymentSheetActivityResultContract.extra_args"
        internal const val EXTRA_RESULT = "com.moyasar.android.sdk.ui.PaymentSheetActivityResultContract.extra_result"
    }
}
