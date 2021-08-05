package com.moyasar.android.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class PaymentAuthContract : ActivityResultContract<String, PaymentAuthActivity.AuthResult>() {
    override fun createIntent(context: Context, input: String?): Intent {
        return Intent(context, PaymentAuthActivity::class.java).apply {
            putExtra(EXTRA_AUTH_URL, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): PaymentAuthActivity.AuthResult {
        return when (resultCode) {
            Activity.RESULT_OK ->
                intent?.getParcelableExtra(EXTRA_RESULT) ?:
                PaymentAuthActivity.AuthResult.Failed("No data was returned from PaymentAuthActivity")
            else -> PaymentAuthActivity.AuthResult.Failed("Unexpected activity result code was returned from PaymentAuthActivity")
        }
    }

    companion object {
        internal const val EXTRA_AUTH_URL = "com.moyasar.android.sdk.ui.PaymentAuthorizationActivityResultContract.auth_url"
        internal const val EXTRA_RESULT = "com.moyasar.android.sdk.ui.PaymentAuthorizationActivityResultContract.result"
    }
}
