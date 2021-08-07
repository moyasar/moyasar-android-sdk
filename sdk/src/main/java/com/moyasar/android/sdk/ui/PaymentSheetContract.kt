package com.moyasar.android.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentResult

class PaymentSheetContract : ActivityResultContract<PaymentConfig, PaymentResult>() {
    override fun createIntent(context: Context, input: PaymentConfig?): Intent {
        return Intent(context, PaymentSheetActivity::class.java).apply {
            putExtra(EXTRA_ARGS, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): PaymentResult {
        return when (resultCode) {
            Activity.RESULT_OK ->
                intent?.getParcelableExtra(EXTRA_RESULT) ?:
                PaymentResult.Failed("No data was returned from PaymentSheetActivity")
            else -> PaymentResult.Failed("Unexpected activity result code was returned from PaymentSheetActivity")
        }
    }

    companion object {
        internal const val EXTRA_ARGS = "com.moyasar.android.sdk.ui.PaymentSheetActivityResultContract.extra_args"
        internal const val EXTRA_RESULT = "com.moyasar.android.sdk.ui.PaymentSheetActivityResultContract.extra_result"
    }
}
