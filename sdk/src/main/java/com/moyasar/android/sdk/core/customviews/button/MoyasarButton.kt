package com.moyasar.android.sdk.core.customviews.button

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import java.util.Locale

internal class MoyasarButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.buttonStyle,
) : AppCompatButton(context, attrs, defStyleAttr) {

    init {
        gravity = Gravity.CENTER
    }

    fun setButtonType(buttonType: MoyasarButtonType, iconSymbol: Int? =if (MoyasarAppContainer.paymentRequest.currency=="SAR") R.drawable.moyasar_ic_saudi_riyal_symbol else null) {
        text = when (iconSymbol) {
            null -> {
                if (buttonType == MoyasarButtonType.PLAIN)  getTitleText(buttonType)
                else getTitleText(buttonType).plus(' ').plus(MoyasarAppContainer.viewModel.amountLabel)
            }
            else -> getSpannableButtonTitle(buttonType, iconSymbol)
        }
    }

    private fun getTitleText(buttonType: MoyasarButtonType) = when (buttonType) {
        MoyasarButtonType.PLAIN -> context.getString(R.string.lbl_moyasar_button_plain)
        MoyasarButtonType.PAY -> context.getString(R.string.lbl_moyasar_button_pay)
        MoyasarButtonType.BUY -> context.getString(R.string.lbl_moyasar_button_buy)
        MoyasarButtonType.BOOK -> context.getString(R.string.lbl_moyasar_button_book)
        MoyasarButtonType.RENT -> context.getString(R.string.lbl_moyasar_button_rent)
        MoyasarButtonType.CONTINUE -> context.getString(R.string.lbl_moyasar_button_continue)
        MoyasarButtonType.DONATE -> context.getString(R.string.lbl_moyasar_button_donate)
        MoyasarButtonType.TOP_UP -> context.getString(R.string.lbl_moyasar_button_topup)
        MoyasarButtonType.ORDER -> context.getString(R.string.lbl_moyasar_button_order)
        MoyasarButtonType.SUPPORT -> context.getString(R.string.lbl_moyasar_button_support)
    }

    private fun getSpannableButtonTitle(buttonType: MoyasarButtonType, iconSymbol: Int?): SpannableStringBuilder {
        val title = getTitleText(buttonType)

        val amountLabel = MoyasarAppContainer.viewModel.amountLabel
        val isArabic = Locale.getDefault().language == "ar"

        val fullText = if (isArabic) {
            "$title $amountLabel  " // Space for icon at the end
        } else {
            "$title   $amountLabel" // Space for icon in the middle
        }

        val spannable = SpannableStringBuilder(fullText)

        val drawable: Drawable? = iconSymbol?.let { ContextCompat.getDrawable(context, it) }

        drawable?.let {
            val iconSizePx = dpToPx(context, 16)
            it.setBounds(0, 0, iconSizePx, iconSizePx) // Adjust icon size

            val iconPosition = if (isArabic) fullText.length - 1 else title.length + 1 // Right (Arabic) or Middle (Others)
            val imageSpan = CenteredImageSpan(it)
            spannable.setSpan(imageSpan, iconPosition, iconPosition + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return spannable
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}

/**
 * Custom ImageSpan that properly aligns the icon in the center of the text.
 */
class CenteredImageSpan(drawable: Drawable) : ImageSpan(drawable) {
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val drawable = drawable
        val rect = drawable.bounds

        if (fm != null) {
            val fontHeight = fm.descent - fm.ascent
            val iconHeight = rect.bottom - rect.top

            val centerY = fm.ascent + fontHeight / 2 - iconHeight / 2

            fm.ascent = centerY
            fm.top = fm.ascent
            fm.descent = centerY + iconHeight
            fm.bottom = fm.descent
        }

        return rect.right
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val drawable = drawable
        canvas.save()

        val fontMetrics = paint.fontMetricsInt
        val iconHeight = drawable.bounds.bottom - drawable.bounds.top
        val centerY = y + fontMetrics.descent - (fontMetrics.descent - fontMetrics.ascent) / 2 - iconHeight / 2

        canvas.translate(x, centerY.toFloat())
        drawable.draw(canvas)
        canvas.restore()
    }
}
