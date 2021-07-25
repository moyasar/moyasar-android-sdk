package com.moyasar.android.sdk.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

class PaymentAuthorizationActivity : AppCompatActivity() {
    private val webViewClient by lazy {
        WebViewClient()
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
    }

    override fun onStart() {
        super.onStart()

        if (authUrl.isNullOrBlank()) {
            return
        }

        webView.loadUrl(authUrl!!)
    }

    sealed class AuthResult {
        data class Success(val id: String, val status: String, val message: String) : AuthResult()
        object Failure : AuthResult()
        object Canceled : AuthResult()
    }
}
