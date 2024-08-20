package com.moyasar.android.sdk.core.extensions

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection

class HttpURLConnectionExtensionsTest {

  @Test
  fun `test postJson successfully`() {
    // Mock HttpURLConnection
    val connection = mock(HttpURLConnection::class.java)
    val responseCode = 200
    val responseText = "{\"key\":\"value\"}"

    // Setup mock behaviors
    `when`(connection.responseCode).thenReturn(responseCode)
    `when`(connection.inputStream).thenReturn(responseText.byteInputStream())
    `when`(connection.errorStream).thenReturn(null)

    // Mock output stream
    val outputStream = ByteArrayOutputStream()
    `when`(connection.outputStream).thenReturn(outputStream)

    // Initialize Gson and call the method
    val gson = Gson()
    val body = mapOf("key" to "value")

    // Call the postJson method
    val response = connection.postJson(body, gson)

    // Assertions
    assertEquals(responseCode, response.statusCode)
    assertEquals(responseText, response.text)
    assertEquals(emptyMap<String, List<String>>(), response.headers)
  }


  @Test
  fun `test postJson handles errors`() {
    // Mock HttpURLConnection
    val connection = mock(HttpURLConnection::class.java)
    val responseCode = 500
    val responseText = "Server Error"

    // Mock the response
    `when`(connection.responseCode).thenReturn(responseCode)
    `when`(connection.errorStream).thenReturn(responseText.byteInputStream())
    `when`(connection.inputStream).thenReturn(null)

    // Mock output stream
    val outputStream = mock(OutputStream::class.java)
    `when`(connection.outputStream).thenReturn(outputStream)

    // Initialize Gson and call the method
    val gson = Gson()
    val body = mapOf("key" to "value")

    // Create a response instance using the postJson function
    val response = connection.postJson(body, gson)

    // Assertions
    assertEquals(responseCode, response.statusCode)
    assertEquals(responseText, response.text)
    assertEquals(mapOf<String, List<String>>(), response.headers)
  }
}

