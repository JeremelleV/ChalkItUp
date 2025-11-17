package com.example.chalkitup.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * A custom lifecycle observer to listen for app resume and pause events.
 *
 * This observer is used to detect when the app comes back to the foreground (onResume)
 * and optionally handle actions when the app is paused (onPause). It triggers the provided
 * `onAppResumed` callback when the app is resumed.
 *
 * @param onAppResumed A callback function that will be triggered when the app resumes (comes to the foreground).
 */
class AppLifecycleObserver(
    private val onAppResumed: () -> Unit // The callback function to call when the app resumes
) : DefaultLifecycleObserver {

    /**
     * Called when the app comes to the foreground (onResume).
     *
     * This method is triggered when the app is resumed, and it will invoke the provided
     * `onAppResumed` callback to notify the rest of the app that the app is active again.
     *
     * @param owner The LifecycleOwner that this observer is observing (usually an activity or fragment).
     */
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        // Trigger when the app comes back to the foreground
        onAppResumed()
    }

    /**
     * Called when the app goes into the background (onPause).
     *
     * Optional: add logic here if needed to handle events when the app is paused,
     * such as saving data or stopping background tasks. Currently, no specific action is taken.
     *
     * @param owner The LifecycleOwner that this observer is observing (usually an activity or fragment).
     */
    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        // Optional: Handle any logic when the app is paused (currently unused).
    }
}
