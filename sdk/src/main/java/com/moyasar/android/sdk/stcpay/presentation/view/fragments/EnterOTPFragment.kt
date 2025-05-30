package com.moyasar.android.sdk.stcpay.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.extensions.afterTextChanged
import com.moyasar.android.sdk.core.extensions.gone
import com.moyasar.android.sdk.core.extensions.shouldDisableButton
import com.moyasar.android.sdk.core.extensions.show
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.viewModel
import com.moyasar.android.sdk.databinding.FragmentEnterOTPBinding
import com.moyasar.android.sdk.stcpay.presentation.model.STCPayViewState


class EnterOTPFragment : Fragment() {

    private lateinit var binding: FragmentEnterOTPBinding
    private lateinit var parentActivity: FragmentActivity


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        parentActivity = requireActivity()
        binding = FragmentEnterOTPBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        binding.payButton.text = getString(R.string.lbl_confirm_button)
        binding.progressBar.gone()
        setupListeners()
        setupObservers()
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
            text?.let { viewModel.stcPayOTPChanged(it) }
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
                    binding.payButton.text = ""
                    binding.progressBar.show()
                    binding.payButton.shouldDisableButton(
                        false,
                        bgEnabledDrawableRes = R.drawable.moyasar_bt_purple_enabled_background,
                        bgDisabledDrawableRes = R.drawable.moyasar_bt_purple_disabled_background,
                        bgEnabledColorRes = R.color.light_purple_button_enabled,
                        bgDisabledColorRes = R.color.light_purple_button_disabled
                    )
                    binding.payButton.isEnabled = false
                    binding.etOTPInput.isEnabled = false
                }

                else -> Unit
            }
        }
    }

    private fun handleFormValidationState(isFormValid: Boolean?) {
        binding.payButton.shouldDisableButton(
            isFormValid ?: false,
            bgEnabledDrawableRes = R.drawable.moyasar_bt_purple_enabled_background,
            bgDisabledDrawableRes = R.drawable.moyasar_bt_purple_disabled_background,
            bgEnabledColorRes = R.color.light_purple_button_enabled,
            bgDisabledColorRes = R.color.light_purple_button_disabled
        )
        binding.payButton.isEnabled = isFormValid ?: false
    }

    private fun showInvalidOTPErrorMsg(errorMsg: String?) {
        binding.etOTPInput.error = errorMsg?.takeIf { it.isEmpty().not() }
    }

    companion object {
        const val TRANSACTION_URL = "transactionURL"
    }

}