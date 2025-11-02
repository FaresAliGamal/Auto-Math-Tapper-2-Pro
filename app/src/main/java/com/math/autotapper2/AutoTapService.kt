package com.math.autotapper2

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class AutoTapService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // لا شيء هنا حالياً
    }

    override fun onInterrupt() {}

    companion object {
        fun isEnabled(context: Context): Boolean = true
        fun performTap(context: Context, x: Int, y: Int) {
            Log.d("AutoTapService", "Simulated tap at $x,$y")
        }
    }
}
