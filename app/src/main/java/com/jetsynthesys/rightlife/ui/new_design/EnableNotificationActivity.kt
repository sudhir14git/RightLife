package com.jetsynthesys.rightlife.ui.new_design

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.ui.CommonAPICall

class EnableNotificationActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContentView(R.layout.activity_enable_notification)
        val btnEnableNotification = findViewById<Button>(R.id.btn_enable_notification)

        findViewById<ImageView>(R.id.imageClose).setOnClickListener {
            sharedPreferenceManager.enableNotification = true
            startActivity(Intent(this, SyncNowActivity::class.java))
        }

        btnEnableNotification.setOnClickListener {
            checkPermission()
        }
    }


    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
                return false
            } else {
                sharedPreferenceManager.enableNotification = true
                startActivity(Intent(this, SyncNowActivity::class.java))
                finishAffinity()
                return true
            }
        } else {
            sharedPreferenceManager.enableNotification = true
            startActivity(Intent(this, SyncNowActivity::class.java))
            finishAffinity()
            // Permission not required before Android 13
            return true
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableNotificationAPICall()
            } else {
                Toast.makeText(
                    this,
                    "Notification permission denied",
                    Toast.LENGTH_SHORT
                ).show()
                sharedPreferenceManager.enableNotification = true
                startActivity(Intent(this, SyncNowActivity::class.java))
                finish()
            }
        }
    }

    private fun enableNotificationAPICall(){
        val requestBody = mapOf("pushNotification" to true)
        CommonAPICall.updateNotificationSettings(this, requestBody) { result, message ->
            showToast(message)
            finishAffinity()
            sharedPreferenceManager.enableNotification = true
            startActivity(Intent(this, SyncNowActivity::class.java))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}