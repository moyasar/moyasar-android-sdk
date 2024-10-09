package com.moyasar.android.sdk.core.customviews.button

import android.content.Context
import android.support.v7.widget.AppCompatButton
import android.util.AttributeSet
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer

/**
 * Created by Mahmoud Ashraf on 01,October,2024
 */
internal class MoyasarButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.buttonStyle,
) : AppCompatButton(context, attrs, defStyleAttr) {

    fun setButtonType(buttonType: MoyasarButtonType) {
        text = getButtonTitle(buttonType)
    }

    private fun getButtonTitle(buttonType: MoyasarButtonType): String {
        return when (buttonType) {
            MoyasarButtonType.PLAIN -> context.getString(R.string.lbl_moyasar_button_plain)
            MoyasarButtonType.PAY -> context.getString(R.string.lbl_moyasar_button_pay).plus(' ')
                .plus(MoyasarAppContainer.viewModel.amountLabel)

            MoyasarButtonType.BUY -> context.getString(R.string.lbl_moyasar_button_buy).plus(' ')
                .plus(MoyasarAppContainer.viewModel.amountLabel)

            MoyasarButtonType.BOOK -> context.getString(R.string.lbl_moyasar_button_book).plus(' ')
                .plus(MoyasarAppContainer.viewModel.amountLabel)

            MoyasarButtonType.RENT -> context.getString(R.string.lbl_moyasar_button_rent).plus(' ')
                .plus(MoyasarAppContainer.viewModel.amountLabel)

            MoyasarButtonType.CONTINUE -> context.getString(R.string.lbl_moyasar_button_continue)
                .plus(' ').plus(MoyasarAppContainer.viewModel.amountLabel)

            MoyasarButtonType.DONATE -> context.getString(R.string.lbl_moyasar_button_donate)
                .plus(' ').plus(MoyasarAppContainer.viewModel.amountLabel)

            MoyasarButtonType.TOP_UP -> context.getString(R.string.lbl_moyasar_button_topup)
                .plus(' ').plus(MoyasarAppContainer.viewModel.amountLabel)

            MoyasarButtonType.ORDER -> context.getString(R.string.lbl_moyasar_button_order)
                .plus(' ').plus(MoyasarAppContainer.viewModel.amountLabel)

            MoyasarButtonType.SUPPORT -> context.getString(R.string.lbl_moyasar_button_support)
                .plus(' ').plus(MoyasarAppContainer.viewModel.amountLabel)
        }
    }
}