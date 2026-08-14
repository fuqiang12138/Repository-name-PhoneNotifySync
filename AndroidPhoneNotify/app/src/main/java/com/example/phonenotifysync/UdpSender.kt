package com.example.phonenotifysync

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.charset.StandardCharsets

object UdpSender {
    private const val PORT = 39555

    fun send(appName: String) {
        Thread {
            val text = "$appName|${System.currentTimeMillis()}"
            val data = text.toByteArray(StandardCharsets.UTF_8)
            try {
                DatagramSocket().use { socket ->
                    socket.broadcast = true
                    var sent = false
                    val interfaces = NetworkInterface.getNetworkInterfaces()
                    while (interfaces.hasMoreElements()) {
                        val network = interfaces.nextElement()
                        if (!network.isUp || network.isLoopback) continue
                        for (address in network.interfaceAddresses) {
                            val broadcast = address.broadcast ?: continue
                            try {
                                socket.send(DatagramPacket(data, data.size, broadcast, PORT))
                                sent = true
                            } catch (_: Exception) { }
                        }
                    }
                    if (!sent) {
                        val fallback = InetAddress.getByName("255.255.255.255")
                        socket.send(DatagramPacket(data, data.size, fallback, PORT))
                    }
                }
            } catch (_: Exception) {
                // 简易版：网络不可用时静默处理
            }
        }.start()
    }
}
