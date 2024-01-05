package com.moyasar.android.sdk.exceptions

import com.moyasar.android.sdk.payment.models.ErrorResponse

class ApiException(val response: ErrorResponse) : MoyasarException()
