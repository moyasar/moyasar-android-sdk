package com.moyasar.android.sdkdriver.customui.stcpay


import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.moyasar.android.sdk.R as sdkR
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.core.exceptions.InvalidConfigException
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.viewModel
import com.moyasar.android.sdk.stcpay.presentation.model.STCPayViewState
import com.moyasar.android.sdk.stcpay.presentation.view.fragments.EnterOTPFragment
import com.moyasar.android.sdkdriver.R
import com.moyasar.android.sdkdriver.databinding.FragmentEnterMobileNumberCustomUIBinding

class EnterMobileNumberCustomUIFragment : Fragment() {

    private lateinit var binding: FragmentEnterMobileNumberCustomUIBinding
    private lateinit var parentActivity: FragmentActivity

    companion object {
        fun newInstance(
            application: Application,
            paymentRequest: PaymentRequest,
            callback: (PaymentResult) -> Unit,
        ): EnterMobileNumberCustomUIFragment {
            val configError = paymentRequest.validate()
            if (configError.any()) {
                throw InvalidConfigException(configError)
            }
            MoyasarAppContainer.initialize(application, paymentRequest, callback)
            return EnterMobileNumberCustomUIFragment()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        parentActivity = requireActivity()
        binding = FragmentEnterMobileNumberCustomUIBinding.inflate(inflater, container, false)
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
        binding.mobileInputLayout.error = errorMsg.takeIf { it?.isNotEmpty() == true}
    }

    private fun handleFormValidationState(isFormValid: Boolean?) {
        binding.payButton.isEnabled = isFormValid ?: false
    }

    private fun handleOnStatusChanged(status: STCPayViewState?) {
        parentActivity.runOnUiThread {
            when (status) {
                is STCPayViewState.SubmittingSTCPayMobileNumber -> {
                    binding.progressBar.isVisible = true
                    binding.payButton.isEnabled = false
                    binding.mobileEt.isEnabled = false
                }

                is STCPayViewState.STCPayOTPAuth -> {
                    binding.llContainer.isVisible = false
                    binding.progressBar.isVisible = false
                    val fragment = EnterOTPCustomUIFragment()
                    val args = Bundle()
                    args.putString(EnterOTPFragment.TRANSACTION_URL, status.url)
                    fragment.arguments = args
                    childFragmentManager.beginTransaction().apply {
                        replace(R.id.enter_mobile_fragment_custom_ui_container, fragment)
                        commit()
                    }
                }

                else -> Unit
            }
        }
    }

    private fun initView() {
        binding.progressBar.isVisible = false
        binding.payButton.setOnClickListener {
            viewModel.submitSTC()
        }

    }

    private fun FragmentEnterMobileNumberCustomUIBinding.setupListeners() {
        mobileEt.doAfterTextChanged { text ->
                viewModel.mobileNumberChanged(text) { s ->
                    mobileEt.setText(s)
                    // Move cursor to the end of the text
                    mobileEt.setSelection(s.length)
                }
        }
    }

}