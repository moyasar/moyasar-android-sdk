package com.moyasar.android.sdk.core.extensions


internal fun String.getResourceUrlFormated(url: String): String {
  val baseUrl = this
  return baseUrl.trimEnd('/').trimEnd() + "/" +
    url.trimStart('/').trimStart()
}