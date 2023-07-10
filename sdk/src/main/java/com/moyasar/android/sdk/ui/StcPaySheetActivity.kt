package com.moyasar.android.sdk.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentResult
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.data.StcPaySheetViewModel
import com.moyasar.android.sdk.databinding.ActivityStcPaySheetBinding
import com.moyasar.android.sdk.payment.models.Payment


class StcPaySheetActivity : AppCompatActivity() {

    private val viewModel: StcPaySheetViewModel by lazy {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return StcPaySheetViewModel(config!!, resources) as T
            }
        }
        ViewModelProvider(this, factory)[StcPaySheetViewModel::class.java]
    }

    private val config: PaymentConfig? by lazy {
        @Suppress("DEPRECATION")
        intent.getParcelableExtra(StcPaySheetContract.EXTRA_ARGS)
    }

    private val binding: ActivityStcPaySheetBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_stc_pay_sheet)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel


        viewModel.status.observe(this) {
            runOnUiThread {
                when (it) {
                    is StcPaySheetViewModel.Status.PaymentOtpSecure -> {
                        Log.d("hiding stcLayout", viewModel.handleStcLayoutVisibility.toString())
                        binding.stcpayLayout.visibility = View.INVISIBLE
                        Log.d("reveal otpLayout", viewModel.handleOtpLayoutVisibility.toString())

                        binding.otpMessage.text =
                            getString(
                                R.string.OTP_message,
                                viewModel.cleanPhoneNumber
                            )
                        binding.otpLayout.visibility = View.VISIBLE
                        Log.d("viewModel status", viewModel.status.value.toString())
                    }

                    is StcPaySheetViewModel.Status.VerifyOtp -> {
                        viewModel.payment.observe(this) {

                        }
                        showOtpDialog(viewModel.payment.value!!)
                    }

                    is StcPaySheetViewModel.Status.Failure -> {
                        runOnUiThread {
                            Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                        }
                    }

                    else -> {
                    }
                }
            }
        }

        viewModel.sheetResult.observe(this) {
            runOnUiThread {
                if (it != null) {
                    setResult(
                        Activity.RESULT_OK,
                        Intent().putExtra(StcPaySheetContract.EXTRA_RESULT, it)
                    )
                    finish()
                }
            }
        }
    }

    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun showOtpDialog(payment: Payment) {

        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle(
            when (payment.status) {
                "paid" -> "Payment has been verified"
                else -> "Payment Failed"
            }
        )
        builder.setMessage(
            payment.source["message"]
        )
        builder.setCancelable(false)

        builder.setPositiveButton(
            (when (payment.status) {
                "paid" -> {
                    "OK"
                }

                else -> "Try Again"
            })
        ) { _: DialogInterface?, _: Int ->
            finish()
        }
        val alertDialog = builder.create()

        alertDialog.show()
    }

    override fun onBackPressed() {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(PaymentSheetContract.EXTRA_RESULT, PaymentResult.Canceled)
        )
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }
}
