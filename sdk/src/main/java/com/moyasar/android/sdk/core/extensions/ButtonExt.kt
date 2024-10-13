package com.moyasar.android.sdk.core.extensions

import android.os.Build
import android.support.v4.content.ContextCompat
import android.widget.Button
import com.moyasar.android.sdk.R

/**
 * Created by Mahmoud Ashraf on 23,June,2024
 */
internal fun Button.shouldDisableButton(
  isFormValidNewValue: Boolean,
  bgEnabledDrawableRes: Int = R.drawable.moyasar_bt_enabled_background,
  bgDisabledDrawableRes: Int = R.drawable.moyasar_bt_disabled_background,
  bgEnabledColorRes: Int =  R.color.light_blue_button_enabled,
  bgDisabledColorRes: Int =  R.color.light_blue_button_disabled
  ) {
  if (isFormValidNewValue) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      background = context.getDrawable(bgEnabledDrawableRes)
    } else {
      val color = ContextCompat.getColor(context, bgEnabledColorRes)
      setBackgroundColor(color)
    }
  } else {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      background = context.getDrawable(bgDisabledDrawableRes)
    } else {
      val color = ContextCompat.getColor(context, bgDisabledColorRes)
      setBackgroundColor(color)
    }
  }
}