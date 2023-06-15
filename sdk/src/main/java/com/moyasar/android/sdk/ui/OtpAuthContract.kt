package com.moyasar.android.sdk.ui

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class OtpAuthContract: ActivityResultContract<String, PaymentAuthActivity.AuthResult>() {
    override fun createIntent(context: Context, input: String): Intent {
        return Intent(context, OtpAuthActivity::class.java).apply {
            putExtra(EXTRA_AUTH_URL, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): PaymentAuthActivity.AuthResult {
        return intent?.getParcelableExtra(EXTRA_RESULT) ?:
        PaymentAuthActivity.AuthResult.Failed("No data was returned from PaymentAuthActivity")
    }

    companion object {
        internal const val EXTRA_AUTH_URL = "com.moyasar.android.sdk.ui.OtpAuthorizationActivityResultContract.auth_url"
        internal const val EXTRA_RESULT = "com.moyasar.android.sdk.ui.OtpAuthorizationActivityResultContract.result"
    }
}