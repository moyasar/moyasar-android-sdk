package com.moyasar.android.sdk.core.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Created by Mahmoud Ashraf on 23,June,2024
 */
internal fun EditText.afterTextChanged(afterTextChanged: (Editable?) -> Unit) {
  this.addTextChangedListener(object : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(editable: Editable?) {
      afterTextChanged.invoke(editable)
    }
  })
}
