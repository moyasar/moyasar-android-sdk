package com.moyasar.android.sdk.util

import android.view.View
import android.databinding.BindingAdapter
import com.moyasar.android.sdk.data.PaymentSheetViewModel

@BindingAdapter("android:showWhenLoading")
fun showWhenLoading(view: View, oldValue: PaymentSheetViewModel.Status?, newValue: PaymentSheetViewModel.Status) {
    if (oldValue != newValue) {
        view.visibility = when (newValue) {
            PaymentSheetViewModel.Status.Reset -> View.INVISIBLE
            else -> View.VISIBLE
        }
    }
}

@BindingAdapter("android:showWhenReset")
fun showWhenReset(view: View, oldValue: PaymentSheetViewModel.Status?, newValue: PaymentSheetViewModel.Status) {
    if (oldValue != newValue) {
        view.visibility = when (newValue) {
            PaymentSheetViewModel.Status.Reset -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }
}

@BindingAdapter("android:disableWhenLoading")
fun disableWhenLoading(view: View, oldValue: PaymentSheetViewModel.Status?, newValue: PaymentSheetViewModel.Status) {
    if (oldValue != newValue) {
        view.isEnabled = when (newValue) {
            PaymentSheetViewModel.Status.Reset -> true
            else -> false
        }
    }
}
