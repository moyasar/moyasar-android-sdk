package com.moyasar.android.sdk.core.extensions

import android.util.Base64
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.nio.charset.Charset

fun HttpURLConnection.postJson(body: Any): Response {
    val gson = Gson()

    try {
        this.setRequestProperty("Content-Type", "application/json; utf-8")
        this.setRequestProperty("Accept", "application/json")
        this.requestMethod = "POST"
        this.doOutput = true
        this.doInput = true
        this.setChunkedStreamingMode(0)

        this.outputStream.use { os ->
            val input: ByteArray = gson.toJson(body).toByteArray(Charset.forName("utf-8"))
            os.write(input, 0, input.size)
        }

        val responseCode = this.responseCode
        val stream = if (responseCode < 300) this.inputStream else this.errorStream
        val text = StringBuilder()

        BufferedReader(InputStreamReader(stream, "utf-8")).use { br ->
            var responseLine: String?
            while (br.readLine().also { responseLine = it } != null) {
                text.append(responseLine!!.trim { it <= ' ' })
            }
        }

        return Response(
            responseCode,
            text.toString(),
            this.headerFields
        )
    } finally {
        this.disconnect()
    }
}

fun HttpURLConnection.setBasicAuth(username: String, password: String) {
    val encodedKey = Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
    this.setRequestProperty("Authorization", "Basic $encodedKey");
}
