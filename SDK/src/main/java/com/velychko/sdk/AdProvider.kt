package com.velychko.sdk

import android.app.Activity
import android.app.Application

/**
 * One ad network (AdMob, AppLovin, Meta, ...).
 *
 * To add a provider: implement this interface and pass the instance to [AdsSdk.init].
 * Nothing else in the SDK or in the app has to change.
 */
interface AdProvider {
    val id: String

    suspend fun initialize(application: Application)

    /** Returns null when no ad could be loaded, so the SDK can fall back to the next provider. */
    suspend fun loadNativeAd(): NativeAdAsset?

    fun preloadInterstitial()
    fun isInterstitialReady(): Boolean

    /** Must only be called when [isInterstitialReady]. [onFinished] runs when the ad is dismissed or fails to show. */
    fun showInterstitial(activity: Activity, onFinished: () -> Unit)
}
