package com.moyasar.android.sdk.presentation.viewmodel

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.viewmodel.FormValidator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class FormValidatorTest {

  @get:Rule
  val instantExecutorRule = InstantTaskExecutorRule()

  private lateinit var application: Application
  private lateinit var formValidator: FormValidator

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    application = Mockito.mock(Application::class.java)
    Mockito.`when`(application.getString(R.string.name_is_required)).thenReturn("Name is required")
    Mockito.`when`(application.getString(R.string.only_english_alpha)).thenReturn("Only English alphabet characters are allowed")
    Mockito.`when`(application.getString(R.string.both_names_required)).thenReturn("Both names are required")
    Mockito.`when`(application.getString(R.string.card_number_required)).thenReturn("Card number is required")
    Mockito.`when`(application.getString(R.string.invalid_card_number)).thenReturn("Invalid card number")
    Mockito.`when`(application.getString(R.string.unsupported_network)).thenReturn("Unsupported network")
    Mockito.`when`(application.getString(R.string.cvc_required)).thenReturn("CVC is required")
    Mockito.`when`(application.getString(R.string.invalid_cvc)).thenReturn("Invalid CVC")
    Mockito.`when`(application.getString(R.string.expiry_is_required)).thenReturn("Expiry date is required")
    Mockito.`when`(application.getString(R.string.invalid_expiry)).thenReturn("Invalid expiry date")
    Mockito.`when`(application.getString(R.string.expired_card)).thenReturn("Card is expired")
    Mockito.`when`(application.getString(R.string.mobile_number_is_required)).thenReturn("Mobile is required")
    Mockito.`when`(application.getString(R.string.invalid_mobile_number)).thenReturn("Mobile is invalid")
    Mockito.`when`(application.getString(R.string.invalid_stc_pay_otp)).thenReturn("OTP is invalid")

    formValidator = FormValidator(application)
  }

  @Test
  fun testNameValidation() {
    formValidator.name.value = ""
    assertFalse(formValidator.nameValidator.isValid())
    formValidator.name.value = "John"
    assertFalse(formValidator.nameValidator.isValid())
    formValidator.name.value = "John Doe"
    assertTrue(formValidator.nameValidator.isValid())
  }

  @Test
  fun testNumberValidation() {
    formValidator.number.value = ""
    assertFalse(formValidator.numberValidator.isValid())
    formValidator.number.value = "1234567812345670" // assuming this is an invalid Luhn number
    assertFalse(formValidator.numberValidator.isValid())
    formValidator.number.value = "4111111111111111" // assuming this is a valid Luhn number
    MoyasarAppContainer.paymentRequest = PaymentRequest(
      amount = 5,
      currency = "SAR",
      description = "Sample Android SDK Payment",
      apiKey = "pk_test_vcFUHJDBwiyRu4Bd3hFuPpTnRPY4gp2ssYdNJMY3",
      baseUrl = "https://api.moyasar.com",
      manual = false,
      metadata = mapOf(
        "order_id" to "order_123"
      ),
      createSaveOnlyToken = false
    )
    assertTrue(formValidator.numberValidator.isValid())
  }

  @Test
  fun testCvcValidation() {
    formValidator.cvc.value = ""
    assertFalse(formValidator.cvcValidator.isValid())
    formValidator.cvc.value = "12"
    assertFalse(formValidator.cvcValidator.isValid())
    formValidator.cvc.value = "123"
    assertTrue(formValidator.cvcValidator.isValid())
    formValidator.cvc.value = "1234"
    assertTrue(formValidator.cvcValidator.isValid())
  }

  @Test
  fun testExpiryValidation() {
    formValidator.expiry.value = ""
    assertFalse(formValidator.expiryValidator.isValid())
    formValidator.expiry.value = "12/25" // assuming this is a valid expiry
    assertTrue(formValidator.expiryValidator.isValid())
    formValidator.expiry.value = "12/18" // assuming this is an expired date
    assertFalse(formValidator.expiryValidator.isValid())
  }

  @Test
  fun testFormValidation() {
    formValidator.name.value = "John Doe"
    formValidator.number.value = "4111111111111111" // Example valid card number
    formValidator.cvc.value = "123"
    formValidator.expiry.value = "12/25" // Example valid expiry date
    MoyasarAppContainer.paymentRequest = PaymentRequest(
      amount = 5,
      currency = "SAR",
      description = "Sample Android SDK Payment",
      apiKey = "pk_test_vcFUHJDBwiyRu4Bd3hFuPpTnRPY4gp2ssYdNJMY3",
      baseUrl = "https://api.moyasar.com",
      manual = false,
      metadata = mapOf(
        "order_id" to "order_123"
      ),
      createSaveOnlyToken = false
    )
    assertTrue(formValidator.validate())
  }
}
