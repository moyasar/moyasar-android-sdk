package com.moyasar.android.sdk.core.exceptions

open class MoyasarException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
}
