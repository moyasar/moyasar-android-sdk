//package com.moyasar.android.sdkdriver.customui
//
//import android.app.Application
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.FragmentActivity
//import com.moyasar.android.sdk.core.domain.entities.PaymentResult
//import com.moyasar.android.sdk.core.exceptions.InvalidConfigException
//import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
//import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
//import com.moyasar.android.sdk.databinding.FragmentPaymentBinding
//import com.moyasar.android.sdkdriver.R
//import com.moyasar.android.sdkdriver.databinding.FragmentCustomUIPaymentBinding
//
//
//class CustomUIPaymentFragment : Fragment() {
//    // TODO 1 - You First Need to Create CustomPaymentUI Fragment to hold your custom UI and UI Logic
//
//    private lateinit var parentActivity: FragmentActivity
//    private lateinit var binding: FragmentCustomUIPaymentBinding
//
//    companion object {
//        fun newInstance(
//            application: Application,
//            paymentRequest: PaymentRequest,
//            callback: (PaymentResult) -> Unit,
//        ): CustomUIPaymentFragment {
//            val configError = paymentRequest.validate()
//            if (configError.any()) {
//                throw InvalidConfigException(configError)
//            }
//            MoyasarAppContainer.initialize(application, paymentRequest, callback)
//            return CustomUIPaymentFragment()
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?,
//    ): View {
//        super.onCreateView(inflater, container, savedInstanceState)
//        parentActivity = requireActivity()
//        binding = FragmentCustomUIPaymentBinding.inflate(inflater, container, false)
//        initView()
//        setupObservers()
//        binding.setupListeners()
//        return binding.root
//    }
//}