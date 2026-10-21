package com.velychko.movies

import android.app.Application
import com.velychko.sdk.AdProviders
import com.velychko.sdk.AdsSdk
import com.velychko.sdk.admob.AdMobConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MoviesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Only place in :app that knows which ad providers are used.
        // Swap AdMobConfig.TEST for real ad unit ids in production.
        AdsSdk.init(
            application = this,
            providers = listOf(AdProviders.admob(AdMobConfig.TEST)),
        )
    }
}
