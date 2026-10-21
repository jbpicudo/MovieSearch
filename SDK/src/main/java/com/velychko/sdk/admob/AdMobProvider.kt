package com.velychko.sdk.admob

import android.app.Activity
import android.app.Application
import android.content.Context
import com.velychko.sdk.AdProvider
import com.velychko.sdk.NativeAdAsset
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

internal class AdMobProvider(private val config: AdMobConfig) : AdProvider {

    override val id = "admob"

    private lateinit var appContext: Context
    private val initialized = CompletableDeferred<Unit>()

    private var interstitial: InterstitialAd? = null
    private var interstitialLoading = false

    override suspend fun initialize(application: Application) {
        appContext = application
        suspendCancellableCoroutine { continuation ->
            MobileAds.initialize(application) { continuation.resume(Unit) }
        }
        initialized.complete(Unit)
    }

    // ---- native ----

    override suspend fun loadNativeAd(): NativeAdAsset? {
        initialized.await()
        // AdLoader must be used from the main thread.
        return withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                AdLoader.Builder(appContext, config.nativeAdUnitId)
                    .forNativeAd { nativeAd ->
                        if (continuation.isActive) {
                            continuation.resume(AdMobNativeAsset(nativeAd))
                        } else {
                            nativeAd.destroy()
                        }
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    })
                    .build()
                    .loadAd(AdRequest.Builder().build())
            }
        }
    }

    // ---- interstitial ----

    override fun preloadInterstitial() {
        if (interstitial != null || interstitialLoading) return
        interstitialLoading = true
        InterstitialAd.load(
            appContext,
            config.interstitialAdUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    interstitialLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                    interstitialLoading = false
                }
            },
        )
    }

    override fun isInterstitialReady(): Boolean = interstitial != null

    override fun showInterstitial(activity: Activity, onFinished: () -> Unit) {
        val ad = interstitial ?: return onFinished()
        interstitial = null // an interstitial can be shown only once
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() = onFinished()
            override fun onAdFailedToShowFullScreenContent(error: AdError) = onFinished()
        }
        ad.show(activity)
    }
}
