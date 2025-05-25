package com.moyasar.android.sdk.creditcard.data.models.request

import com.google.gson.annotations.SerializedName
import com.moyasar.android.sdk.core.customviews.button.MoyasarButtonType
import com.moyasar.android.sdk.core.data.PaymentSource
import com.moyasar.android.sdk.core.exceptions.InvalidConfigException
import com.moyasar.android.sdk.creditcard.data.models.CreditCardNetwork

data class PaymentRequest(
    val apiKey: String,
    val amount: Int = 0,
    val currency: String = "SAR",
    val description: String? = null,
    val metadata: Map<String, Any?> = HashMap(),
    val manual: Boolean = false,
    val saveCard: Boolean = false,
    @SerializedName("given_id") val givenID: String? = null,
    val allowedNetworks: List<CreditCardNetwork> = listOf(
        CreditCardNetwork.Visa,
        CreditCardNetwork.Mastercard,
        CreditCardNetwork.Mada
    ),
    val baseUrl: String = "https://api.moyasar.com/",
    @SerializedName("callback_url") val callbackUrl: String = "",
    val createSaveOnlyToken: Boolean = false,
    val buttonType: MoyasarButtonType = MoyasarButtonType.PAY,
    val source: PaymentSource? = null,

    ) {
    fun validate(): Array<String> {
        val errors = ArrayList<String>()

        if (amount < 100) {
            errors.add("Amount must be greater than or equal to 100")
        }

        if (currency.length != 3) {
            errors.add("Invalid currency")
        }

        if (!apiKey.matches(Regex("^pk_(test|live)_.{40}\$"))) {
            errors.add("Invalid Publishable API key")
        }

        if (!baseUrl.matches(Regex("^https:\\/\\/api(mig)?.moyasar.com(\\/)?\$"))) {
            errors.add("Invalid base URL")
        }

        metadata.let {
            if (it.count() > 50) {
                errors.add("You cannot add more than 50 elements in metadata.")
            }
            it.values.forEach { value ->
                if (value !is Int && value !is Float && value !is String && value !is Boolean)
                    errors.add("Metadata values should be String, Integer, Float or Boolean")

            }
        }

       return errors.toTypedArray()
    }
}