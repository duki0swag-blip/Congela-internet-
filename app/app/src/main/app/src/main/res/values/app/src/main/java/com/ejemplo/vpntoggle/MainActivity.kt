package com.ejemplo.vpntoggle

import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val VPN_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 300, 60, 60)
        }

        layout.addView(boton("1. Permiso overlay") {
            startActivity(Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            ))
        })

        layout.addView(boton("2. Permiso VPN") {
            val i = VpnService.prepare(this)
            if (i != null) startActivityForResult(i, VPN_REQUEST)
            else Toast.makeText(this, "Ya tienes permiso VPN", Toast.LENGTH_SHORT).show()
        })

        layout.addView(boton("3. Iniciar botón flotante") {
            val i = Intent(this, FloatingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(i)
            else startService(i)
            finish()
        })

        setContentView(layout)
    }

    private fun boton(txt: String, onClick: () -> Unit) = Button(this).apply {
        text = txt
        textSize = 16f
        setOnClickListener { onClick() }
    }
}
