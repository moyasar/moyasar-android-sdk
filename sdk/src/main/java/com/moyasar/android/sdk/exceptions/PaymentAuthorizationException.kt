package com.moyasar.android.sdk.exceptions

class PaymentAuthorizationException : PaymentSheetException {
    constructor() : super()
    constructor(message: String?) : super(message)
}
