
package com.math.autotapper2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SymbolUploaderActivity : AppCompatActivity() {

    private val PICK_IMAGE = 100
    private var currentSymbolKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symbol_uploader)

        fun bind(id: Int, key: String) {
            findViewById<Button>(id).setOnClickListener {
                currentSymbolKey = key
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "image/*" }
                startActivityForResult(intent, PICK_IMAGE)
            }
        }

        bind(R.id.btn_0, "0"); bind(R.id.btn_1, "1"); bind(R.id.btn_2, "2"); bind(R.id.btn_3, "3")
        bind(R.id.btn_4, "4"); bind(R.id.btn_5, "5"); bind(R.id.btn_6, "6"); bind(R.id.btn_7, "7")
        bind(R.id.btn_8, "8"); bind(R.id.btn_9, "9"); bind(R.id.btn_plus, "+"); bind(R.id.btn_minus, "-")
        bind(R.id.btn_x, "x"); bind(R.id.btn_divide, "÷"); bind(R.id.btn_equal, "=")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                TemplateStore.saveSymbol(this, currentSymbolKey, uri)
                Toast.makeText(this, "Saved symbol for $currentSymbolKey", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
