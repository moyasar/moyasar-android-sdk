package com.moyasar.android.sdk.ui

import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentResult
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.data.PaymentSheetViewModel
import com.moyasar.android.sdk.data.SharedPaymentViewModelHolder
import com.moyasar.android.sdk.databinding.FragmentPaymentBinding

class PaymentFragment : Fragment() {

    private val viewModel: PaymentSheetViewModel = SharedPaymentViewModelHolder.sharedViewModel

    private lateinit var parentActivity: FragmentActivity

    companion object {
        fun newInstance(application: Application, config: PaymentConfig, callback: (PaymentResult) -> Unit): PaymentFragment {
            SharedPaymentViewModelHolder.sharedViewModel = PaymentSheetViewModel(application, config, callback)
            return PaymentFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        parentActivity = requireActivity()

        val binding = FragmentPaymentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.status.observe(viewLifecycleOwner) {
            parentActivity.runOnUiThread {
                when (it) {
                    is PaymentSheetViewModel.Status.PaymentAuth3dSecure -> {
                        childFragmentManager.beginTransaction().apply {
                            replace(R.id.payment_fragment_container, PaymentAuthFragment())
                            commit()
                        }
                    }

                    else -> {}
                }
            }
        }

        viewModel.sheetResult.observe(viewLifecycleOwner) {
            parentActivity.runOnUiThread {
                if (it != null) {
                    childFragmentManager.beginTransaction().remove(this).commit()
                }
            }
        }

        return binding.root
    }
}