package com.moyasar.android.sdk.core.extensions

import com.moyasar.android.sdk.core.exceptions.ApiException
import com.moyasar.android.sdk.creditcard.presentation.model.RequestResultViewState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal fun <T> scope(
  mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
  ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
  block: suspend CoroutineScope.() -> T,
  resultBlock: suspend (RequestResultViewState<T>) -> Unit
) {
  val coroutineScope: CoroutineScope = CoroutineScope(Job() + mainDispatcher)
  coroutineScope.launch {
    val result: RequestResultViewState<T> = withContext(ioDispatcher) {
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