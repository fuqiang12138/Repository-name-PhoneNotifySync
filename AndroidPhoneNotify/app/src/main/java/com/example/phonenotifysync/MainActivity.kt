package com.example.phonenotifysync

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.widget.*

class MainActivity : Activity() {
    private lateinit var status: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48,64,48,48) }
        root.addView(TextView(this).apply { text = "手机通知同步"; textSize = 28f; setTextColor(Color.BLACK) })
        status = TextView(this).apply { textSize = 18f; setPadding(0,32,0,24) }
        root.addView(status)
        root.addView(Button(this).apply { text = "① 打开通知访问权限"; setOnClickListener { startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) } })
        root.addView(Button(this).apply { text = "② 测试电脑通知"; setOnClickListener { UdpSender.send("手机通知同步测试"); Toast.makeText(this@MainActivity,"已发送，请看电脑",Toast.LENGTH_SHORT).show() } })
        root.addView(TextView(this).apply { text = "\n使用说明\n手机和电脑连接同一个 Wi‑Fi。\n打开通知访问权限后，微信、QQ、淘宝等收到通知时，电脑会提示 App 名称。\n\n本版本不会发送通知正文。"; textSize = 16f })
        setContentView(root)
    }
    override fun onResume() { super.onResume(); status.text = if (NotificationSyncService.isEnabled(this)) "状态：通知监听已开启" else "状态：请打开通知访问权限" }
}
