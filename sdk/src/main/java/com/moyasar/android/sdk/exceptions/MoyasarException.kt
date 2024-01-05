package com.moyasar.android.sdk.exceptions

open class MoyasarException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
}
