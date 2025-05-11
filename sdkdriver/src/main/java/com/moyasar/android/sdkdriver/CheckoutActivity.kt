package com.moyasar.android.sdkdriver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moyasar.android.sdkdriver.StartActivity.Companion.PAYMENT_TYPE
import com.moyasar.android.sdkdriver.databinding.ActivityCheckoutBinding

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding

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
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val paymentType = intent.getStringExtra(PAYMENT_TYPE)
        when(paymentType){
            StartActivity.PaymentOptions.CREDIT.name-> {
                viewModel.beginDonationWithCreditCard(this, R.id.paymentSheetFragment)
            }
            StartActivity.PaymentOptions.CREDIT_CUSTOM_UI.name-> {
                viewModel.beginDonationWithCreditCardCustomUI(this, R.id.paymentSheetFragment)
            }
            StartActivity.PaymentOptions.STC.name-> {
                viewModel.beginDonationWithSTC(this,  R.id.paymentSheetFragment)
            }
            StartActivity.PaymentOptions.STC_CUSTOM_UI.name-> {
                viewModel.beginDonationWithSTCCustomUI(this,  R.id.paymentSheetFragment)
            }
        }
        viewModel.status.observe(this){
            setSuccessVisibility(binding.successTv, it)
            setErrorVisibility(binding.errorTv, it)
        }
    }
}
