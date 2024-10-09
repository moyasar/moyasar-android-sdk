package com.moyasar.android.sdk.creditcard.presentation.model

import com.moyasar.android.sdk.core.extensions.gone
import com.moyasar.android.sdk.core.extensions.show
import com.moyasar.android.sdk.creditcard.data.models.CreditCardNetwork
import com.moyasar.android.sdk.databinding.FragmentPaymentBinding

/**
 * Created by Mahmoud Ashraf on 06,October,2024
 */
internal fun showAllowedCreditCardsInEditText(
    text: String,
    allowedNetworks: List<CreditCardNetwork>,
    binding: FragmentPaymentBinding
) {
    if (text.isEmpty()) {
        allowedNetworks.forEach {
            when (it) {
                CreditCardNetwork.Visa -> binding.imgVisa.show()
                CreditCardNetwork.Mastercard -> binding.imgMaster.show()
                CreditCardNetwork.Mada -> binding.imgMada.show()
                CreditCardNetwork.Amex -> binding.imgAmex.show()
                else -> Unit
            }
        }
    } else {
        binding.imgVisa.gone()
        binding.imgMaster.gone()
        binding.imgMada.gone()
        binding.imgAmex.gone()
    }
}