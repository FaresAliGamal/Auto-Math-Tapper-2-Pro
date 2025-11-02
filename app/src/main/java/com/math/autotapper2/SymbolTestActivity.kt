package com.math.autotapper2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/**
 * شاشة اختبار بسيطة لالتقاط صورة رمز باستخدام SAF (ACTION_OPEN_DOCUMENT)
 * وتثبيت إذن القراءة (persistable) بحيث لا يحدث SecurityException.
 */
class SymbolTestActivity : AppCompatActivity() {

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            Toast.makeText(this, "لم يتم اختيار صورة", Toast.LENGTH_SHORT).show()
            finish()
            return@registerForActivityResult
        }

        // خُد إذن القراءة الدائم للصورة المختارة (مسموح فقط مع ACTION_OPEN_DOCUMENT)
        try {
            val takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION)
            contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (_: SecurityException) {
            // لو الـURI غير قابل للتثبيت، لا مشكلة—هنستخدمه مباشرة.
        }

        // جرّب فتح Stream آمن بدون أي كراش
        try {
            contentResolver.openInputStream(uri)?.use {
                // هنا ممكن تفك Bitmap أو تمررها للـTemplateStore حسب ما تحتاج.
                // بنكتفي باختبار الوصول بدون مشاكل.
            }
            Toast.makeText(this, "تم استيراد الصورة بنجاح ✅", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "تعذر فتح الصورة: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            finish() // ارجع للشاشة السابقة بعد الاختيار
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // افتح منتقي الملفات مباشرة
        val mimeTypes = arrayOf("image/png", "image/jpeg", "image/webp")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            // تلميح لعرض الصور فقط
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, null as Uri?)
            }
        }
        pickImage.launch(mimeTypes)
    }
}
