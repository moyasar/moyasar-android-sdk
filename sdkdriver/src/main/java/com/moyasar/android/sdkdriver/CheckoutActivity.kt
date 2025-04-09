package com.moyasar.android.sdkdriver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moyasar.android.sdkdriver.StartActivity.Companion.PAYMENT_TYPE
import com.moyasar.android.sdkdriver.databinding.ActivityCheckoutBinding

class CheckoutActivity : AppCompatActivity() {
    private val binding: ActivityCheckoutBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_checkout)
    }

    private val viewModel: CheckoutViewModel by lazy {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CheckoutViewModel() as T
            }
        }

        ViewModelProvider(this, factory).get(CheckoutViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val paymentType = intent.getStringExtra(PAYMENT_TYPE)
        when(paymentType){
            StartActivity.PaymentOptions.CREDIT.name-> {
                viewModel.beginDonationWithCreditCard(this, R.id.paymentSheetFragment)
            }
            StartActivity.PaymentOptions.STC.name-> {
                viewModel.beginDonationWithSTC(this,  R.id.paymentSheetFragment)
            }
        }
    }
}
