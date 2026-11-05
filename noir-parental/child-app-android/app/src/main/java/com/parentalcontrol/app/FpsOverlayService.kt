package com.parentalcontrol.app

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast

class FpsOverlayService : Service() {

    private var overlayView: TextView? = null
    private var windowManager: WindowManager? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {
            Toast.makeText(
                this,
                "Izin tampil di atas aplikasi lain belum diberikan.",
                Toast.LENGTH_LONG
            ).show()

            stopSelf()
            return START_NOT_STICKY
        }

        try {
            windowManager =
                getSystemService(WINDOW_SERVICE) as WindowManager

            if (overlayView != null) return START_NOT_STICKY

            overlayView = TextView(this).apply {
                text = "FPS: --"
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(0x88000000.toInt())
                setPadding(16, 8, 16, 8)
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.TOP or Gravity.START
            params.x = 20
            params.y = 100

            windowManager?.addView(overlayView, params)

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Overlay gagal ditampilkan: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {
            }
        }

        overlayView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
