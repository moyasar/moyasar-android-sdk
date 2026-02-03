package com.moyasar.android.sdkdriver.customui.stcpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.viewModel
import com.moyasar.android.sdk.stcpay.presentation.model.STCPayViewState
import com.moyasar.android.sdkdriver.databinding.FragmentEnterOTPCustomUIBinding


class EnterOTPCustomUIFragment : Fragment() {

    private lateinit var binding: FragmentEnterOTPCustomUIBinding
    private lateinit var parentActivity: FragmentActivity


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        parentActivity = requireActivity()
        binding = FragmentEnterOTPCustomUIBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        binding.progressBar.isVisible = false
        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.payButton.setOnClickListener {
            val transactionURL = arguments?.getString(TRANSACTION_URL).orEmpty()
            viewModel.submitSTCPayOTP(
                transactionURL = transactionURL,
                otp = binding.otpEt.text.toString()
            )
        }
        binding.otpEt.doAfterTextChanged { text ->
            viewModel.stcPayOTPChanged(text)
        }
    }

    private fun setupObservers() {
        viewModel.stcPayStatus.observe(viewLifecycleOwner, ::handleOnStatusChanged)
        viewModel.inputFieldsValidatorLiveData.observe(viewLifecycleOwner) { inputFieldUIModel ->
            showInvalidOTPErrorMsg(inputFieldUIModel?.stcPayUIModel?.otpErrorMsg)
            handleFormValidationState(viewModel.inputFieldsValidatorLiveData.value?.stcPayUIModel?.isOTPValid)
        }
    }

    private fun handleOnStatusChanged(status: STCPayViewState?) {
        parentActivity.runOnUiThread {
            when (status) {
                is STCPayViewState.SubmittingSTCPayOTP -> {
                    binding.progressBar.isVisible = true
                    binding.payButton.isEnabled = false
                    binding.otpEt.isEnabled = false
                }

                else -> Unit
            }
        }
    }

    private fun handleFormValidationState(isFormValid: Boolean?) {
        binding.payButton.isEnabled = isFormValid ?: false
    }

    private fun showInvalidOTPErrorMsg(errorMsg: String?) {
        binding.otpInputLayout.error = errorMsg?.takeIf { it.isEmpty().not() }
    }

    companion object {
        const val TRANSACTION_URL = "transactionURL"
    }

}