package com.moyasar.android.sdkdriver

import android.view.View

fun setSuccessVisibility(view: View, newValue: CheckoutViewModel.Status) {
        view.visibility = when (newValue) {
            CheckoutViewModel.Status.Idle -> View.INVISIBLE
            CheckoutViewModel.Status.Success -> View.VISIBLE
            is CheckoutViewModel.Status.Failed -> View.INVISIBLE
        }
}

fun setErrorVisibility(view: View, newValue: CheckoutViewModel.Status) {
        view.visibility = when (newValue) {
            CheckoutViewModel.Status.Idle -> View.INVISIBLE
            CheckoutViewModel.Status.Success -> View.INVISIBLE
            is CheckoutViewModel.Status.Failed -> View.VISIBLE
        }
}
