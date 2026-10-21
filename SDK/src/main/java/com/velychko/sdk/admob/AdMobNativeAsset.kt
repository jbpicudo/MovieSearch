package com.velychko.sdk.admob

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isVisible
import com.velychko.sdk.NativeAdAsset
import com.velychko.sdk.R
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/** AdMob native ads must be rendered inside a [NativeAdView] so impressions and clicks are tracked. */
internal class AdMobNativeAsset(private val ad: NativeAd) : NativeAdAsset {

    @SuppressLint("InflateParams")
    @Composable
    override fun Content(modifier: Modifier) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                LayoutInflater.from(context).inflate(R.layout.sdk_native_ad, null) as NativeAdView
            },
            update = { view -> view.bind(ad) },
        )
    }

    override fun destroy() = ad.destroy()
}

private fun NativeAdView.bind(ad: NativeAd) {
    headlineView = findViewById<TextView>(R.id.sdk_ad_headline).apply {
        text = ad.headline
    }
    bodyView = findViewById<TextView>(R.id.sdk_ad_body).apply {
        text = ad.body
        isVisible = ad.body != null
    }
    callToActionView = findViewById<Button>(R.id.sdk_ad_cta).apply {
        text = ad.callToAction
        isVisible = ad.callToAction != null
    }
    iconView = findViewById<ImageView>(R.id.sdk_ad_icon).apply {
        setImageDrawable(ad.icon?.drawable)
        isVisible = ad.icon != null
    }
    mediaView = findViewById<MediaView>(R.id.sdk_ad_media).apply {
        mediaContent = ad.mediaContent
        isVisible = ad.mediaContent != null
    }
    setNativeAd(ad) // must be last
}
