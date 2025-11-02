
package com.math.autotapper2

import android.content.Context
import android.net.Uri
import androidx.preference.PreferenceManager

object TemplateStore {
    fun saveSymbol(context: Context, symbol: String, uri: Uri) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString("symbol_uri_$symbol", uri.toString()).apply()
    }
    fun loadSymbol(context: Context, symbol: String): Uri? {
        val s = PreferenceManager.getDefaultSharedPreferences(context).getString("symbol_uri_$symbol", null)
        return s?.let { Uri.parse(it) }
    }
    fun listSymbols(): List<String> = listOf("0","1","2","3","4","5","6","7","8","9","+","-","x","÷","=")
}
