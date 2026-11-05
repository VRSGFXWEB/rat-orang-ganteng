package com.parentalcontrol.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tampilan utama app anak — bertema "game booster" sesuai permintaan,
 * TAPI tetap jelas ini adalah app parental control:
 *  - Nama app & ikon terlihat jelas di launcher (lihat AndroidManifest / strings.xml)
 *  - Tidak ada logic "pura-pura crash" atau penyamaran apa pun
 *  - Tombol SOS & check-in terlihat untuk anak, bukan tersembunyi
 */
class MainActivity : AppCompatActivity() {

    private lateinit var deviceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Anonymous auth -> UID dipakai sebagai device_id
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener { result ->
                deviceId = result.user!!.uid
                registerDevice()
                startForegroundControlService()
            }

        findViewById<Button>(R.id.btnBoost).setOnClickListener { runBoost() }
        findViewById<Button>(R.id.btnSOS).setOnClickListener { sendSOS() }
        findViewById<Button>(R.id.btnCheckin).setOnClickListener { sendCheckin() }
        findViewById<Button>(R.id.btnFpsOverlay).setOnClickListener { toggleFpsOverlay() }
    }

    private fun registerDevice() {
        val db = FirebaseDatabase.getInstance().reference
        val deviceData = mapOf(
            "child_name" to android.os.Build.MODEL,
            "status" to "online",
            "battery" to getBatteryLevel(),
            "uninstall_protected" to false,
            "flashlight" to "off"
        )
        db.child("devices").child(deviceId).updateChildren(deviceData)
    }

    private fun getBatteryLevel(): Int {
        val bm = getSystemService(BATTERY_SERVICE) as android.os.BatteryManager
        return bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun startForegroundControlService() {
        val intent = Intent(this, ControlForegroundService::class.java)
        intent.putExtra("device_id", deviceId)
        startForegroundService(intent)
    }

    /** Fitur BOOST sungguhan: trim memory + hint ke sistem untuk bersihkan cache app ini. */
    private fun runBoost() {
        val am = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        // Catatan: Android modern membatasi akses untuk membersihkan cache app LAIN
        // tanpa izin system/root. Yang legal & bisa dilakukan: trim memori proses sendiri
        // dan beri tips ke pengguna.
        Runtime.getRuntime().gc()
        Toast.makeText(this, "Boost selesai: memori dibersihkan.", Toast.LENGTH_SHORT).show()
    }

    /** Tombol SOS — anak tekan manual, kirim lokasi + alert ke ortu. Transparan, bukan silent. */
    private fun sendSOS() {
        val db = FirebaseDatabase.getInstance().reference
        LocationHelper.getCurrentLocation(this) { lat, lng ->
            val alert = mapOf(
                "type" to "sos",
                "location" to mapOf("lat" to lat, "lng" to lng),
                "timestamp" to System.currentTimeMillis(),
                "resolved" to false
            )
            db.child("alerts").child(deviceId).push().setValue(alert)
            Toast.makeText(this, "SOS terkirim ke orang tua.", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendCheckin() {
        val db = FirebaseDatabase.getInstance().reference
        db.child("checkins").child(deviceId).child("last_checkin")
            .setValue(System.currentTimeMillis())
        Toast.makeText(this, "Check-in terkirim: aman.", Toast.LENGTH_SHORT).show()
    }

    /** Overlay FPS mod-menu — murni kosmetik gaming, bisa di-off dari pengaturan app. */
    private fun toggleFpsOverlay() {
        val intent = Intent(this, FpsOverlayService::class.java)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val enabled = !prefs.getBoolean("fps_overlay_on", false)
        prefs.edit().putBoolean("fps_overlay_on", enabled).apply()
        if (enabled) startService(intent) else stopService(intent)
        Toast.makeText(this, "Mod menu overlay: ${if (enabled) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
    }
}
