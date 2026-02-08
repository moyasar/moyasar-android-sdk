package com.moyasar.android.sdk.samsungpay.presentation

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.util.MoyasarLogger
import com.moyasar.android.sdk.creditcard.data.models.CreditCardNetwork
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.paymentRequest
import com.moyasar.android.sdk.creditcard.presentation.view.fragments.PaymentAuthFragment
import com.moyasar.android.sdk.samsungpay.data.PaymentCredentialJson
import com.moyasar.android.sdk.samsungpay.data.sources.SamsungPayPaymentSource
import com.samsung.android.sdk.samsungpay.v2.PartnerInfo
import com.samsung.android.sdk.samsungpay.v2.SamsungPay
import com.samsung.android.sdk.samsungpay.v2.SpaySdk
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_ALREADY_DONE
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_DUPLICATED_SDK_API_CALLED
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_INITIATION_FAIL
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_INVALID_INPUT
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_INVALID_PARAMETER
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_NONE
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_NOT_ALLOWED
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_NOT_FOUND
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_NOT_SUPPORTED
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_NO_NETWORK
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_PARTNER_INFO_INVALID
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_PARTNER_SDK_API_LEVEL
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_PARTNER_SERVICE_TYPE
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_REGISTRATION_FAIL
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_SERVER_NO_RESPONSE
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_SPAY_INTERNAL
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ERROR_USER_CANCELED
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.EXTRA_ERROR_REASON
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.SPAY_NOT_READY
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.SPAY_NOT_SUPPORTED
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ServiceType
import com.samsung.android.sdk.samsungpay.v2.StatusListener
import com.samsung.android.sdk.samsungpay.v2.payment.CardInfo
import com.samsung.android.sdk.samsungpay.v2.payment.CustomSheetPaymentInfo
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountBoxControl
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountConstants
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.CustomSheet
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale
import java.util.UUID
import kotlin.math.pow

object SamsungPayManager {

