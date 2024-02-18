package com.moyasar.android.sdk

import android.support.v7.app.AppCompatActivity
import com.moyasar.android.sdk.data.PaymentSheetViewModel
import com.moyasar.android.sdk.data.SharedPaymentViewModelHolder
import com.moyasar.android.sdk.ui.PaymentFragment

class PaymentSheet(
    private val context: AppCompatActivity,
    private val callback: PaymentSheetResultCallback,
    private val config: PaymentConfig
) {

    // TODO: Try if better to return a fragment after configuring it and remove the parameter and context (Maybe better for Compose)
    @Throws(InvalidConfigException::class)
    fun present(fragmentResourceId: Int) {
        val configError = config.validate()
        if (configError.any()) {
            throw InvalidConfigException(configError)
        }

        // TODO: Consider clearing the view model after finishing the payment (onDestroy or sheetResult in fragment?)
        SharedPaymentViewModelHolder.sharedViewModel = PaymentSheetViewModel(context.application, config, callback)

        context.supportFragmentManager.beginTransaction().apply {
            replace(fragmentResourceId, PaymentFragment.newInstance())
            commit()
        }
    }
}