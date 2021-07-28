package com.moyasar.android.sdk.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import kotlinx.parcelize.Parcelize

class PaymentAuthorizationActivity : AppCompatActivity() {
    private val webViewClient by lazy {
        val activity = this

        object : WebViewClient() {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return activity.shouldOverrideUrlLoading(request?.url)
            }

            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return activity.shouldOverrideUrlLoading(if (url != null) Uri.parse(url) else null)
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                activity.onReceivedError(error?.description?.toString())
            }

            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                activity.onReceivedError(description)
            }
        }
    }

    private val webView by lazy {
        val wv = WebView(this)
        wv.webViewClient = webViewClient
        wv
    }

    private val authUrl: String? by lazy {
        intent.getStringExtra(PaymentAuthorizationActivityResultContract.EXTRA_AUTH_URL)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(webView)
    }

    override fun onStart() {
        super.onStart()

        if (authUrl.isNullOrBlank()) {
            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(
                    PaymentSheetActivityResultContract.EXTRA_RESULT,
                    AuthResult.Failed("Missing Payment 3DS Auth URL.")
                )
            )
            finish()
            return
        }

        webView.loadUrl(authUrl!!)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(PaymentSheetActivityResultContract.EXTRA_RESULT, AuthResult.Canceled)
        )

        finish()
    }

    fun shouldOverrideUrlLoading(url: Uri?): Boolean {
        if (url?.host == RETURN_HOST) {
            val id = url.getQueryParameter("id") ?: ""
            val status = url.getQueryParameter("status") ?: ""
            val message = url.getQueryParameter("message") ?: ""

            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(
                    PaymentSheetActivityResultContract.EXTRA_RESULT,
                    AuthResult.Completed(id, status, message)
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
            Intent().putExtra(PaymentSheetActivityResultContract.EXTRA_RESULT, AuthResult.Failed(error))
        )
        finish()
    }

    sealed class AuthResult : Parcelable {
        @Parcelize
        data class Completed(val id: String, val status: String, val message: String) : AuthResult()

        @Parcelize
        data class Failed(val error: String? = null) : AuthResult()

        @Parcelize
        object Canceled : AuthResult()

        @Parcelize
        object NoResult : AuthResult()
    }

    companion object {
        val RETURN_HOST = "sdk.moyasar.com";
        val RETURN_URL = "https://${RETURN_HOST}/payment/return"
    }
}
