package com.moyasar.android.sdk.util

import android.databinding.BindingAdapter
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.EditText
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.data.PaymentSheetViewModel

@BindingAdapter("appMoyasar:showWhenLoading")
fun showWhenLoading(view: View, oldValue: PaymentSheetViewModel.Status?, newValue: PaymentSheetViewModel.Status) {
    if (oldValue != newValue) {
        view.visibility = when (newValue) {
            PaymentSheetViewModel.Status.Reset -> View.INVISIBLE
            else -> View.VISIBLE
        }
    }
}

@BindingAdapter("appMoyasar:showWhenReset")
fun showWhenReset(view: View, oldValue: PaymentSheetViewModel.Status?, newValue: PaymentSheetViewModel.Status) {
    if (oldValue != newValue) {
        view.visibility = when (newValue) {
            PaymentSheetViewModel.Status.Reset -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }
}

@BindingAdapter("appMoyasar:shouldDisableButton")
fun shouldDisableButton(view: View, isFormValidNewValue: Boolean) {
        if (isFormValidNewValue) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.background = view.context.getDrawable(R.drawable.moyasar_bt_enabled_background)
            } else {
                val color =
                    ContextCompat.getColor(view.context, R.color.light_blue_button_enabled)
                view.setBackgroundColor(color)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.background = view.context.getDrawable(R.drawable.moyasar_bt_disabled_background)
            } else {
                val color =
                    ContextCompat.getColor(view.context, R.color.light_blue_button_disabled)
                view.setBackgroundColor(color)
            }
    }
}

@BindingAdapter("appMoyasar:showCcNumberIconsWhenEmpty")
fun showCcNumberIconsWhenEmpty(view: EditText, text: String) {
    if (text.isEmpty()) {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_supported_cards, 0)
    } else {
        view.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

    }
}

@BindingAdapter("appMoyasar:disableWhenLoading")
fun moyasarDisableWhenLoading(view: View, oldValue: PaymentSheetViewModel.Status?, newValue: PaymentSheetViewModel.Status) {
    if (oldValue != newValue) {
        view.isEnabled = when (newValue) {
            PaymentSheetViewModel.Status.Reset -> true
            else -> false
        }
    }
}
