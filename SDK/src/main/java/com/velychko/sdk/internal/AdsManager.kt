package com.velychko.sdk.internal

import android.app.Activity
import android.app.Application
import com.velychko.sdk.AdProvider
import com.velychko.sdk.NativeAdAsset
import kotlin.coroutines.cancellation.CancellationException

internal const val DEFAULT_RETURNS_BEFORE_INTERSTITIAL = 2

/**
 * All ad policy lives here, independent of any concrete network:
 * provider fallback for native ads, per-slot caching and the "second return" interstitial rule.
 * Must be used from the main thread, except [loadNativeAd] which is safe to call from coroutines.
 */
internal class AdsManager(
    private val providers: List<AdProvider>,
    returnsBeforeInterstitial: Int = DEFAULT_RETURNS_BEFORE_INTERSTITIAL,
) {
    private val returnCounter = ReturnCounter(returnsBeforeInterstitial)
    private val nativeAds = NativeAdCache(loader = { loadFromProviders() })
    private var interstitialPending = false

    suspend fun initialize(application: Application) {
        providers.forEach { provider ->
            try {
                provider.initialize(application)
                provider.preloadInterstitial()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // One broken provider must not take the others down.
            }
        }
    }

    // ---- native ----

    suspend fun loadNativeAd(slotId: String): NativeAdAsset? = nativeAds.get(slotId)

    fun releaseNativeAds() = nativeAds.clear()

    /** Providers are tried in priority order; the first one that fills the slot wins. */
    private suspend fun loadFromProviders(): NativeAdAsset? {
        for (provider in providers) {
            val ad = try {
                provider.loadNativeAd()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
            if (ad != null) return ad
        }
        return null
    }

    // ---- interstitial ----

    fun onAppForegrounded() {
        if (returnCounter.onAppForegrounded()) interstitialPending = true
    }

    /**
     * The process-level "foreground" event fires before the activity is resumed,
     * so the ad is shown on the next resume. If it isn't loaded yet it is skipped, never delayed.
     */
    fun onActivityResumed(activity: Activity) {
        if (!interstitialPending) return
        interstitialPending = false

        val provider = providers.firstOrNull { it.isInterstitialReady() } ?: return
        provider.showInterstitial(activity) { provider.preloadInterstitial() }
    }
}
