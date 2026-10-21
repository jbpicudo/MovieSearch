package com.velychko.sdk.internal

/**
 * Counts how often the app comes to the foreground.
 * The first foreground event is the launch itself, not a "return".
 */
internal class ReturnCounter(private val triggerOnReturn: Int) {

    private var foregroundEvents = 0

    /** @return true exactly once: on the [triggerOnReturn]-th return to the app. */
    fun onAppForegrounded(): Boolean {
        foregroundEvents++
        val returns = foregroundEvents - 1
        return returns == triggerOnReturn
    }
}
