package com.moyasar.android.sdk

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.moyasar.android.sdk.ui.PaymentFragment
import java.io.Serializable

class PaymentSheet(
    private val context: AppCompatActivity,
    private val callback: PaymentSheetResultCallback,
    private val config: PaymentConfig
): PaymentSheetResultCallback, Serializable {

    // TODO: Try if better to return a fragment after configuring it and remove the parameter and context (Maybe better for Compose)
    fun present(fragmentResourceId: Int) {
        val configError = config.validate()
        if (configError.any()) {
            throw InvalidConfigException(configError)
        }

        val paymentFragment = PaymentFragment()
        val args = Bundle()
        args.putParcelable(PaymentFragment.EXTRA_ARGS, config)
        args.putSerializable(PaymentFragment.EXTRA_PAYMENT_SHEET, this)
        paymentFragment.arguments = args

        context.supportFragmentManager.beginTransaction().apply {
            // TODO: Test if should replace or add
            replace(fragmentResourceId, paymentFragment)
            // TODO: Test if need to add to back stack, test with pressing back button and check behavior
            addToBackStack(null)
            commit()
        }
    }

    override fun onResult(result: PaymentResult) {
        callback.onResult(result)
    }
}