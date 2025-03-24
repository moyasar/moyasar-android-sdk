package com.moyasar.android.sdk.samsungpay.presentation

import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.core.exceptions.InvalidConfigException
import com.moyasar.android.sdk.core.extensions.show
import com.moyasar.android.sdk.core.util.MoyasarLogger
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.paymentRequest
import com.moyasar.android.sdk.databinding.FragmentPaymentBinding
import com.moyasar.android.sdk.databinding.FragmentSamsungPayBinding


class SamsungPayFragment : Fragment() {

    private lateinit var binding: FragmentSamsungPayBinding

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
            MoyasarAppContainer.initialize(application, paymentRequest, callback)
            return SamsungPayFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSamsungPayBinding.inflate(inflater, container, false)
      //TODO TEST  InitiateSamsungPay.authorizePayment("alslslsslslslsl")
        InitiateSamsungPay.initiate(requireActivity() ,paymentRequest.apiKey, paymentRequest.samsungPayOrderNum,
            authorizePayment = { token->
                MoyasarLogger.log("spay token data",token?.data?:"")
                binding.progressBar.show()
                InitiateSamsungPay.authorizePayment(token?.data?:"")

            })
        return binding.root
    }


}