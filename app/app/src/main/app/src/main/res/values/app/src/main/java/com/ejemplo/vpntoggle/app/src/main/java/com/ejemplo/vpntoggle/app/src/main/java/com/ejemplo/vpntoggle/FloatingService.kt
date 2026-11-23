package com.ejemplo.vpntoggle

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import kotlin.math.abs
import kotlin.math.hypot

class FloatingService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var vista: BotonFlotante
    private lateinit var params: WindowManager.LayoutParams
    private var activo = false
    private var tamano = 140

    override fun onCreate() {
        super.onCreate()
        iniciarForeground()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        crearBoton()
    }

    private fun iniciarForeground() {
        val canal = NotificationChannel(
            "floating", "Botón flotante", NotificationManager.IMPORTANCE_MIN
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(canal)

        val notif = NotificationCompat.Builder(this, "floating")
            .setContentTitle("VPN Toggle")
            .setContentText("Botón flotante activo")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()

        startForeground(1, notif)
    }

    private fun crearBoton() {
        vista = BotonFlotante(this)
        vista.activo = false

        params = WindowManager.LayoutParams(
            tamano, tamano,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }

        vista.setOnTouchListener(TouchHandler())
        wm.addView(vista, params)
    }

    private inner class TouchHandler : android.view.View.OnTouchListener {
        private var iniX = 0; private var iniY = 0
        private var iniTouchX = 0f; private var iniTouchY = 0f
        private var iniDist = 0f
        private var iniTamano = 0
        private var modoPinch = false
        private var movido = false

        override fun onTouch(v: android.view.View, e: MotionEvent): Boolean {
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    iniX = params.x; iniY = params.y
                    iniTouchX = e.rawX; iniTouchY = e.rawY
                    movido = false
                    modoPinch = false
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (e.pointerCount >= 2) {
                        modoPinch = true
                        iniDist = dist(e)
                        iniTamano = tamano
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (modoPinch && e.pointerCount >= 2) {
                        val d = dist(e)
                        if (iniDist > 0f) {
                            val nuevo = (iniTamano * (d / iniDist)).toInt()
                            tamano = nuevo.coerceIn(90, 320)
                            params.width = tamano
                            params.height = tamano
                            wm.updateViewLayout(vista, params)
                        }
                    } else {
                        val dx = (e.rawX - iniTouchX).toInt()
                        val dy = (e.rawY - iniTouchY).toInt()
                        if (abs(dx) > 12 || abs(dy) > 12) movido = true
                        params.x = iniX + dx
                        params.y = iniY + dy
                        wm.updateViewLayout(vista, params)
                    }
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    if (e.pointerCount <= 2) modoPinch = false
                }

                MotionEvent.ACTION_UP -> {
                    if (!movido && !modoPinch) toggleVpn()
                }
            }
            return true
        }

        private fun dist(e: MotionEvent): Float {
            val dx = e.getX(0) - e.getX(1)
            val dy = e.getY(0) - e.getY(1)
            return hypot(dx, dy)
        }
    }

    private fun toggleVpn() {
        if (!activo) {
            val prep = VpnService.prepare(this)
            if (prep != null) {
                prep.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(prep)
                return
            }
            startService(Intent(this, MyVpnService::class.java)
                .setAction(MyVpnService.ACTION_CONNECT))
            activo = true
        } else {
            startService(Intent(this, MyVpnService::class.java)
                .setAction(MyVpnService.ACTION_DISCONNECT))
            activo = false
        }
        vista.activo = activo
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::vista.isInitialized) try { wm.removeView(vista) } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
