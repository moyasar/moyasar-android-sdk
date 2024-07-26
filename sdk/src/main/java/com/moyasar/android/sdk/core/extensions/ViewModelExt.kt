package com.moyasar.android.sdk.core.extensions

import android.arch.lifecycle.ViewModel
import com.moyasar.android.sdk.core.exceptions.ApiException
import com.moyasar.android.sdk.presentation.model.RequestResultViewState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal fun <T> ViewModel.scope(
  coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Main),
  ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
  block: suspend CoroutineScope.() -> T,
  resultBlock: suspend (RequestResultViewState)-> Unit
) {
  coroutineScope.launch {
    val result: RequestResultViewState = withContext(ioDispatcher) {
      try {
        val response: T = block()
        RequestResultViewState.Success(response)
      } catch (e: ApiException) {
        RequestResultViewState.Failure(e)
      } catch (e: Exception) {
        RequestResultViewState.Failure(e)
      }
    }
    resultBlock.invoke(result)
  }
}