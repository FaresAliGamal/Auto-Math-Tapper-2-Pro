package com.math.autotapper2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // أزرار افتراضية داخل layout
        val start = findViewById<Button?>(R.id.btnStartLive)
        val stop = findViewById<Button?>(R.id.btnStopLive)

        start?.setOnClickListener {
            startService(Intent(this, LiveAnalyzeService::class.java))
        }

        stop?.setOnClickListener {
            stopService(Intent(this, LiveAnalyzeService::class.java))
        }
    }
}
