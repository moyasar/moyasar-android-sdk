package com.moyasar.sdkdriver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentSheet

class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val sheet = PaymentSheet(
            this,
            {
                runOnUiThread {
                    Toast.makeText(this, it, Toast.LENGTH_LONG ).show()
                }
            },
            PaymentConfig(
                amount = 100,
                currency = "SAR",
                description = "Sample Android SDK Payment",
                apiKey = "pk_test_vcFUHJDBwiyRu4Bd3hFuPpTnRPY4gp2ssYdNJMY3"
            )
        )

        sheet.present()
    }
}
