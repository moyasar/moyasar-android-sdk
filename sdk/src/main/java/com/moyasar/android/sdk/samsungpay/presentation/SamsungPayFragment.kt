package com.moyasar.android.sdk.samsungpay.presentation

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.core.exceptions.InvalidConfigException
import com.moyasar.android.sdk.core.extensions.show
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.paymentRequest

class SamsungPayFragment : Fragment() {

    private lateinit var progressBar: View

    companion object {
        fun newInstance(
            application: Application,
            paymentRequest: PaymentRequest,
            callback: (PaymentResult) -> Unit,
        ): SamsungPayFragment {
            val configError = paymentRequest.validate()
            if (configError.any()) {
                throw InvalidConfigException(configError)
            }

            // Validate Samsung Pay configuration
            if (paymentRequest.samsungPay == null) {
                throw InvalidConfigException(arrayOf("Samsung Pay configuration is required"))
            }

            MoyasarAppContainer.initialize(application, paymentRequest, callback)
            return SamsungPayFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_samsung_pay, container, false)
        progressBar = view.findViewById(R.id.progressBar)

        SamsungPayManager.initiate(
            requireActivity(),
            paymentRequest,
            authorizePayment = { token, orderNumber ->
                if (token != null && orderNumber != null) {
                    progressBar.show()
                    SamsungPayManager.authorizePayment(token, orderNumber)
                }
                else MoyasarAppContainer.viewModel.notifyPaymentResult(PaymentResult.Failed(
                    Throwable("Something went wrong.")))
            }
        )

        return view
    }
}
