package com.moyasar.android.sdk.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.data.PaymentSheetViewModel
import com.moyasar.android.sdk.data.SharedPaymentViewModelHolder
import com.moyasar.android.sdk.databinding.FragmentPaymentAuthBinding

@SuppressLint("SetJavaScriptEnabled")
internal class PaymentAuthFragment : Fragment() {

    private val viewModel: PaymentSheetViewModel = SharedPaymentViewModelHolder.sharedViewModel

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    private val authUrl: String? by lazy {
        viewModel.payment.value?.getCardTransactionUrl()
    }

    private val webViewClient by lazy {

        object : WebViewClient() {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return shouldOverrideUrlLoading(request?.url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return shouldOverrideUrlLoading(if (url != null) Uri.parse(url) else null)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                progressBar.visibility = View.GONE
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                onReceivedError(error?.description?.toString())
            }

            @Suppress("DEPRECATION")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                onReceivedError(description)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val binding = FragmentPaymentAuthBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        webView = binding.webView
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = webViewClient

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.circularProgressIndicator)
    }

    override fun onStart() {
        super.onStart()

        val url = authUrl

        if (url.isNullOrBlank()) {
            viewModel.onPaymentAuthReturn(AuthResult.Failed("Missing Payment 3DS Auth URL."))
            return
        }

        webView.loadUrl(url)
    }

    private fun shouldOverrideUrlLoading(url: Uri?): Boolean {
        if (url?.host == RETURN_HOST) {
            val id = url.getQueryParameter("id") ?: ""
            val status = url.getQueryParameter("status") ?: ""
            val message = url.getQueryParameter("message") ?: ""

            viewModel.onPaymentAuthReturn(AuthResult.Completed(id, status, message))

            return true;
        }

        return false;
    }

    private fun onReceivedError(error: String?) {
        viewModel.onPaymentAuthReturn(AuthResult.Failed(error))
    }

    sealed class AuthResult {
        data class Completed(val id: String, val status: String, val message: String) : AuthResult()

        data class Failed(val error: String? = null) : AuthResult()

        data object Canceled : AuthResult()
    }

    companion object {
        const val RETURN_HOST = "sdk.moyasar.com";
        const val RETURN_URL = "https://${RETURN_HOST}/payment/return"
    }
}