package com.moyasar.android.sdk.stcpay.presentation.view.fragments

import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.exceptions.InvalidConfigException
import com.moyasar.android.sdk.core.extensions.afterTextChanged
import com.moyasar.android.sdk.core.extensions.gone
import com.moyasar.android.sdk.core.extensions.shouldDisableButton
import com.moyasar.android.sdk.databinding.FragmentEnterMobileNumberBinding
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.core.extensions.show
import com.moyasar.android.sdk.core.util.MoyasarLogger
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentConfig
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentStatusViewState

class EnterMobileNumberFragment : Fragment() {

    private lateinit var binding: FragmentEnterMobileNumberBinding
    private lateinit var parentActivity: FragmentActivity

    companion object {
        fun newInstance(
            application: Application,
            config: PaymentConfig,
            callback: (PaymentResult) -> Unit,
        ): EnterMobileNumberFragment {
            val configError = config.validate()
            if (configError.any()) {
                throw InvalidConfigException(configError)
            }
            MoyasarAppContainer.initialize(application, config, callback)
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
        MoyasarAppContainer.viewModel.isFormValid.observe(viewLifecycleOwner, ::handleFormValidationState)
        MoyasarAppContainer.viewModel.status.observe(viewLifecycleOwner, ::handleOnStatusChanged)
        MoyasarAppContainer.viewModel.formValidator.mobileNumberValidator.error.observe(viewLifecycleOwner, ::showInvalidPhoneErrorMsg)
    }
    private fun showInvalidPhoneErrorMsg(errorMsg: String?) {
        if (MoyasarAppContainer.viewModel.isFirstVisitEnterMobileNumber){
            MoyasarAppContainer.viewModel.isFirstVisitEnterMobileNumber = false
            return
        }
        binding.tvMobileNumber.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.red)
        )
        binding.tvMobileNumber.text = errorMsg
    }
    private fun handleFormValidationState(isFormValid: Boolean?) {
        binding.payButton.isEnabled = isFormValid?: false
        binding.payButton.shouldDisableButton(isFormValid ?: false)
    }

    private fun handleOnStatusChanged(status: PaymentStatusViewState?) {
        parentActivity.runOnUiThread {
            when (status) {
                is PaymentStatusViewState.SubmittingSTCPayMobileNumber ->{
                    binding.payButton.text = ""
                    binding.progressBar.show()
                    binding.payButton.shouldDisableButton(false)
                    binding.payButton.isEnabled = false
                    binding.etMobileNumberInput.isEnabled = false
                }

                is PaymentStatusViewState.STCPayOTPAuth -> {
                    MoyasarLogger.log("STCPay TransactionURL", status.url)
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
        binding.payButton.text = getString(R.string.payBtnLabel).plus(' ').plus(MoyasarAppContainer.viewModel.amountLabel)
        binding.payButton.setOnClickListener {
            MoyasarAppContainer.viewModel.submitSTC()
        }

    }
    private fun FragmentEnterMobileNumberBinding.setupListeners() {
        etMobileNumberInput.afterTextChanged { text ->
            MoyasarAppContainer.viewModel.formValidator.mobileNumber.value = text?.toString()
            text?.let { MoyasarAppContainer.viewModel.mobileNumberChanged(it) }
        }
    }

}