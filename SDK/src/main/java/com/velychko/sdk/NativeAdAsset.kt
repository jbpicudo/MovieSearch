package com.velychko.sdk

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A loaded native ad, independent of the network that produced it.
 * Each provider knows how to render (and track impressions/clicks for) its own ads.
 */
interface NativeAdAsset {
    @Composable
    fun Content(modifier: Modifier = Modifier)

    fun destroy()
}
