package com.moyasar.android.sdkdriver.customui.creditcard

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.moyasar.android.sdk.R as sdkR
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.core.exceptions.InvalidConfigException
import com.moyasar.android.sdk.creditcard.data.models.CreditCardNetwork
import com.moyasar.android.sdk.creditcard.data.models.getNetwork
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.viewModel
import com.moyasar.android.sdk.creditcard.presentation.model.FieldValidation
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentStatusViewState
import com.moyasar.android.sdk.creditcard.presentation.view.fragments.PaymentAuthFragment
import com.moyasar.android.sdkdriver.R
import com.moyasar.android.sdkdriver.databinding.FragmentCustomUIPaymentBinding


class CustomUIPaymentFragment : Fragment() {
    private lateinit var parentActivity: FragmentActivity
    private lateinit var binding: FragmentCustomUIPaymentBinding

    companion object {
        fun newInstance(
            application: Application,
            paymentRequest: PaymentRequest,
            callback: (PaymentResult) -> Unit,
        ): CustomUIPaymentFragment {
            val configError = paymentRequest.validate()
            if (configError.any()) {
                throw InvalidConfigException(configError)
            }
            MoyasarAppContainer.initialize(application, paymentRequest, callback)
            return CustomUIPaymentFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        parentActivity = requireActivity()
        binding = FragmentCustomUIPaymentBinding.inflate(inflater, container, false)
        setupObservers()
        binding.setupListeners()
        return binding.root
    }

    private fun setupObservers() {
        viewModel.creditCardStatus.observe(viewLifecycleOwner, ::handleOnStatusChanged)
        viewModel.inputFieldsValidatorLiveData.observe(viewLifecycleOwner) { inputFieldUIModel ->
            showInvalidNameErrorMsg(inputFieldUIModel.errorMessage?.nameErrorMsg)
            showInvalidCardNumberErrorMsg(inputFieldUIModel.errorMessage?.numberErrorMsg)
            showInvalidExpiryErrorMsg(inputFieldUIModel.errorMessage?.expiryDateErrorMsg)
            showInvalidCVVErrorMsg(inputFieldUIModel.errorMessage?.cvcErrorMsg)
            handleFormValidationState(inputFieldUIModel.isFormValid)
            handleShowAllowedCardTypesIcons(inputFieldUIModel.cardNumber)
        }
    }

    private fun handleShowAllowedCardTypesIcons(text: String?) {
        // showAllowedCreditCardsInEditText(number.orEmpty(), paymentRequest.allowedNetworks, binding)
        if (text.orEmpty().isEmpty()) {
            MoyasarAppContainer.allowedNetworks.forEach {
                when (it) {
                    CreditCardNetwork.Visa -> binding.imgVisa.isVisible = true
                    CreditCardNetwork.Mastercard -> binding.imgMaster.isVisible = true
                    CreditCardNetwork.Mada -> binding.imgMada.isVisible = true
                    CreditCardNetwork.Amex -> binding.imgAmex.isVisible = true
                    else -> Unit
                }
            }
        } else {
            when (getNetwork(text.orEmpty())) {
                CreditCardNetwork.Visa -> {
                    binding.imgVisa.isVisible = true
                    binding.imgMaster.isVisible = false
                    binding.imgMada.isVisible = false
                    binding.imgAmex.isVisible = false
                }

                CreditCardNetwork.Mastercard -> {
                    binding.imgMaster.isVisible = true
                    binding.imgVisa.isVisible = false
                    binding.imgMada.isVisible = false
                    binding.imgAmex.isVisible = false
                }

                CreditCardNetwork.Mada -> {
                    binding.imgMada.isVisible = true
                    binding.imgVisa.isVisible = false
                    binding.imgMaster.isVisible = false
                    binding.imgAmex.isVisible = false
                }

                CreditCardNetwork.Amex -> {
                    binding.imgAmex.isVisible = true
                    binding.imgVisa.isVisible = false
                    binding.imgMaster.isVisible = false
                    binding.imgMada.isVisible = false
                }

                else -> {
                    binding.imgVisa.isVisible = false
                    binding.imgMaster.isVisible = false
                    binding.imgMada.isVisible = false
                    binding.imgAmex.isVisible = false
                }
            }
        }
    }

    private fun handleFormValidationState(formValid: Boolean) {
        binding.payButton.isEnabled = formValid
        // for test purpose only
        if (formValid) binding.payButton.setTextColor(
            ContextCompat.getColor(
                requireContext(), sdkR.color.light_blue_button_enabled
            )
        )
        else binding.payButton.setTextColor(ContextCompat.getColor(requireContext(), sdkR.color.red))
    }

    private fun showInvalidCVVErrorMsg(cvvErrorMsg: String?) {
        binding.cvvInputLayout.error = cvvErrorMsg.takeIf { it?.isNotEmpty() == true}
    }

    private fun showInvalidExpiryErrorMsg(expiryErrorMsg: String?) {
        binding.expiryDateInputLayout.error = expiryErrorMsg.takeIf { it?.isNotEmpty() == true}
    }

    private fun showInvalidCardNumberErrorMsg(numberErrorMsg: String?) {
        binding.cardNumberInputLayout.error = numberErrorMsg.takeIf { it?.isNotEmpty() == true}
    }

    private fun showInvalidNameErrorMsg(nameErrorMsg: String?) {
        binding.holderNameInputLayout.error = nameErrorMsg.takeIf { it?.isNotEmpty() == true}
    }

    private fun FragmentCustomUIPaymentBinding.setupListeners() {
        holderNameEt.doAfterTextChanged { text ->
            viewModel.creditCardNameChanged(text?.toString().orEmpty())
        }
        cardNumberEt.doAfterTextChanged { text ->
            viewModel.creditCardNumberChanged(text) { s ->
                // update formating
                cardNumberEt.setText(s)
                // Move cursor to the end of the text
                cardNumberEt.setSelection(s.length)
            }
        }
        expiryDateEt.doAfterTextChanged { text ->
            viewModel.creditCardExpiryChanged(text) { s ->
                // update formating
                expiryDateEt.setText(s)
                // Move cursor to the end of the text
                expiryDateEt.setSelection(s.length)
            }
        }

        cvvEt.doAfterTextChanged { text ->
            viewModel.creditCardCvcChanged(text)
        }
        payButton.setOnClickListener {
            viewModel.submit()
        }
        holderNameEt.setOnFocusChangeListener { _, hf ->
            Log.e("TEXT",""+ holderNameEt.text?.toString().orEmpty())
            viewModel.validateField(
                fieldType = FieldValidation.Name,
                value = holderNameEt.text?.toString().orEmpty(),
                ///name = holderNameEt.text?.toString().orEmpty(),

                // required to validated with cvv
                cardNumber = cardNumberEt.text?.toString().orEmpty(),
                hasFocus = hf,
            )
        }
        cardNumberEt.setOnFocusChangeListener { _, hf ->
            viewModel.validateField(
                fieldType = FieldValidation.Number,
                value = cardNumberEt.text?.toString().orEmpty(),
           //     name = holderNameEt.text?.toString().orEmpty(),
                cardNumber = cardNumberEt.text?.toString().orEmpty(),
                hasFocus = hf,
            )
        }
        expiryDateEt.setOnFocusChangeListener { _, hf ->
            viewModel.validateField(
                fieldType = FieldValidation.Expiry,
                value = expiryDateEt.text?.toString().orEmpty(),
                //name = holderNameEt.text?.toString().orEmpty(),
                cardNumber = cardNumberEt.text?.toString().orEmpty(),
                hasFocus = hf,
            )
        }
        cvvEt.setOnFocusChangeListener { _, hf ->
            viewModel.validateField(
                fieldType = FieldValidation.Cvc,
                value = cvvEt.text?.toString().orEmpty(),
              //  name = holderNameEt.text?.toString().orEmpty(),
                cardNumber = cardNumberEt.text?.toString().orEmpty(),
                hasFocus = hf,
            )
        }
    }

    private fun showLoading() {
        binding.circularProgressIndicator.isVisible = true
    }

    private fun hideLoading() {
        binding.circularProgressIndicator.isVisible = false
    }

    private fun handleOnStatusChanged(status: PaymentStatusViewState?) {
        parentActivity.runOnUiThread {
            when (status) {
                is PaymentStatusViewState.PaymentAuth3dSecure -> {
                    hideLoading()
                    hideScreenViews()
                    childFragmentManager.beginTransaction().apply {
                        replace(R.id.custom_ui_payment_fragment_container, PaymentAuthFragment())
                        commit()
                    }
                }

                is PaymentStatusViewState.Reset -> {
                    hideLoading()
                    showScreenViews()
                }

                is PaymentStatusViewState.SubmittingPayment -> {
                    showLoading()
                    hideScreenViews()
                }

                else -> Unit
            }
        }
    }


    private fun showScreenViews() = with(binding) {
        llContainer.isVisible = true
    }

    private fun hideScreenViews() = with(binding) {
        llContainer.isVisible = false
    }


}