package com.moyasar.android.sdk.payment.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Token(
    val id: String,
    val status: String,
    val brand: String,
    val funding: String,
    val country: String,
    val month: String,
    val year: String,
    val name: String,
    @SerializedName("last_four") val lastFour: String,
    val metadata: Map<String, String>?,
    val message: String?,
    @SerializedName("verification_url") val verificationUrl: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
) : Parcelable
