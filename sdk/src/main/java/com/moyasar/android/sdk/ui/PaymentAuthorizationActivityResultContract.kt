package com.moyasar.android.sdk.ui

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class PaymentAuthorizationActivityResultContract : ActivityResultContract<String, PaymentAuthorizationActivity.AuthResult>() {
    override fun createIntent(context: Context, input: String?): Intent {
        return Intent(context, PaymentAuthorizationActivity::class.java).apply {
            putExtra(EXTRA_AUTH_URL, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): PaymentAuthorizationActivity.AuthResult {
        TODO("Not yet implemented")
    }

    companion object {
        internal const val EXTRA_AUTH_URL = "com.moyasar.android.sdk.ui.PaymentAuthorizationActivityResultContract.auth_url"
        internal const val EXTRA_RESULT = "com.moyasar.android.sdk.ui.PaymentAuthorizationActivityResultContract.result"
    }
}
