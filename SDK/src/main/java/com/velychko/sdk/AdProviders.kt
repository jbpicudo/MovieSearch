package com.velychko.sdk

import com.velychko.sdk.admob.AdMobConfig
import com.velychko.sdk.admob.AdMobProvider

/** Factory for built-in providers; keeps the implementation classes `internal`. */
object AdProviders {
    fun admob(config: AdMobConfig = AdMobConfig.TEST): AdProvider = AdMobProvider(config)
}