    fun initiate(
        context: Context,
        paymentRequest: PaymentRequest,
        authorizePayment: (String?, String?) -> Unit
    ) {
        val samsungPayConfig = paymentRequest.samsungPay
            ?: throw IllegalArgumentException("Samsung Pay configuration is required")

        val partnerInfo = samsungPayPartnerInfo(samsungPayConfig.serviceId)
        val paymentManager = PaymentManager(context, partnerInfo)
        val samsungPay = SamsungPay(context, partnerInfo)

        samsungPay.getSamsungPayStatus(object : StatusListener {
            override fun onSuccess(status: Int, bundle: Bundle) {
                when (status) {
                    SpaySdk.SPAY_READY -> {
                        MoyasarLogger.log("MoyasarSDK", "Samsung Pay ready")
                        // Only proceed with payment when status is READY
                        startInAppPay(
                            paymentManager,
                            paymentRequest,
                            context,
                            samsungPay,
                            authorizePayment
                        )
                    }
                    SpaySdk.SPAY_NOT_READY -> {
                        MoyasarLogger.log("MoyasarSDK", "Samsung Pay not ready")
                        val extraError = bundle.getInt(SamsungPay.EXTRA_ERROR_REASON)
                        if (extraError == SamsungPay.ERROR_SPAY_SETUP_NOT_COMPLETED) {
                            samsungPay.activateSamsungPay()
                            Toast.makeText(
                                context.applicationContext,
                                context.getString(R.string.msg_moyasar_samsung_pay_go_to_activate_page),
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (extraError == SamsungPay.ERROR_SPAY_APP_NEED_TO_UPDATE) {
                            samsungPay.goToUpdatePage()
                            Toast.makeText(
                                context.applicationContext,
                                context.getString(R.string.msg_moyasar_samsung_pay_go_to_update_page),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        // Notify that payment cannot proceed
                        authorizePayment(null, null)
                    }
                    SpaySdk.SPAY_NOT_ALLOWED_TEMPORALLY -> {
                        MoyasarLogger.log("MoyasarSDK", "Samsung Pay not allowed temporarily")
                        Toast.makeText(
                            context.applicationContext,
                            context.getString(R.string.msg_moyasar_samsung_pay_feature_is_not_allowed_temp),
                            Toast.LENGTH_LONG
                        ).show()
                        // Notify that payment cannot proceed
                        authorizePayment(null, null)
                    }
                    SpaySdk.SPAY_NOT_SUPPORTED -> {
                        MoyasarLogger.log(
                            "MoyasarSDK",
                            "Samsung Pay is not supported on this device or emulator/simulator"
                        )
                        Toast.makeText(
                            context.applicationContext,
                            context.getString(R.string.msg_moyasar_samsung_pay_feature_is_not_supported),
                            Toast.LENGTH_LONG
                        ).show()
                        // Notify that payment cannot proceed
                        authorizePayment(null, null)
                    }
                    else -> {
                        MoyasarLogger.log("MoyasarSDK", "Samsung Pay unknown status: $status")
                        Toast.makeText(
                            context.applicationContext,
                            context.getString(R.string.msg_moyasar_samsung_pay_error),
                            Toast.LENGTH_LONG
                        ).show()
                        // Notify that payment cannot proceed
                        authorizePayment(null, null)
                    }
                }
            }

            override fun onFail(errorCode: Int, bundle: Bundle?) {
                bundle?.let { handleOnFail(it, samsungPay, context, null) }
                // Notify that payment cannot proceed
                authorizePayment(null, null)
            }
        })
    }

    /**
     * Creates payment request with Samsung Pay token and order number in metadata
     */
    fun authorizePayment(token: String, orderNumber: String) {
        val samsungPayConfig = paymentRequest.samsungPay
            ?: throw IllegalArgumentException("Samsung Pay configuration is required")
        // Add order number to metadata
        val updatedMetadata = paymentRequest.metadata.toMutableMap().apply {
            put("samsungpay_order_id", orderNumber)
        }

        MoyasarAppContainer.viewModel.createPayment(
            request = paymentRequest.copy(
                callbackUrl = PaymentAuthFragment.RETURN_URL,
                source = SamsungPayPaymentSource(
                    token = token,
                    manual = if (samsungPayConfig.manual) "true" else "false"
                ),
                metadata = updatedMetadata,
                manual = samsungPayConfig.manual
            )
        )
    }

    private fun startInAppPay(
        paymentManager: PaymentManager,
        paymentRequest: PaymentRequest,
        context: Context,
        samsungPay: SamsungPay,
        authorizePayment: (String?, String?) -> Unit
    ) {
        val customSheetPaymentInfo = makeCustomSheetPaymentInfo(paymentRequest)
        paymentManager.startInAppPayWithCustomSheet(
            customSheetPaymentInfo,
            object : PaymentManager.CustomSheetTransactionInfoListener {
                override fun onCardInfoUpdated(cardInfo: CardInfo, sheet: CustomSheet) {
                    val amountControl = sheet.getSheetControl(AMOUNT_CONTROL_ID) as? AmountBoxControl
                        ?: return

                    amountControl.currencyCode = paymentRequest.currency
                    val currentLocale = Locale.getDefault()
                    val paymentCurrency = Currency.getInstance(paymentRequest.currency)
                    val currencyFormatter = DecimalFormat.getCurrencyInstance(currentLocale).apply {
                        currency = paymentCurrency
                    }
                    val amountFormatted =
                        paymentRequest.amount / (10.0.pow(currencyFormatter.currency!!.defaultFractionDigits.toDouble()))

                    amountControl.setAmountTotal(amountFormatted, AmountConstants.FORMAT_TOTAL_PRICE_ONLY)
                    sheet.updateControl(amountControl)
                    paymentManager.updateSheet(sheet)
                }

                override fun onSuccess(
                    response: CustomSheetPaymentInfo,
                    paymentCredential: String,
                    extraPaymentData: Bundle
                ) {
                    try {
                        val DPAN = response.cardInfo.cardMetaData.getString(SpaySdk.EXTRA_LAST4_DPAN, "")
                        val FPAN = response.cardInfo.cardMetaData.getString(SpaySdk.EXTRA_LAST4_FPAN, "")
                        MoyasarLogger.log("MoyasarSDK", "Samsung Pay success - DPAN: $DPAN FPAN: $FPAN")
                    } catch (e: NullPointerException) {
                        MoyasarLogger.log("MoyasarSDK", "Error getting card metadata: ${e.message}")
                    }

                    // Extract token from payment credential
                    val paymentCredentialJson = Gson().fromJson(paymentCredential, PaymentCredentialJson::class.java)
                    val token = paymentCredentialJson.data?.data
                    val orderNumber = response.orderNumber

                    if (token != null) {
                        authorizePayment(token, orderNumber)
                    } else {
                        MoyasarLogger.log("MoyasarSDK", "Samsung Pay token is null")
                        authorizePayment(null, null)
                    }
                }

                override fun onFailure(errorCode: Int, errorData: Bundle?) {
                    if (errorCode == PaymentManager.ERROR_USER_CANCELED) {
                        MoyasarLogger.log("MoyasarSDK", "Samsung Pay canceled by user")
                        return
                    }
                    handleOnFail(errorData ?: Bundle(), samsungPay, context, errorCode)
                    authorizePayment(null, null)
                }
            })
    }

    private fun handleOnFail(
        errorData: Bundle,
        samsungPay: SamsungPay,
        context: Context,
        errorCode: Int?,
    ) {
        val errorReason = errorData.getInt(EXTRA_ERROR_REASON, -11111)
        when (errorReason) {
            SPAY_NOT_SUPPORTED -> {
                samsungPay.activateSamsungPay()
                Toast.makeText(
                    context.applicationContext,
                    context.getString(R.string.msg_moyasar_samsung_pay_go_to_activate_page),
                    Toast.LENGTH_LONG
                ).show()
                MoyasarLogger.log("reason-action", "activateSamsungPay")
            }

            SPAY_NOT_READY -> {
                samsungPay.goToUpdatePage()
                Toast.makeText(
                    context.applicationContext,
                    context.getString(R.string.msg_moyasar_samsung_pay_go_to_update_page),
                    Toast.LENGTH_LONG
                ).show()
                MoyasarLogger.log("reason-action", "goToUpdatePage")
            }

            else -> {
                handleError(errorCode, context)
                MoyasarLogger.log("reason-action", "show-toast")
            }
        }
    }

    private fun handleError(errorCode: Int?, context: Context) {
        var errorMessage = context.getString(R.string.msg_moyasar_samsung_pay_error)
        when (errorCode) {
            ERROR_NONE -> MoyasarLogger.log("samsung-pay-error", "ERROR_NONE")
            ERROR_SPAY_INTERNAL -> MoyasarLogger.log("samsung-pay-error", "ERROR_SPAY_INTERNAL")
            ERROR_INVALID_INPUT -> MoyasarLogger.log("samsung-pay-error", "ERROR_INVALID_INPUT")
            ERROR_NOT_SUPPORTED -> {
                errorMessage = context.getString(R.string.msg_moyasar_samsung_pay_feature_is_not_supported)
                MoyasarLogger.log("samsung-pay-error", "ERROR_NOT_SUPPORTED")
            }
            ERROR_NOT_FOUND -> MoyasarLogger.log("samsung-pay-error", "ERROR_NOT_FOUND")
            ERROR_ALREADY_DONE -> MoyasarLogger.log("samsung-pay-error", "ERROR_ALREADY_DONE")
            ERROR_NOT_ALLOWED -> MoyasarLogger.log("samsung-pay-error", "ERROR_NOT_ALLOWED")
            ERROR_USER_CANCELED -> MoyasarLogger.log("samsung-pay-error", "ERROR_USER_CANCELED")
            ERROR_PARTNER_SDK_API_LEVEL -> MoyasarLogger.log("samsung-pay-error", "ERROR_PARTNER_SDK_API_LEVEL")
            ERROR_PARTNER_SERVICE_TYPE -> MoyasarLogger.log("samsung-pay-error", "ERROR_PARTNER_SERVICE_TYPE")
            ERROR_INVALID_PARAMETER -> MoyasarLogger.log("samsung-pay-error", "ERROR_INVALID_PARAMETER")
            ERROR_NO_NETWORK -> MoyasarLogger.log("samsung-pay-error", "ERROR_NO_NETWORK")
            ERROR_SERVER_NO_RESPONSE -> MoyasarLogger.log("samsung-pay-error", "ERROR_SERVER_NO_RESPONSE")
            ERROR_PARTNER_INFO_INVALID -> MoyasarLogger.log("samsung-pay-error", "ERROR_PARTNER_INFO_INVALID")
            ERROR_INITIATION_FAIL -> MoyasarLogger.log("samsung-pay-error", "ERROR_INITIATION_FAIL")
            ERROR_REGISTRATION_FAIL -> MoyasarLogger.log("samsung-pay-error", "ERROR_REGISTRATION_FAIL")
            ERROR_DUPLICATED_SDK_API_CALLED -> MoyasarLogger.log("samsung-pay-error", "ERROR_DUPLICATED_SDK_API_CALLED")
            ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION -> MoyasarLogger.log("samsung-pay-error", "ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION")
            else -> {
                MoyasarLogger.log("samsung-pay-error", "SOMETHING_WRONG")
            }
        }
        Toast.makeText(
            context.applicationContext,
            errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun samsungPayPartnerInfo(serviceId: String): PartnerInfo {
        val bundle = Bundle().apply {
            putString(SpaySdk.PARTNER_SERVICE_TYPE, ServiceType.INAPP_PAYMENT.toString())
        }
        return PartnerInfo(serviceId, bundle)
    }

    private fun makeCustomSheetPaymentInfo(paymentRequest: PaymentRequest): CustomSheetPaymentInfo {
        val samsungPayConfig = paymentRequest.samsungPay
            ?: throw IllegalArgumentException("Samsung Pay configuration is required")

        // Convert amount from minor units to major units
        val currentLocale = Locale.getDefault()
        val paymentCurrency = Currency.getInstance(paymentRequest.currency)
        val currencyFormatter = DecimalFormat.getCurrencyInstance(currentLocale).apply {
            currency = paymentCurrency
        }
        val amountFormatted =
            paymentRequest.amount / (10.0.pow(currencyFormatter.currency!!.defaultFractionDigits.toDouble()))

        // Create amount control
        val amountBoxControl = AmountBoxControl(AMOUNT_CONTROL_ID, paymentRequest.currency)
        amountBoxControl.setAmountTotal(amountFormatted, AmountConstants.FORMAT_TOTAL_PRICE_ONLY)

        val customSheet = CustomSheet()
        customSheet.addControl(amountBoxControl)

        // Generate order number if not provided
        val orderNumber = samsungPayConfig.orderNumber ?: UUID.randomUUID().toString()

        // Map CreditCardNetwork to SpaySdk.Brand
        val brandList = mapCreditCardNetworksToBrands(paymentRequest.allowedNetworks)

        // Extract merchant ID from API key (first 15 characters)
        val merchantId = paymentRequest.apiKey.substring(0, minOf(15, paymentRequest.apiKey.length))

        return CustomSheetPaymentInfo.Builder()
            .setMerchantId(merchantId) // Required for MADA
            .setMerchantName(samsungPayConfig.merchantName)
            .setOrderNumber(orderNumber) // Required for VISA
            .setAllowedCardBrands(brandList)
            .setMerchantCountryCode(paymentRequest.merchantCountryCode) // Required for MADA
            .setCustomSheet(customSheet)
            .build()
    }

    private fun mapCreditCardNetworksToBrands(networks: List<CreditCardNetwork>): ArrayList<SpaySdk.Brand> {
        val brandList = ArrayList<SpaySdk.Brand>()
        networks.forEach { network ->
            when (network) {
                CreditCardNetwork.Mada -> brandList.add(SpaySdk.Brand.MADA)
                CreditCardNetwork.Visa -> brandList.add(SpaySdk.Brand.VISA)
                CreditCardNetwork.Mastercard -> brandList.add(SpaySdk.Brand.MASTERCARD)
                CreditCardNetwork.Amex -> brandList.add(SpaySdk.Brand.AMERICANEXPRESS)
                CreditCardNetwork.Unknown -> {
                    // Unknown card network, skip
                }
            }
        }
        return brandList
    }
}

private const val AMOUNT_CONTROL_ID = "amountControlId"
