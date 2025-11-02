package com.math.autotapper2

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PickRoiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_roi)

        val overlay = findViewById<OverlayView>(R.id.overlayView)
        overlay.roi = ROIStore.load(this)

        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            setResult(Activity.RESULT_CANCELED); finish()
        }
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            ROIStore.save(this, overlay.roi)
            setResult(Activity.RESULT_OK); finish()
        }
    }
}
