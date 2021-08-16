package com.moyasar.android.sdk.exceptions

class ApiException(val type: String, val errors: List<String>?) : MoyasarException()
