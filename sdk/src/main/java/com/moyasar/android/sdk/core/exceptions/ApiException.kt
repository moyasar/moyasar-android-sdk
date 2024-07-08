package com.moyasar.android.sdk.core.exceptions

import com.moyasar.android.sdk.data.models.ErrorResponse

class ApiException(val response: ErrorResponse) : MoyasarException()
