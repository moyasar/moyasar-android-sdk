package com.moyasar.android.sdkdriver

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("android:setDonateVisibleFromStatus")
fun setDonateVisibility(view: View, oldValue: CheckoutViewModel.Status?, newValue: CheckoutViewModel.Status) {
    if (oldValue != newValue) {
        view.visibility = when (newValue) {
            CheckoutViewModel.Status.Idle -> View.VISIBLE
            CheckoutViewModel.Status.Success -> View.INVISIBLE
            is CheckoutViewModel.Status.Failed -> View.INVISIBLE
        }
    }
}

@BindingAdapter("android:setSuccessVisibleFromStatus")
fun setSuccessVisibility(view: View, oldValue: CheckoutViewModel.Status?, newValue: CheckoutViewModel.Status) {
    if (oldValue != newValue) {
        view.visibility = when (newValue) {
            CheckoutViewModel.Status.Idle -> View.INVISIBLE
            CheckoutViewModel.Status.Success -> View.VISIBLE
            is CheckoutViewModel.Status.Failed -> View.INVISIBLE
        }
    }
}

@BindingAdapter("android:setErrorVisibleFromStatus")
fun setErrorVisibility(view: View, oldValue: CheckoutViewModel.Status?, newValue: CheckoutViewModel.Status) {
    if (oldValue != newValue) {
        view.visibility = when (newValue) {
            CheckoutViewModel.Status.Idle -> View.INVISIBLE
            CheckoutViewModel.Status.Success -> View.INVISIBLE
            is CheckoutViewModel.Status.Failed -> View.VISIBLE
        }
    }
}
