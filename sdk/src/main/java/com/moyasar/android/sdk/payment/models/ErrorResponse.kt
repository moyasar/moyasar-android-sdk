package com.moyasar.android.sdk.payment.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable
@Parcelize
data class ErrorResponse(val message: String?, val type: String?, val errors: Map<String, List<String>>?) : Parcelable, Serializable {}
