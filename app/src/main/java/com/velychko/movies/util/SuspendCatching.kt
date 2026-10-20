package com.velychko.movies.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * Like [runCatching], but never swallows [CancellationException],
 * so structured concurrency keeps working.
 */
inline fun <T> suspendCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
