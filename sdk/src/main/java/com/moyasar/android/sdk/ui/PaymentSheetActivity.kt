package com.moyasar.android.sdk.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.TransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentResult
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.data.PaymentSheetViewModel
import com.moyasar.android.sdk.databinding.ActivityPaymentSheetBinding

class PaymentSheetActivity : AppCompatActivity() {
    private val viewModel: PaymentSheetViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PaymentSheetViewModel(config!!, resources) as T
            }
        }
    }

    private val config: PaymentConfig? by lazy {
        intent.getParcelableExtra(PaymentSheetContract.EXTRA_ARGS)
    }

    private val binding: ActivityPaymentSheetBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_payment_sheet)
    }

    private val authActivity = registerForActivityResult(PaymentAuthContract()) {
        viewModel.onPaymentAuthReturn(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.status.observe(this) {
            runOnUiThread {
                when (it) {
                    is PaymentSheetViewModel.Status.PaymentAuth3dSecure -> {
                        authActivity.launch(it.url)
                    }
                    is PaymentSheetViewModel.Status.Failure -> {
                        runOnUiThread {
                            Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                    else -> {}
                }
            }
        }

        viewModel.sheetResult.observe(this) {
            runOnUiThread {
                if (it != null) {
                    setResult(Activity.RESULT_OK, Intent().putExtra(PaymentSheetContract.EXTRA_RESULT, it))
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().putExtra(PaymentSheetContract.EXTRA_RESULT, PaymentResult.Canceled))
        super.onBackPressed()
    }
}
