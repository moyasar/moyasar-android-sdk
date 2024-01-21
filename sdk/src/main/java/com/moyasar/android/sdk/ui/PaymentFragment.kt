package com.moyasar.android.sdk.ui

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentSheetResultCallback
import com.moyasar.android.sdk.data.PaymentSheetViewModel
import com.moyasar.android.sdk.databinding.FragmentPaymentBinding

internal class PaymentFragment: Fragment() {

    private val viewModel: PaymentSheetViewModel by lazy {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PaymentSheetViewModel(config, resources) as T
            }
        }

        ViewModelProvider(this, factory).get(PaymentSheetViewModel::class.java)
    }

    private val config: PaymentConfig by lazy {
        @Suppress("DEPRECATION")
        arguments?.getParcelable<PaymentConfig>(EXTRA_ARGS) as PaymentConfig
    }

    private val paymentSheetResultCallback: PaymentSheetResultCallback by lazy {
        @Suppress("DEPRECATION")
        arguments?.getSerializable(EXTRA_PAYMENT_SHEET) as PaymentSheetResultCallback
    }

    private lateinit var parentActivity: FragmentActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentPaymentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = viewModel
        }

        viewModel.status.observe(this) {
            // TODO: Check if runOnUiThread need to be here or inside cases
            parentActivity.runOnUiThread {
                when (it) {
                    is PaymentSheetViewModel.Status.PaymentAuth3dSecure -> {
                        startActivity(
                            Intent(
                                parentActivity,
                                PaymentAuthActivity::class.java
                            ).apply {
                                putExtra(PaymentAuthActivity.EXTRA_AUTH_URL, it.url)
                                putExtra(PaymentAuthActivity.EXTRA_AUTH_VIEW_MODEL, viewModel)
                            })
                    }

                    is PaymentSheetViewModel.Status.Failure -> {
                        parentActivity.runOnUiThread {
                            Toast.makeText(parentActivity, it.toString(), Toast.LENGTH_LONG).show()
                        }
                    }

                    else -> {}
                }
            }
        }

        viewModel.sheetResult.observe(this) {
            parentActivity.runOnUiThread {
                if (it != null) {
                    paymentSheetResultCallback.onResult(it)
                }
            }
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentActivity = requireActivity()
    }

    companion object {
        internal const val EXTRA_ARGS =
            "com.moyasar.android.sdk.ui.PaymentFragment.extra_args"
        internal const val EXTRA_PAYMENT_SHEET =
            "com.moyasar.android.sdk.ui.PaymentFragment.payment_sheet"
    }

}