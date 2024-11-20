package com.moyasar.android.sdkdriver

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button


class StartActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        val donateBtn = findViewById<Button>(R.id.button2)
        val stcPayBtn =  findViewById<Button>(R.id.button3)
        donateBtn.setOnClickListener {
            startActivity(
                Intent(this, CheckoutActivity::class.java).apply {
                putExtra(PAYMENT_TYPE,PaymentOptions.CREDIT.name)
            })
        }
        stcPayBtn.setOnClickListener {
            startActivity(
                Intent(this, CheckoutActivity::class.java).apply {
                putExtra(PAYMENT_TYPE,PaymentOptions.STC.name)
            })
        }
    }
    companion object {
        const val PAYMENT_TYPE = "payment_type"
    }

    enum class PaymentOptions {CREDIT, STC}
}