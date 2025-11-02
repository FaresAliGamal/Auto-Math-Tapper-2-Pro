package com.math.autotapper2

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.View

class RoiOverlayActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(OverlayView(this))
    }

    class OverlayView(context: Context) : View(context) {
        private val paint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        private val rect = RectF(100f, 200f, 600f, 700f)
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawRect(rect, paint)
        }
    }
}
