package com.moyasar.android.sdk.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.moyasar.android.sdk.data.PaymentSheetViewModel
import com.moyasar.android.sdk.data.SharedPaymentViewModelHolder
import kotlinx.parcelize.Parcelize

@SuppressLint("SetJavaScriptEnabled")
internal class PaymentAuthActivity : AppCompatActivity() {
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

            @Suppress("DEPRECATION")
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
        wv.settings.domStorageEnabled = true
        wv.settings.javaScriptEnabled = true
        wv.webViewClient = webViewClient
        wv
    }

    private val authUrl: String? by lazy {
        intent.getStringExtra(EXTRA_AUTH_URL)
    }

    private val viewModel: PaymentSheetViewModel = SharedPaymentViewModelHolder.sharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(webView)
    }

    override fun onStart() {
        super.onStart()

        if (authUrl.isNullOrBlank()) {
            viewModel.onPaymentAuthReturn(AuthResult.Failed("Missing Payment 3DS Auth URL."))

            finish()
            return
        }

        webView.loadUrl(authUrl!!)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        viewModel.onPaymentAuthReturn(AuthResult.Canceled)

        finish()
    }

    fun shouldOverrideUrlLoading(url: Uri?): Boolean {
        if (url?.host == RETURN_HOST) {
            val id = url.getQueryParameter("id") ?: ""
            val status = url.getQueryParameter("status") ?: ""
            val message = url.getQueryParameter("message") ?: ""

            viewModel.onPaymentAuthReturn(AuthResult.Completed(id, status, message))

            finish()
            return true;
        }

        return false;
    }

    fun onReceivedError(error: String?) {
        viewModel.onPaymentAuthReturn(AuthResult.Failed(error))

        finish()
    }

    sealed class AuthResult : Parcelable {
        @Parcelize
        data class Completed(val id: String, val status: String, val message: String) : AuthResult()

        @Parcelize
        data class Failed(val error: String? = null) : AuthResult()

        @Parcelize
        data object Canceled : AuthResult()
    }

    companion object {
        const val RETURN_HOST = "sdk.moyasar.com";
        const val RETURN_URL = "https://${RETURN_HOST}/payment/return"
        internal const val EXTRA_AUTH_URL = "com.moyasar.android.sdk.ui.PaymentAuthorizationActivityResultContract.auth_url"
    }
}
