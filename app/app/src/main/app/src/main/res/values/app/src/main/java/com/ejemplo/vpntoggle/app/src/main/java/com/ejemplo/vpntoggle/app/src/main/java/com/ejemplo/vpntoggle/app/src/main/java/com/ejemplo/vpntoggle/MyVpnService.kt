package com.ejemplo.vpntoggle

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

class MyVpnService : VpnService() {

    companion object {
        const val ACTION_CONNECT = "CONNECT"
        const val ACTION_DISCONNECT = "DISCONNECT"
    }

    private var tun: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> conectar()
            ACTION_DISCONNECT -> desconectar()
        }
        return START_STICKY
    }

    private fun conectar() {
        if (tun != null) return
        try {
            tun = Builder()
                .setSession("VPN Toggle")
                .setMtu(1500)
                .addAddress("10.0.0.1", 32)
                .addRoute("0.0.0.0", 0)
                .addAddress("fd00::1", 128)
                .addRoute("::", 0)
                .addDnsServer("10.0.0.1")
                .establish()
        } catch (e: Exception) {
            e.printStackTrace()
            tun = null
        }
    }

    private fun desconectar() {
        try { tun?.close() } catch (_: Exception) {}
        tun = null
        stopSelf()
    }

    override fun onRevoke() {
        desconectar()
        super.onRevoke()
    }

    override fun onDestroy() {
        desconectar()
        super.onDestroy()
    }
}
