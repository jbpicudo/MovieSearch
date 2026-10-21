package com.velychko.sdk.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.velychko.sdk.AdsSdk
import com.velychko.sdk.NativeAdAsset

private sealed interface AdState {
    data object Loading : AdState
    data object Failed : AdState
    data class Loaded(val ad: NativeAdAsset) : AdState
}

/**
 * Drop-in native ad. Reserves space while loading and renders nothing if no provider could fill the slot.
 * [slotId] identifies the position, so the same ad survives scrolling out of / back into view.
 */
@Composable
fun NativeAdCard(slotId: String, modifier: Modifier = Modifier) {
    var state by remember(slotId) { mutableStateOf<AdState>(AdState.Loading) }

    LaunchedEffect(slotId) {
        val ad = AdsSdk.loadNativeAd(slotId)
        state = if (ad != null) AdState.Loaded(ad) else AdState.Failed
    }

    when (val current = state) {
        AdState.Loading -> Spacer(modifier.fillMaxWidth().height(72.dp))
        AdState.Failed -> Unit
        is AdState.Loaded -> Card(modifier.fillMaxWidth()) { current.ad.Content() }
    }
}
