package com.moyasar.android.sdk.core.domain.entities

/**
 * Created by Mahmoud Ashraf on 11,May,2025
 */
typealias Predicate = (value: String?) -> Boolean
data class ValidationRule(val predicate: Predicate, val error: String)
