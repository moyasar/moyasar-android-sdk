package com.moyasar.android.sdk.core.exceptions

import java.lang.IllegalArgumentException

class InvalidConfigException(
    private val errors: Array<String>
) : IllegalArgumentException() {
    override fun toString(): String {
        return errors.joinToString(", ")
    }
}
