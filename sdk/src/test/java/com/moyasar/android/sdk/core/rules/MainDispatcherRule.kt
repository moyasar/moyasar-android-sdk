package com.moyasar.android.sdk.core.rules

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/// Used for test function that using Android Main Looper
class MainDispatcherRule(private val dispatcher: TestDispatcher = StandardTestDispatcher()) :
  TestWatcher() {

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun starting(description: Description) {
    super.starting(description)
    Dispatchers.setMain(dispatcher)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun finished(description: Description) {
    super.finished(description)
    Dispatchers.resetMain()
  }
}