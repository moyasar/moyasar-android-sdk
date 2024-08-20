package com.moyasar.android.sdk.core.extensions

import android.view.View

// view Extensions
internal fun View.show() {
  visibility = View.VISIBLE
}

internal fun View.gone() {
  visibility = View.GONE
}

internal fun View.hide() {
  visibility = View.INVISIBLE
}