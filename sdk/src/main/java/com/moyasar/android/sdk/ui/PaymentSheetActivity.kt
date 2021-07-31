package com.moyasar.android.sdk.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.whenStarted
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.data.PaymentSheetViewModel
import com.moyasar.android.sdk.databinding.ActivityPaymentSheetBinding

class PaymentSheetActivity : AppCompatActivity() {
    private val viewModel: PaymentSheetViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PaymentSheetViewModel(config!!) as T
            }
        }
    }

    private val config: PaymentConfig? by lazy {
        intent.getParcelableExtra(PaymentSheetActivityResultContract.EXTRA_ARGS)
    }

    private val binding: ActivityPaymentSheetBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_payment_sheet)
    }

    private val authActivity = registerForActivityResult(PaymentAuthorizationActivityResultContract()) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.payment.observe(this) {
            runOnUiThread {
                Toast.makeText(this, "Got payment with ID: ${it?.id}.", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.uiStatus.observe(this) {
            runOnUiThread {
                when (it) {
                    is PaymentSheetViewModel.UiStatus.Ok -> {}
                    is PaymentSheetViewModel.UiStatus.RuntimeError -> {
                        Toast.makeText(this, "Error: ${it.e.message}.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewModel.status.observe(this) {
            when (it) {
                PaymentSheetViewModel.Status.PaymentAuth3dSecure -> {
                    val url = viewModel.payment.value?.source?.get("transaction_url")
                    Toast.makeText(this, "Transaction URL: ${url}.", Toast.LENGTH_LONG).show()
                    authActivity.launch(url)
                }
                PaymentSheetViewModel.Status.Finish -> {

                }
                else -> {}
            }
        }
    }

    fun setPaymentResult() {
        setResult(Activity.RESULT_OK, Intent().putExtra(PaymentSheetActivityResultContract.EXTRA_RESULT, "Hello from new Activity, " + config?.baseUrl))
    }
}
