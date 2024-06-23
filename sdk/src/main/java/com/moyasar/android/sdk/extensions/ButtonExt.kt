package com.moyasar.android.sdk.extensions

import android.os.Build
import android.support.v4.content.ContextCompat
import android.widget.Button
import com.moyasar.android.sdk.R

/**
 * Created by Mahmoud Ashraf on 23,June,2024
 */
internal fun Button.shouldDisableButton(isFormValidNewValue: Boolean) {
  if (isFormValidNewValue) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      background = context.getDrawable(R.drawable.moyasar_bt_enabled_background)
    } else {
      val color = ContextCompat.getColor(context, R.color.light_blue_button_enabled)
      setBackgroundColor(color)
    }
  } else {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      background = context.getDrawable(R.drawable.moyasar_bt_disabled_background)
    } else {
      val color = ContextCompat.getColor(context, R.color.light_blue_button_disabled)
      setBackgroundColor(color)
    }
  }
}