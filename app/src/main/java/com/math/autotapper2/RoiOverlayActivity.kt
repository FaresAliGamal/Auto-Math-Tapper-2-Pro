package com.math.autotapper2

import android.graphics.*
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.max
import kotlin.math.min

/**
 * شاشة كاملة شفافة مع مستطيل ROI قابل للسحب والتحجيم (مقابض).
 * حفظ النتيجة في ROIStore ثم finish().
 */
class RoiOverlayActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        super.onCreate(savedInstanceState)
        setContentView(RoiView(this) { rect ->
            ROIStore.set(this, rect)
            finish()
        })
    }

    class RoiView(
        context: android.content.Context,
        private val onDone: (Rect) -> Unit
    ): View(context) {

        private var rect = ROIStore.get(context)
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            color = Color.MAGENTA
        }
        private val shade = Paint().apply { color = 0x66000000 }
        private val handle = Paint().apply { color = Color.WHITE }
        private var dragging = false
        private var resizing = false
        private val touchSlop = 24

        override fun onDraw(c: Canvas) {
            super.onDraw(c)
            // ظل
            c.drawRect(0f,0f,width.toFloat(),height.toFloat(), shade)
            // إطار
            c.drawRect(rect, paint)
            // مقابض
            val s=18f
            c.drawCircle(rect.left.toFloat(), rect.top.toFloat(), s, handle)
            c.drawCircle(rect.right.toFloat(), rect.bottom.toFloat(), s, handle)
        }

        override fun onTouchEvent(e: MotionEvent): Boolean {
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val nearLT = (Math.abs(e.x - rect.left) < touchSlop && Math.abs(e.y - rect.top) < touchSlop)
                    val nearRB = (Math.abs(e.x - rect.right) < touchSlop && Math.abs(e.y - rect.bottom) < touchSlop)
                    resizing = nearLT || nearRB
                    dragging = rect.contains(e.x.toInt(), e.y.toInt()) && !resizing
                }
                MotionEvent.ACTION_MOVE -> {
                    if (resizing) {
                        if (e.x < rect.centerX()) { rect.set( e.x.toInt() } else {  e.x.toInt() }
                        if (e.y < rect.centerY()) {  e.y.toInt() } else {  e.y.toInt() }
                        normalize()
                        invalidate()
                    } else if (dragging) {
                        val w = rect.width()
                        val h = rect.height()
                        rect.offsetTo((e.x - w/2).toInt(), (e.y - h/2).toInt())
                        clampInside()
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    ROIStore.set(context, rect)
                    onDone(rect)
                }
            }
            return true
        }

        private fun normalize() {
            val l = min(rect.left, rect.right)
            val r = max(rect.left, rect.right)
            val t = min(rect.top, rect.bottom)
            val b = max(rect.top, rect.bottom)
            rect.set(l,t,r,b)
            clampInside()
        }
        private fun clampInside() {
            rect.set( rect.left.coerceAtLeast(0)
             rect.top.coerceAtLeast(0)
             rect.right.coerceAtMost(width)
             rect.bottom.coerceAtMost(height)
        }
    }
}
