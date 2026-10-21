package com.velychko.sdk.internal

import android.app.Activity
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AdsManagerTest {

    private val activity = mockk<Activity>()

    /** Simulates: process comes to foreground, then the activity resumes. */
    private fun AdsManager.comeToForeground() {
        onAppForegrounded()
        onActivityResumed(activity)
    }

    // ---------- interstitial ----------

    @Test
    fun `interstitial is not shown on launch or on the first return`() {
        val provider = FakeAdProvider()
        val manager = AdsManager(listOf(provider))

        manager.comeToForeground() // launch
        manager.comeToForeground() // return 1

        assertEquals(0, provider.interstitialShown)
    }

    @Test
    fun `interstitial is shown on the second return, and only once`() {
        val provider = FakeAdProvider()
        val manager = AdsManager(listOf(provider))

        repeat(3) { manager.comeToForeground() } // launch, return 1, return 2
        assertEquals(1, provider.interstitialShown)

        repeat(3) { manager.comeToForeground() } // returns 3, 4, 5
        assertEquals(1, provider.interstitialShown)
    }

    @Test
    fun `resuming an activity without a return never shows an ad`() {
        val provider = FakeAdProvider()
        val manager = AdsManager(listOf(provider))

        repeat(5) { manager.onActivityResumed(activity) } // e.g. navigation between activities

        assertEquals(0, provider.interstitialShown)
    }

    @Test
    fun `interstitial that is not loaded is skipped, not postponed`() {
        val provider = FakeAdProvider(interstitialReady = false)
        val manager = AdsManager(listOf(provider))

        repeat(3) { manager.comeToForeground() }
        provider.interstitialReady = true
        manager.onActivityResumed(activity)

        assertEquals(0, provider.interstitialShown)
    }

    @Test
    fun `next interstitial is preloaded after one is shown`() {
        val provider = FakeAdProvider()
        val manager = AdsManager(listOf(provider))

        repeat(3) { manager.comeToForeground() }

        assertEquals(1, provider.preloadCalls)
    }

    @Test
    fun `the first provider with a ready interstitial is used`() {
        val notReady = FakeAdProvider(id = "a", interstitialReady = false)
        val ready = FakeAdProvider(id = "b")
        val manager = AdsManager(listOf(notReady, ready))

        repeat(3) { manager.comeToForeground() }

        assertEquals(0, notReady.interstitialShown)
        assertEquals(1, ready.interstitialShown)
    }

    // ---------- native ----------

    @Test
    fun `native ad falls back to the next provider`() = runTest {
        val fallbackAd = FakeNativeAd()
        val primary = FakeAdProvider(id = "primary", nativeAd = null)
        val fallback = FakeAdProvider(id = "fallback", nativeAd = fallbackAd)
        val manager = AdsManager(listOf(primary, fallback))

        val ad = manager.loadNativeAd("slot")

        assertSame(fallbackAd, ad)
        assertEquals(1, primary.nativeLoadCalls)
    }

    @Test
    fun `native ad is null when every provider fails`() = runTest {
        val manager = AdsManager(listOf(FakeAdProvider(nativeAd = null), FakeAdProvider(nativeAd = null)))

        assertNull(manager.loadNativeAd("slot"))
    }

    @Test
    fun `native ad is loaded once per slot`() = runTest {
        val provider = FakeAdProvider()
        val manager = AdsManager(listOf(provider))

        val first = manager.loadNativeAd("slot_1")
        val again = manager.loadNativeAd("slot_1")
        manager.loadNativeAd("slot_3")

        assertSame(first, again)
        assertEquals(2, provider.nativeLoadCalls)
    }

    @Test
    fun `releasing native ads destroys them and allows reloading`() = runTest {
        val ad = FakeNativeAd()
        val provider = FakeAdProvider(nativeAd = ad)
        val manager = AdsManager(listOf(provider))

        manager.loadNativeAd("slot_1")
        manager.releaseNativeAds()
        manager.loadNativeAd("slot_1")

        assertTrue(ad.destroyed)
        assertEquals(2, provider.nativeLoadCalls)
    }
}
