package com.math.autotapper2

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.view.*
import android.widget.Button

class RoiOverlayActivity : Activity() {

    private lateinit var overlayView: OverlayView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlayView = OverlayView(this)
        setContentView(overlayView)
    }

    inner class OverlayView(context: Context) : View(context) {
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
