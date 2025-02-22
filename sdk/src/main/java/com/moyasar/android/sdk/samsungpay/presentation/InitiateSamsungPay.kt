package com.moyasar.android.sdk.samsungpay.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.util.MoyasarLogger
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.paymentRequest
import com.moyasar.android.sdk.creditcard.presentation.view.fragments.PaymentAuthFragment
import com.moyasar.android.sdk.samsungpay.data.CryptoDataJson
import com.moyasar.android.sdk.samsungpay.data.PaymentCredentialJson
import com.moyasar.android.sdk.samsungpay.data.sources.SamsungPayPaymentSource
import com.moyasar.android.sdk.stcpay.data.models.sources.STCPayPaymentSource
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
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.SPAY_READY
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ServiceType
import com.samsung.android.sdk.samsungpay.v2.StatusListener
import com.samsung.android.sdk.samsungpay.v2.payment.CardInfo
import com.samsung.android.sdk.samsungpay.v2.payment.CustomSheetPaymentInfo
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountBoxControl
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountConstants
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.CustomSheet


/**
 * Created by Mahmoud Ashraf on 16,February,2025
 */
object InitiateSamsungPay {
    fun initiate(context: Context, apiKey: String, orderNum: String, authorizePayment: (CryptoDataJson?)->Unit) {
        val paymentManager = PaymentManager(context, samsungPayPartnerInfo())
        val samsungPay = SamsungPay(context, samsungPayPartnerInfo())
        samsungPay.getSamsungPayStatus(object : StatusListener{
            override fun onSuccess(status: Int, bundle: Bundle?) {
                when (status) {
                    SPAY_NOT_SUPPORTED -> {
                        Log.e("s-pay","not supported")
                        Toast.makeText(
                            context.applicationContext,
                            context.getString(R.string.msg_moyasar_samsung_pay_feature_is_not_supported),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        startInAppPay(paymentManager, apiKey, orderNum, context, samsungPay, authorizePayment)

                    }
                }
            }

            override fun onFail(errorCode: Int, bundle: Bundle?) {
                bundle?.let { handleOnFail(it, samsungPay, context, null) }
            }

        })
    }

    fun authorizePayment(token: String){
        MoyasarAppContainer.viewModel.createPayment(request = paymentRequest.copy(
            callbackUrl = PaymentAuthFragment.RETURN_URL,
            source = SamsungPayPaymentSource(
               token
            )
        ))
    }
    private fun startInAppPay(
        paymentManager: PaymentManager,
        apiKey: String,
        orderNum: String,
        context: Context,
        samsungPay: SamsungPay,
        authorizePayment: (CryptoDataJson?)->Unit
    ) {
         paymentManager.startInAppPayWithCustomSheet(makeCustomSheetPaymentInfo(apiKey, orderNum), object :
       PaymentManager.CustomSheetTransactionInfoListener {
       override fun onCardInfoUpdated(cardInfo: CardInfo, sheet: CustomSheet) {
           val amountControl = sheet.getSheetControl("amount-control") as AmountBoxControl
           amountControl.setAmountTotal(1.00, AmountConstants.FORMAT_TOTAL_PRICE_ONLY)

           sheet.updateControl(amountControl)
           paymentManager.updateSheet(sheet)
       }

       override fun onSuccess(response: CustomSheetPaymentInfo, paymentCredential: String, extraPaymentData: Bundle) {
           try {
               val DPAN = response.cardInfo.cardMetaData.getString(SpaySdk.EXTRA_LAST4_DPAN, "")
               val FPAN = response.cardInfo.cardMetaData.getString(SpaySdk.EXTRA_LAST4_FPAN, "")

              MoyasarLogger.log("success get card data ", "DPAN: " + DPAN + "FPAN: " + FPAN)
           } catch (e: java.lang.NullPointerException) {
               e.printStackTrace()
           }

           Toast.makeText(context, "Transaction : onSuccess", Toast.LENGTH_LONG).show()
           val token = Gson().fromJson(paymentCredential, PaymentCredentialJson::class.java).data
           authorizePayment(token)
       }

       override fun onFailure(errorCode: Int, errorData: Bundle) {
           handleOnFail(errorData, samsungPay, context, errorCode)
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
        when(errorCode){
            ERROR_NONE-> MoyasarLogger.log("samsung-pay-error","ERROR_NONE")
            ERROR_SPAY_INTERNAL-> MoyasarLogger.log("samsung-pay-error","ERROR_SPAY_INTERNAL")
            ERROR_INVALID_INPUT-> MoyasarLogger.log("samsung-pay-error","ERROR_INVALID_INPUT")
            // - indicates Samsung Wallet is not supported on this device; typically returned if the device is incompatible with Samsung Pay or if the Samsung Wallet app is not installed.
            ERROR_NOT_SUPPORTED-> {
                errorMessage = context.getString(R.string.msg_moyasar_samsung_pay_feature_is_not_supported)
                MoyasarLogger.log("samsung-pay-error","ERROR_NOT_SUPPORTED")
            }
            ERROR_NOT_FOUND-> MoyasarLogger.log("samsung-pay-error","ERROR_NOT_FOUND")
            ERROR_ALREADY_DONE-> MoyasarLogger.log("samsung-pay-error","ERROR_ALREADY_DONE")
            ERROR_NOT_ALLOWED-> MoyasarLogger.log("samsung-pay-error","ERROR_NOT_ALLOWED")
            ERROR_USER_CANCELED-> MoyasarLogger.log("samsung-pay-error","ERROR_USER_CANCELED")
            //  - tells the partner app it is using the wrong API level. To resolve the error condition, the partner app must set a valid API level.
            ERROR_PARTNER_SDK_API_LEVEL-> MoyasarLogger.log("samsung-pay-error","ERROR_PARTNER_SDK_API_LEVEL")
            ERROR_PARTNER_SERVICE_TYPE-> MoyasarLogger.log("samsung-pay-error","ERROR_PARTNER_SERVICE_TYPE")
            ERROR_INVALID_PARAMETER-> MoyasarLogger.log("samsung-pay-error","ERROR_INVALID_PARAMETER")
            ERROR_NO_NETWORK-> MoyasarLogger.log("samsung-pay-error","ERROR_NO_NETWORK")
            ERROR_SERVER_NO_RESPONSE-> MoyasarLogger.log("samsung-pay-error","ERROR_SERVER_NO_RESPONSE")
            //- indicates that Partner app information is invalid; typically the partner app is using a SDK version that is not allowed, an invalid service type, or the wrong API level.
            ERROR_PARTNER_INFO_INVALID-> MoyasarLogger.log("samsung-pay-error","ERROR_PARTNER_INFO_INVALID")
            ERROR_INITIATION_FAIL-> MoyasarLogger.log("samsung-pay-error","ERROR_INITIATION_FAIL")
            ERROR_REGISTRATION_FAIL-> MoyasarLogger.log("samsung-pay-error","ERROR_REGISTRATION_FAIL")
            ERROR_DUPLICATED_SDK_API_CALLED-> MoyasarLogger.log("samsung-pay-error","ERROR_DUPLICATED_SDK_API_CALLED")
            ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION-> MoyasarLogger.log("samsung-pay-error","ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION")
            else -> {
                MoyasarLogger.log("samsung-pay-error","SOMETHING_WRONG")
            }
        }
        Toast.makeText(
            context.applicationContext,
           errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun samsungPayPartnerInfo(): PartnerInfo {
        val bundle = Bundle().apply {
            putString(SamsungPay.PARTNER_SERVICE_TYPE, ServiceType.INAPP_PAYMENT.toString())
        }
       // TODO - THIS VALUE SHOULD BE UPDATED FROM SAMSUNG DEVELOPER CONSOLE
        return PartnerInfo("1b7da11c1d1945d1a21e3b", bundle)
    }

    private fun makeCustomSheetPaymentInfo(apiKey: String, orderNum : String): CustomSheetPaymentInfo {
        val brandList = mutableListOf(
            SpaySdk.Brand.VISA,
            SpaySdk.Brand.MASTERCARD
        )

        val amountControl = AmountBoxControl("amount-control", "SAR").apply {
            setAmountTotal(1.00, AmountConstants.FORMAT_TOTAL_PRICE_ONLY)
        }

        val customSheet = CustomSheet().apply {
            addControl(amountControl)
        }

        return CustomSheetPaymentInfo.Builder()
            .setMerchantId(apiKey)
            .setOrderNumber(orderNum)
            .setMerchantName("Checkout Hero")
            .setAllowedCardBrands(brandList)
            .setCardHolderNameEnabled(true)
            .setRecurringEnabled(false)
            .setCustomSheet(customSheet)
            .build()
    }
}