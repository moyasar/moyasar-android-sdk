package com.moyasar.android.sdk.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moyasar.android.sdk.data.PaymentSheetViewModel
import com.moyasar.android.sdk.data.SharedPaymentViewModelHolder
import com.moyasar.android.sdk.databinding.FragmentPaymentBinding

internal class PaymentFragment: Fragment() {

    private val viewModel: PaymentSheetViewModel = SharedPaymentViewModelHolder.sharedViewModel

    private lateinit var parentActivity: FragmentActivity

    companion object {
        fun newInstance(): PaymentFragment {
            return PaymentFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val binding = FragmentPaymentBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.status.observe(viewLifecycleOwner) {
            parentActivity.runOnUiThread {
                when (it) {
                    is PaymentSheetViewModel.Status.PaymentAuth3dSecure -> {
                        startActivity(
                            Intent(
                                parentActivity,
                                PaymentAuthActivity::class.java
                            ).apply {
                                putExtra(PaymentAuthActivity.EXTRA_AUTH_URL, it.url)
                            })
                    }

                    else -> {}
                }
            }
        }

        viewModel.sheetResult.observe(viewLifecycleOwner) {
            parentActivity.runOnUiThread {
                if (it != null) {
                    parentActivity.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
                }
            }
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentActivity = requireActivity()
    }
}