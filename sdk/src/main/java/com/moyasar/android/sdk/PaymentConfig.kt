package com.moyasar.android.sdk;

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentConfig(
    val amount: Int = 0,
    val currency: String = "SAR",
    val description: String,
    val apiKey: String,
    val baseUrl: String,
    val metadata: Map<String, String>? = null,
) : Parcelable {
    fun validate(): Array<String> {
        val errors = ArrayList<String>()

        if (amount < 100) {
            errors.add("Amount must be greater than or equal to 100")
        }

        if (currency.length != 3) {
            errors.add("Invalid currency")
        }

        if (description.trim().isEmpty()) {
            errors.add("Description is required")
        }

        if (! apiKey.matches(Regex("^pk_(test|live)_.{40}\$"))) {
            errors.add("Invalid Publishable API key")
        }

        if (! baseUrl.matches(Regex("^https:\\/\\/api(mig)?.moyasar.com(\\/)?\$"))) {
            errors.add("Invalid base URL")
        }

        metadata?.let {
            if (it.count() > 50) {
                errors.add("You cannot add more than 50 elements in metadata.")
            }
        }

        return errors.toTypedArray()
    }
}
