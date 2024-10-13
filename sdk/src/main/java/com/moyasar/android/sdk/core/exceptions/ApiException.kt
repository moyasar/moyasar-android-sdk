package com.moyasar.android.sdk.core.exceptions

import com.moyasar.android.sdk.core.data.response.ErrorResponse

class ApiException(val response: ErrorResponse) : MoyasarException()
