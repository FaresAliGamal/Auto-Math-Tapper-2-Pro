
package com.math.autotapper2

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent

class AutoTapService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    companion object {
        var instance: AutoTapService? = null
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }
    fun tapRect(rect: Rect) {
        val x = (rect.left + rect.right) / 2f
        val y = (rect.top + rect.bottom) / 2f
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, null, null)
    }
}
