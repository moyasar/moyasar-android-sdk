package com.moyasar.android.sdk.stcpay.presentation.view.fragments

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.core.exceptions.InvalidConfigException
import com.moyasar.android.sdk.core.extensions.afterTextChanged
import com.moyasar.android.sdk.core.extensions.gone
import com.moyasar.android.sdk.core.extensions.shouldDisableButton
import com.moyasar.android.sdk.core.extensions.show
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.viewModel
import com.moyasar.android.sdk.databinding.FragmentEnterMobileNumberBinding
import com.moyasar.android.sdk.stcpay.presentation.model.STCPayViewState

class EnterMobileNumberFragment : Fragment() {

    private lateinit var binding: FragmentEnterMobileNumberBinding
    private lateinit var parentActivity: FragmentActivity

    companion object {
        fun newInstance(
            application: Application,
            paymentRequest: PaymentRequest,
            callback: (PaymentResult) -> Unit,
        ): EnterMobileNumberFragment {
            val configError = paymentRequest.validate()
            if (configError.any()) {
                throw InvalidConfigException(configError)
            }
            MoyasarAppContainer.initialize(application, paymentRequest, callback)
            return EnterMobileNumberFragment()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        parentActivity = requireActivity()
        binding = FragmentEnterMobileNumberBinding.inflate(inflater, container, false)
        initView()
        setupObservers()
        binding.setupListeners()
        return binding.root
    }

    private fun setupObservers() {
        viewModel.stcPayStatus.observe(
            viewLifecycleOwner,
            ::handleOnStatusChanged
        )
        viewModel.inputFieldsValidatorLiveData.observe(viewLifecycleOwner) { inputFieldUIModel ->
            showInvalidPhoneErrorMsg(inputFieldUIModel?.stcPayUIModel?.mobileNumberErrorMsg)
            handleFormValidationState(viewModel.inputFieldsValidatorLiveData.value?.stcPayUIModel?.isMobileValid)
        }
    }

    private fun showInvalidPhoneErrorMsg(errorMsg: String?) {
        binding.tvMobileNumber.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.red)
        )
        binding.tvMobileNumber.text = errorMsg
    }

    private fun handleFormValidationState(isFormValid: Boolean?) {
        binding.payButton.isEnabled = isFormValid ?: false
        binding.payButton.shouldDisableButton(
            isFormValid ?: false,
            bgEnabledDrawableRes = R.drawable.moyasar_bt_purple_enabled_background,
            bgDisabledDrawableRes = R.drawable.moyasar_bt_purple_disabled_background,
            bgEnabledColorRes = R.color.light_purple_button_enabled,
            bgDisabledColorRes = R.color.light_purple_button_disabled
        )
    }

    private fun handleOnStatusChanged(status: STCPayViewState?) {
        parentActivity.runOnUiThread {
            when (status) {
                is STCPayViewState.SubmittingSTCPayMobileNumber -> {
                    binding.payButton.setButtonType(MoyasarAppContainer.paymentRequest.buttonType)
                    binding.progressBar.show()
                    binding.payButton.shouldDisableButton(
                        false,
                        bgEnabledDrawableRes = R.drawable.moyasar_bt_purple_enabled_background,
                        bgDisabledDrawableRes = R.drawable.moyasar_bt_purple_disabled_background,
                        bgEnabledColorRes = R.color.light_purple_button_enabled,
                        bgDisabledColorRes = R.color.light_purple_button_disabled
                    )
                    binding.payButton.isEnabled = false
                    binding.etMobileNumberInput.isEnabled = false
                }

                is STCPayViewState.STCPayOTPAuth -> {
                    binding.enterMobileFragmentContent.gone()
                    val fragment = EnterOTPFragment()
                    val args = Bundle()
                    args.putString(EnterOTPFragment.TRANSACTION_URL, status.url)
                    fragment.arguments = args
                    childFragmentManager.beginTransaction().apply {
                        replace(R.id.enter_mobile_fragment_container, fragment)
                        commit()
                    }
                }

                else -> Unit
            }
        }
    }

    private fun initView() {
        binding.progressBar.gone()
        binding.payButton.setButtonType(MoyasarAppContainer.paymentRequest.buttonType)
        binding.payButton.setOnClickListener {
            viewModel.submitSTC()
        }

    }

    private fun FragmentEnterMobileNumberBinding.setupListeners() {
        etMobileNumberInput.afterTextChanged { text ->
            text?.let {
                viewModel.mobileNumberChanged(it) { s ->
                    etMobileNumberInput.setText(s)
                    // Move cursor to the end of the text
                    etMobileNumberInput.setSelection(s.length)
                }
            }
        }
    }

}