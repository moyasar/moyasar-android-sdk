package com.moyasar.android.sdk.creditcard.presentation.model

import com.moyasar.android.sdk.core.extensions.gone
import com.moyasar.android.sdk.core.extensions.show
import com.moyasar.android.sdk.creditcard.data.models.CreditCardNetwork
import com.moyasar.android.sdk.creditcard.data.models.getNetwork
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
                CreditCardNetwork.Visa -> binding.viewCard.imgVisa.show()
                CreditCardNetwork.Mastercard -> binding.viewCard.imgMaster.show()
                CreditCardNetwork.Mada -> binding.viewCard.imgMada.show()
                CreditCardNetwork.Amex -> binding.viewCard.imgAmex.show()
                else -> Unit
            }
        }
    } else {
        when (getNetwork(text)) {
            CreditCardNetwork.Visa -> {
                binding.viewCard.imgVisa.show()
                binding.viewCard.imgMaster.gone()
                binding.viewCard.imgMada.gone()
                binding.viewCard.imgAmex.gone()
            }

            CreditCardNetwork.Mastercard -> {
                binding.viewCard.imgMaster.show()
                binding.viewCard.imgVisa.gone()
                binding. viewCard . imgMada.gone ()
                binding . viewCard . imgAmex.gone ()
        }
            CreditCardNetwork.Mada -> {
                binding.viewCard.imgMada.show()
                binding.viewCard.imgVisa.gone()
                binding.viewCard.imgMaster.gone()
                binding.viewCard.imgAmex.gone()
            }
            CreditCardNetwork.Amex -> {
                binding.viewCard.imgAmex.show()
                binding.viewCard.imgVisa.gone()
                binding.viewCard.imgMaster.gone()
                binding.viewCard.imgMada.gone()
            }
            else -> {
                binding.viewCard.imgVisa.gone()
                binding.viewCard.imgMaster.gone()
                binding.viewCard.imgMada.gone()
                binding.viewCard.imgAmex.gone()
            }
        }
    }
}