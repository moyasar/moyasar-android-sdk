package com.moyasar.android.sdk.stcpay.presentation.view.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.extensions.afterTextChanged
import com.moyasar.android.sdk.core.extensions.shouldDisableButton
import com.moyasar.android.sdk.databinding.FragmentEnterOTPBinding
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.viewModel
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentStatusViewState


class EnterOTPFragment : Fragment() {

    private lateinit var binding: FragmentEnterOTPBinding
    private lateinit var parentActivity: FragmentActivity


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        parentActivity = requireActivity()
        binding = FragmentEnterOTPBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        binding.payButton.text = getString(R.string.lbl_confirm_button)
        setupListeners()
        setupObservers()
        viewModel.formValidator.stcPayOTP.value = binding.etOTPInput.text?.toString()
        binding.etOTPInput.text?.let { viewModel.stcPayOTPChanged() }
    }

    private fun setupListeners() {
        binding.payButton.setOnClickListener {
            val transactionURL = arguments?.getString(TRANSACTION_URL).orEmpty()
            viewModel.submitSTCPayOTP(
                transactionURL = transactionURL,
                otp = binding.etOTPInput.text.toString()
            )
        }
        binding.etOTPInput.afterTextChanged { text ->
            viewModel.formValidator.stcPayOTP.value = text?.toString()
            text?.let { viewModel.stcPayOTPChanged() }
        }
    }

    private fun setupObservers() {
        viewModel.isFormValid.observe(viewLifecycleOwner, ::handleFormValidationState)
        viewModel.formValidator.stcPayOTPValidator.error.observe(
            viewLifecycleOwner,
            ::showInvalidOTPErrorMsg
        )
        viewModel.status.observe(viewLifecycleOwner, ::handleOnStatusChanged)
    }

    private fun handleOnStatusChanged(status: PaymentStatusViewState?) {
        parentActivity.runOnUiThread {
            when (status) {
                is PaymentStatusViewState.SubmittingSTCPayOTP->{
                    binding.payButton.text = resources.getString(R.string.lbl_loading)
                    binding.payButton.shouldDisableButton(false)
                    binding.payButton.isEnabled = false
                    binding.etOTPInput.isEnabled = false
                }

                else -> Unit
            }
        }
    }

    private fun handleFormValidationState(isFormValid: Boolean?) {
        binding.payButton.shouldDisableButton(isFormValid ?: false)
        binding.payButton.isEnabled = isFormValid ?: false
    }

    private fun showInvalidOTPErrorMsg(errorMsg: String?) {
        if (viewModel.isFirstVisitOTP){
            viewModel.isFirstVisitOTP = false
            return
        }
        binding.etOTPInput.error = errorMsg
    }

    companion object {
        const val TRANSACTION_URL = "transactionURL"
    }

}