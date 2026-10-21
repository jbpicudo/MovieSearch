package com.velychko.sdk

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.velychko.sdk.internal.ActivityResumeListener
import com.velychko.sdk.internal.AdsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Single entry point of the SDK.
 *
 * ```
 * AdsSdk.init(application, listOf(AdProviders.admob()))   // once, in Application.onCreate
 * NativeAdCard(slotId = "search_ad_1")                    // anywhere in Compose UI
 * ```
 * Interstitials need no call at all: the SDK shows one itself on the user's second return to the app.
 */
object AdsSdk {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var manager: AdsManager? = null

    @Synchronized
    fun init(application: Application, providers: List<AdProvider>) {
        if (manager != null) return
        val adsManager = AdsManager(providers).also { manager = it }

        application.registerActivityLifecycleCallbacks(
            ActivityResumeListener(onResumed = adsManager::onActivityResumed),
        )
        // ProcessLifecycleOwner reports app-level foreground/background, ignoring rotations
        // and activity-to-activity navigation.
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = adsManager.onAppForegrounded()
        })

        scope.launch { adsManager.initialize(application) }
    }

    /** Loads (or returns the cached) native ad for [slotId]; null if every provider failed. */
    suspend fun loadNativeAd(slotId: String): NativeAdAsset? = manager?.loadNativeAd(slotId)

    /** Destroys cached native ads. Call when the screen that displays them goes away. */
    fun releaseNativeAds() {
        manager?.releaseNativeAds()
    }
}
