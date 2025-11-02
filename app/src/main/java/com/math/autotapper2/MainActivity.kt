package com.math.autotapper2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnStartLive)?.setOnClickListener {
            val i = Intent(this, LiveAnalyzeService::class.java)
            startService(i)
        }

        findViewById<Button>(R.id.btnStopLive)?.setOnClickListener {
            stopService(Intent(this, LiveAnalyzeService::class.java))
        }
    }
}
