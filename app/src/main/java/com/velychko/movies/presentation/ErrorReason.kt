package com.velychko.movies.presentation

import java.io.IOException

enum class ErrorReason { NETWORK, UNKNOWN }

fun Throwable?.toErrorReason(): ErrorReason = when (this) {
    is IOException -> ErrorReason.NETWORK
    else -> ErrorReason.UNKNOWN
}
