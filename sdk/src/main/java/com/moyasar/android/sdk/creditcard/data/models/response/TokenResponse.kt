package com.moyasar.android.sdk.creditcard.data.models.response

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("funding")
    val funding: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("month")
    val month: String,

    @SerializedName("year")
    val year: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("last_four")
    val lastFour: String,

    @SerializedName("metadata")
    val metadata: Map<String, Any>?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("verification_url")
    val verificationUrl: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)
