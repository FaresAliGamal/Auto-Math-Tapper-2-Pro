
package com.math.autotapper2

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var roi: Rect = Rect(100, 200, 800, 500)
    private val stroke = Paint().apply {
        color = Color.argb(255, 98,0,238)
        style = Paint.Style.STROKE; strokeWidth = 6f; isAntiAlias = true
    }
    private val shade = Paint().apply { color = Color.argb(80, 0,0,0) }

    private var dragging = false
    private var startX = 0f; private var startY = 0f

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        c.drawRect(0f,0f,width.toFloat(),height.toFloat(), shade)
        val save = c.save()
        c.clipRect(roi)
        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        c.restoreToCount(save)
        c.drawRect(roi, stroke)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x; val y = e.y
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragging = true; startX = x; startY = y
                roi.set(x.toInt(), y.toInt(), x.toInt(), y.toInt()); invalidate()
            }
            MotionEvent.ACTION_MOVE -> if (dragging) {
                val l = min(startX, x).toInt()
                val t = min(startY, y).toInt()
                val r = max(startX, x).toInt()
                val b = max(startY, y).toInt()
                roi.set(l, t, r, b); invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { dragging = false }
        }
        return true
    }
}
