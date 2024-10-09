package com.moyasar.android.sdk.creditcard.data.models.response

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    val id: String,
    val status: String,
    val brand: String,
    val funding: String,
    val country: String,
    val month: String,
    val year: String,
    val name: String,
    @SerializedName("last_four") val lastFour: String,
    val metadata: Map<String, Any>?,
    val message: String?,
    @SerializedName("verification_url") val verificationUrl: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
)
