
package com.parentalcontrol.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private var deviceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnBoost).setOnClickListener {
            runBoost()
        }

        findViewById<Button>(R.id.btnSOS).setOnClickListener {
            sendSOS()
        }

        findViewById<Button>(R.id.btnCheckin).setOnClickListener {
            sendCheckin()
        }

        findViewById<Button>(R.id.btnFpsOverlay).setOnClickListener {
            toggleFpsOverlay()
        }

        FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener { result ->
                val uid = result.user?.uid

                if (uid == null) {
                    showToast("Gagal mendapatkan ID perangkat")
                    return@addOnSuccessListener
                }

                deviceId = uid
                registerDevice(uid)
                startForegroundControlService(uid)
            }
            .addOnFailureListener { error ->
                showToast("Login Firebase gagal: ${error.message}")
            }
    }

    private fun requireDeviceId(): String? {
        val uid = deviceId

        if (uid == null) {
            showToast("Tunggu, koneksi Firebase sedang disiapkan.")
        }

        return uid
    }

    private fun registerDevice(uid: String) {
        val batteryManager =
            getSystemService(BATTERY_SERVICE) as android.os.BatteryManager

        val deviceData = mapOf(
            "child_name" to android.os.Build.MODEL,
            "status" to "online",
            "battery" to batteryManager.getIntProperty(
                android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY
            ),
            "uninstall_protected" to false,
            "flashlight" to "off"
        )

        FirebaseDatabase.getInstance().reference
            .child("devices")
            .child(uid)
            .updateChildren(deviceData)
    }

    private fun startForegroundControlService(uid: String) {
        val serviceIntent = Intent(
            this,
            ControlForegroundService::class.java
        ).apply {
            putExtra("device_id", uid)
        }

        startForegroundService(serviceIntent)
    }

    private fun runBoost() {
        Runtime.getRuntime().gc()

        showToast("Boost selesai.")
    }

    private fun sendSOS() {
        val uid = requireDeviceId() ?: return

        LocationHelper.getCurrentLocation(this) { lat, lng ->
            val alert = mapOf(
                "type" to "sos",
                "location" to mapOf(
                    "lat" to lat,
                    "lng" to lng
                ),
                "timestamp" to System.currentTimeMillis(),
                "resolved" to false
            )

            FirebaseDatabase.getInstance().reference
                .child("alerts")
                .child(uid)
                .push()
                .setValue(alert)
                .addOnSuccessListener {
                    showToast("SOS terkirim ke orang tua.")
                }
                .addOnFailureListener {
                    showToast("Gagal mengirim SOS.")
                }
        }
    }

    private fun sendCheckin() {
        val uid = requireDeviceId() ?: return

        FirebaseDatabase.getInstance().reference
            .child("checkins")
            .child(uid)
            .child("last_checkin")
            .setValue(System.currentTimeMillis())
            .addOnSuccessListener {
                showToast("Check-in terkirim: aman.")
            }
            .addOnFailureListener {
                showToast("Check-in gagal.")
            }
    }

    private fun toggleFpsOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            val permissionIntent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )

            startActivity(permissionIntent)

            showToast(
                "Aktifkan izin Tampil di atas aplikasi lain."
            )
            return
        }

        val preferences = getSharedPreferences(
            "overlay",
            MODE_PRIVATE
        )

        val serviceIntent = Intent(
            this,
            FpsOverlayService::class.java
        )

        val isRunning = preferences.getBoolean("running", false)

        if (isRunning) {
            stopService(serviceIntent)
            preferences.edit()
                .putBoolean("running", false)
                .apply()

            showToast("FPS Overlay dimatikan.")
        } else {
            startService(serviceIntent)
            preferences.edit()
                .putBoolean("running", true)
                .apply()

            showToast("FPS Overlay diaktifkan.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
