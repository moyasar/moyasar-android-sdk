package com.moyasar.android.sdkdriver

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
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

        viewModel.registerForActivity(this)

        val donateBtn = findViewById<Button>(R.id.button2)
        donateBtn.setOnClickListener {
            viewModel.beginDonation(R.id.paymentSheetFragment)
        }
    }
}
