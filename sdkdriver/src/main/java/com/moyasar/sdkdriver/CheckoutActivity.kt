package com.moyasar.sdkdriver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.activity.viewModels
import com.moyasar.sdkdriver.databinding.ActivityCheckoutBinding

class CheckoutActivity : AppCompatActivity() {
    private val binding: ActivityCheckoutBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_checkout)
    }

    private val viewModel: CheckoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.registerForActivity(this)

        val donateBtn = findViewById<Button>(R.id.button2)
        donateBtn.setOnClickListener {
            viewModel.beginDonation()
        }
    }
}
