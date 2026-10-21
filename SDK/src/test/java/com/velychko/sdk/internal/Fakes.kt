package com.velychko.sdk.internal

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.velychko.sdk.AdProvider
import com.velychko.sdk.NativeAdAsset

class FakeNativeAd : NativeAdAsset {
    var destroyed = false

    @Composable
    override fun Content(modifier: Modifier) = Unit

    override fun destroy() {
        destroyed = true
    }
}

class FakeAdProvider(
    override val id: String = "fake",
    private val nativeAd: NativeAdAsset? = FakeNativeAd(),
    var interstitialReady: Boolean = true,
) : AdProvider {
    var nativeLoadCalls = 0
    var interstitialShown = 0
    var preloadCalls = 0

    override suspend fun initialize(application: Application) = Unit

    override suspend fun loadNativeAd(): NativeAdAsset? {
        nativeLoadCalls++
        return nativeAd
    }

    override fun preloadInterstitial() {
        preloadCalls++
    }

    override fun isInterstitialReady() = interstitialReady

    override fun showInterstitial(activity: Activity, onFinished: () -> Unit) {
        interstitialShown++
        onFinished()
    }
}
