package com.example.phonenotifysync

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.charset.StandardCharsets

object UdpSender {
    private const val PORT = 39555
    fun send(appName: String) {
        Thread {
            try {
                val data = "$appName|${System.currentTimeMillis()}".toByteArray(StandardCharsets.UTF_8)
                DatagramSocket().use { socket ->
                    socket.broadcast = true
                    val address = InetAddress.getByName("255.255.255.255")
                    socket.send(DatagramPacket(data, data.size, address, PORT))
                }
            } catch (_: Exception) { }
        }.start()
    }
}
