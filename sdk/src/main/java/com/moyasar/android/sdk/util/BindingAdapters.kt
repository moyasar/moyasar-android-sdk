package com.moyasar.android.sdk.util

import android.view.View
import androidx.databinding.BindingAdapter
import com.moyasar.android.sdk.data.PaymentSheetViewModel
import com.moyasar.android.sdk.data.StcPaySheetViewModel

@BindingAdapter("android:showWhenLoading")
fun showWhenLoading(view: View, oldValue: PaymentSheetViewModel.Status?, newValue: PaymentSheetViewModel.Status) {
    if (oldValue != newValue) {
        view.visibility = when (newValue) {
            PaymentSheetViewModel.Status.Reset -> View.INVISIBLE
            else -> View.VISIBLE
        }
    }
}
@BindingAdapter("android:stcShowWhenLoading")
fun stcShowWhenLoading(view: View, oldValue: StcPaySheetViewModel.Status?, newValue: StcPaySheetViewModel.Status) {
    if (oldValue != newValue) {
        view.visibility = when (newValue) {
            StcPaySheetViewModel.Status.Reset -> View.INVISIBLE
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
@BindingAdapter("android:stcShowWhenReset")
fun stcShowWhenReset(view: View, oldValue: StcPaySheetViewModel.Status?, newValue: StcPaySheetViewModel.Status) {
    if (oldValue != newValue) {
        view.visibility = when (newValue) {
            StcPaySheetViewModel.Status.Reset -> View.VISIBLE
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
@BindingAdapter("android:stcDisableWhenLoading")
fun stcDisableWhenLoading(view: View, oldValue: StcPaySheetViewModel.Status?, newValue: StcPaySheetViewModel.Status) {
    if (oldValue != newValue) {
        view.isEnabled = when (newValue) {
            StcPaySheetViewModel.Status.Reset -> true
            else -> false
        }
    }
}
