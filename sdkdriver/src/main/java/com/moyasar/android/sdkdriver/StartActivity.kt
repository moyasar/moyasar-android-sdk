package com.moyasar.android.sdkdriver

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class StartActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        val donateBtn = findViewById<Button>(R.id.button2)
        val donateWithCreditCardCustomUIBtn = findViewById<Button>(R.id.donateWithCreditCardCustomUIBtn)
        val stcPayBtn =  findViewById<Button>(R.id.button3)
        val donateWithSTCPayCustomUIBtn =  findViewById<Button>(R.id.donateWithSTCPayCustomUIBtn)
        donateBtn.setOnClickListener {
            startActivity(
                Intent(this, CheckoutActivity::class.java).apply {
                putExtra(PAYMENT_TYPE,PaymentOptions.CREDIT.name)
            })
        }
        donateWithCreditCardCustomUIBtn.setOnClickListener {
            startActivity(
                Intent(this, CheckoutActivity::class.java).apply {
                putExtra(PAYMENT_TYPE,PaymentOptions.CREDIT_CUSTOM_UI.name)
            })
        }
        stcPayBtn.setOnClickListener {
            startActivity(
                Intent(this, CheckoutActivity::class.java).apply {
                putExtra(PAYMENT_TYPE,PaymentOptions.STC.name)
            })
        }
        donateWithSTCPayCustomUIBtn.setOnClickListener {
            startActivity(
                Intent(this, CheckoutActivity::class.java).apply {
                putExtra(PAYMENT_TYPE,PaymentOptions.STC_CUSTOM_UI.name)
            })
        }
    }
    companion object {
        const val PAYMENT_TYPE = "payment_type"
    }

    enum class PaymentOptions {CREDIT, STC, CREDIT_CUSTOM_UI, STC_CUSTOM_UI}
}