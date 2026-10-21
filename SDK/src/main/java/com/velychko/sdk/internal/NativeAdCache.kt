package com.velychko.sdk.internal

import com.velychko.sdk.NativeAdAsset
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/** Keeps one native ad per slot, so scrolling a list never triggers a reload. */
internal class NativeAdCache(private val loader: suspend () -> NativeAdAsset?) {

    private val ads = ConcurrentHashMap<String, NativeAdAsset>()
    private val loadLock = Mutex() // load slots one at a time

    suspend fun get(slotId: String): NativeAdAsset? {
        ads[slotId]?.let { return it }
        return loadLock.withLock {
            ads[slotId] ?: loader()?.also { ads[slotId] = it }
        }
    }

    fun clear() {
        ads.values.forEach { it.destroy() }
        ads.clear()
    }
}
