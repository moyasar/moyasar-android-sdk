package com.moyasar.android.sdk.creditcard.data.models.request

import com.google.gson.annotations.SerializedName
import com.moyasar.android.sdk.core.customviews.button.MoyasarButtonType
import com.moyasar.android.sdk.core.data.PaymentSource
import com.moyasar.android.sdk.core.exceptions.InvalidConfigException
import com.moyasar.android.sdk.creditcard.data.models.CreditCardNetwork
import com.moyasar.android.sdk.samsungpay.data.SamsungPayConfig

data class PaymentRequest(
    @SerializedName("apiKey")
    val apiKey: String,

    @SerializedName("amount")
    val amount: Int = 0,

    @SerializedName("currency")
    val currency: String = "SAR",

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("metadata")
    val metadata: Map<String, Any?> = HashMap(),

    @SerializedName("manual")
    val manual: Boolean = false,

    @SerializedName("saveCard")
    val saveCard: Boolean = false,

    @SerializedName("given_id")
    val givenID: String? = null,

    @SerializedName("allowedNetworks")
    val allowedNetworks: List<CreditCardNetwork> = listOf(
        CreditCardNetwork.Visa,
        CreditCardNetwork.Mastercard,
        CreditCardNetwork.Mada
    ),

    @SerializedName("baseUrl")
    val baseUrl: String = "https://api.moyasar.com/",

    @SerializedName("callback_url")
    val callbackUrl: String = "",

    @SerializedName("createSaveOnlyToken")
    val createSaveOnlyToken: Boolean = false,

    @SerializedName("buttonType")
    val buttonType: MoyasarButtonType = MoyasarButtonType.PAY,

    @SerializedName("source")
    val source: PaymentSource? = null,
    
    @SerializedName("apply_coupon")
    val applyCoupon: Boolean? = true,

    val merchantCountryCode: String = "SA",
    val samsungPay: SamsungPayConfig? = null

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
