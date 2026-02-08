package com.moyasar.android.sdk.samsungpay.presentation

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.core.exceptions.InvalidConfigException
import com.moyasar.android.sdk.core.extensions.show
import com.moyasar.android.sdk.core.util.MoyasarLogger
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.paymentRequest

/**
 * Fragment for Samsung Pay payment flow
 * Matches React Native implementation pattern
 */
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
        val view = inflater.inflate(com.moyasar.android.sdk.R.layout.fragment_samsung_pay, container, false)
        progressBar = view.findViewById(com.moyasar.android.sdk.R.id.progressBar)

        InitiateSamsungPay.initiate(
            requireActivity(),
            paymentRequest,
            authorizePayment = { token, orderNumber ->
                if (token != null && orderNumber != null) {
                    MoyasarLogger.log("MoyasarSDK", "Samsung Pay token received, orderNumber: $orderNumber")
                    progressBar.show()
                    InitiateSamsungPay.authorizePayment(token, orderNumber)
                } else {
                    MoyasarLogger.log("MoyasarSDK", "Samsung Pay token or orderNumber is null")
                }
            }
        )

        return view
    }
}
