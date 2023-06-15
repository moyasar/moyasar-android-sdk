package com.moyasar.android.sdk.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.moyasar.android.sdk.R

class OtpAuthActivity : AppCompatActivity() {

    private val authUrl: String? by lazy {
        intent.getStringExtra(OtpAuthContract.EXTRA_AUTH_URL)
    }

    override fun onStart() {
        super.onStart()

        if (authUrl.isNullOrBlank()) {
            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(
                    OtpAuthContract.EXTRA_RESULT,
                    PaymentAuthActivity.AuthResult.Failed("Missing OTP Auth URL.")
                )
            )
            finish()
            return
        }

//        webView.loadUrl(authUrl!!)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_auth)
    }
    override fun onBackPressed() {
        super.onBackPressed()

        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(OtpAuthContract.EXTRA_RESULT, PaymentAuthActivity.AuthResult.Canceled)
        )

        finish()
    }
    fun shouldOverrideUrlLoading(url: Uri?): Boolean {
        if (url?.host == PaymentAuthActivity.RETURN_HOST) {
            val id = url.getQueryParameter("id") ?: ""
            val status = url.getQueryParameter("status") ?: ""
            val message = url.getQueryParameter("message") ?: ""

            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(
                    PaymentAuthContract.EXTRA_RESULT,
                    PaymentAuthActivity.AuthResult.Completed(id, status, message)
                )
            )

            finish()
            return true;
        }

        return false;
    }
    fun onReceivedError(error: String?) {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(OtpAuthContract.EXTRA_RESULT, PaymentAuthActivity.AuthResult.Failed(error))
        )
        finish()
    }


    fun verifyOTP(view: View) {}
    companion object {
        val RETURN_HOST = "sdk.moyasar.com";
        val RETURN_URL = "https://${RETURN_HOST}/payments"
    }
}