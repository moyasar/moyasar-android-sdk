package com.moyasar.android.sdk.ui

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentResult

class StcPaySheetContract : ActivityResultContract<PaymentConfig, PaymentResult>() {

    override fun createIntent(context: Context, input: PaymentConfig): Intent {
        return Intent(context, StcPaySheetActivity::class.java).apply {
            putExtra(EXTRA_ARGS, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): PaymentResult {
        return intent?.getParcelableExtra(EXTRA_RESULT)
            ?: PaymentResult.Failed(Exception("No data was returned from StcPaySheetActivity"))
    }

    companion object {
        internal const val EXTRA_ARGS =
            "com.moyasar.android.sdk.ui.StcPaySheetActivityResultContract.extra_args"
        internal const val EXTRA_RESULT =
            "com.moyasar.android.sdk.ui.StcPaySheetActivityResultContract.extra_result"
    }
}
