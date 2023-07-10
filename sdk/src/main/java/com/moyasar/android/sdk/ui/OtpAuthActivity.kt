package com.moyasar.android.sdk.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.data.StcPaySheetViewModel
import kotlinx.parcelize.Parcelize

class OtpAuthActivity : AppCompatActivity() {

    private val viewModel: StcPaySheetViewModel by viewModels()
    private var otpEditText: EditText? = null
    private val authUrl: String? by lazy {
        intent.getStringExtra(OtpAuthContract.EXTRA_AUTH_URL)
    }

    data class OtpRequest(val stcPayTransactionUrl: String?, val otp: String) {

    }

    companion object {
        val RETURN_HOST = "sdk.moyasar.com";
        val RETURN_URL = "https://${RETURN_HOST}/payments"
    }

    sealed class AuthResult : Parcelable {
        @Parcelize
        data class Completed(val id: String, val status: String, val message: String) : AuthResult()

        @Parcelize
        data class Failed(val error: String? = null) : AuthResult()

        @Parcelize
        object Canceled : AuthResult()
    }
}