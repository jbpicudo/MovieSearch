package com.velychko.sdk.admob

data class AdMobConfig(
    val nativeAdUnitId: String,
    val interstitialAdUnitId: String,
) {
    companion object {
        /** Google's public test ad units. Never use real ad units while developing. */
        val TEST = AdMobConfig(
            nativeAdUnitId = "ca-app-pub-3940256099942544/2247696110",
            interstitialAdUnitId = "ca-app-pub-3940256099942544/1033173712",
        )
    }
}
