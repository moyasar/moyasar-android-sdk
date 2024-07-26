package com.moyasar.android.sdk.presentation.view.fragments

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
import com.moyasar.android.sdk.core.extensions.gone
import com.moyasar.android.sdk.databinding.FragmentPaymentAuthBinding
import com.moyasar.android.sdk.presentation.model.AuthResultViewState
import com.moyasar.android.sdk.presentation.viewmodel.PaymentSheetViewModel
import com.moyasar.android.sdk.presentation.viewmodel.SharedPaymentViewModelHolder

@SuppressLint("ValidFragment")
internal class PaymentAuthFragment : Fragment() {

  private val viewModel: PaymentSheetViewModel = SharedPaymentViewModelHolder.sharedViewModel
  private lateinit var binding: FragmentPaymentAuthBinding

  private val authUrl: String? by lazy {
    viewModel.payment.value?.getCardTransactionUrl()
  }

  private val webViewClient by lazy {

    object : WebViewClient() {
      @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
      override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?,
      ): Boolean {
        return shouldOverrideUrlLoading(request?.url)
      }

      override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return shouldOverrideUrlLoading(if (url != null) Uri.parse(url) else null)
      }

      override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        binding.circularProgressIndicator.gone()
      }

      @RequiresApi(Build.VERSION_CODES.M)
      override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
      ) {
        onReceivedError(error?.description?.toString())
      }

      override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?,
      ) {
        onReceivedError(description)
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    super.onCreateView(inflater, container, savedInstanceState)

    binding = FragmentPaymentAuthBinding.inflate(inflater, container, false)
    binding.webView.settings.domStorageEnabled = true
    binding.webView.settings.javaScriptEnabled = true
    binding.webView.webViewClient = webViewClient

    return binding.root
  }

  override fun onStart() {
    super.onStart()

    val url = authUrl

    if (url.isNullOrBlank()) {
      viewModel.onPaymentAuthReturn(AuthResultViewState.Failed(ERROR_MESSAGE_FAILED_3DS_CHECK))
      return
    }

    binding.webView.loadUrl(url)
  }

  private fun shouldOverrideUrlLoading(url: Uri?): Boolean {
    if (url?.host == RETURN_HOST) {
      val id = url.getQueryParameter(URI_QUERY_PARAM_ID_KEY).orEmpty()
      val status = url.getQueryParameter(URI_QUERY_PARAM_STATUS_KEY).orEmpty()
      val message = url.getQueryParameter(URI_QUERY_PARAM_MESSAGE_KEY).orEmpty()

      viewModel.onPaymentAuthReturn(AuthResultViewState.Completed(id, status, message))

      return true;
    }

    return false;
  }

  private fun onReceivedError(error: String?) {
    viewModel.onPaymentAuthReturn(AuthResultViewState.Failed(error))
  }

  companion object {
    const val RETURN_HOST = "sdk.moyasar.com";
    const val RETURN_URL = "https://$RETURN_HOST/payment/return"
    private const val URI_QUERY_PARAM_ID_KEY = "id"
    private const val URI_QUERY_PARAM_STATUS_KEY = "status"
    private const val URI_QUERY_PARAM_MESSAGE_KEY = "message"
    private const val ERROR_MESSAGE_FAILED_3DS_CHECK = "Missing Payment 3DS Auth URL."
  }
}