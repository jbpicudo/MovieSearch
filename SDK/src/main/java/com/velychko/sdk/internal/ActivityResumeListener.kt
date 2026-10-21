package com.velychko.sdk.internal

import android.app.Activity
import android.app.Application
import android.os.Bundle

/** Reports resumed host activities; ignores the ad network's own full-screen activities. */
internal class ActivityResumeListener(
    private val onResumed: (Activity) -> Unit,
) : Application.ActivityLifecycleCallbacks {

    override fun onActivityResumed(activity: Activity) {
        if (!activity.javaClass.name.startsWith(AD_ACTIVITY_PACKAGE)) onResumed(activity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    private companion object {
        const val AD_ACTIVITY_PACKAGE = "com.google.android.gms.ads"
    }
}
