package com.example.phonenotifysync

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var statusDot: TextView

    private val blue = Color.rgb(37, 99, 235)
    private val bg = Color.rgb(246, 248, 252)
    private val text = Color.rgb(25, 32, 45)
    private val subText = Color.rgb(103, 113, 130)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(28))
            setBackgroundColor(bg)
        }
        scroll.addView(root)

        val header = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val title = TextView(this).apply {
            this.text = "手机通知同步"
            textSize = 28f
            setTextColor(text)
            typeface = Typeface.DEFAULT_BOLD
        }
        val subtitle = TextView(this).apply {
            this.text = "让手机通知，安静地出现在电脑上"
            textSize = 15f
            setTextColor(subText)
            setPadding(0, dp(5), 0, dp(14))
        }
        header.addView(title)
        header.addView(subtitle)
        root.addView(header)

        val statusCard = card()
        val statusRow = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        statusDot = TextView(this).apply {
            text = "●"
            textSize = 22f
            setPadding(0, 0, dp(10), 0)
        }
        statusText = TextView(this).apply {
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(text)
        }
        statusRow.addView(statusDot)
        statusRow.addView(statusText)
        statusCard.addView(statusRow)
        val statusHint = TextView(this).apply {
            text = "手机和电脑连接同一个 Wi‑Fi 即可同步"
            textSize = 13f
            setTextColor(subText)
            setPadding(dp(32), dp(5), 0, 0)
        }
        statusCard.addView(statusHint)
        root.addView(statusCard, marginBottom(14))

        val permissionButton = Button(this).apply {
            text = "①  打开通知访问权限"
            textSize = 16f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(blue, 14)
            minHeight = dp(54)
            setOnClickListener {
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        }
        root.addView(permissionButton, marginBottom(10))

        val testButton = Button(this).apply {
            text = "②  测试电脑通知"
            textSize = 16f
            isAllCaps = false
            setTextColor(blue)
            background = rounded(Color.WHITE, 14, Color.rgb(220, 226, 238))
            minHeight = dp(54)
            setOnClickListener {
                UdpSender.send("手机通知同步测试")
                Toast.makeText(this@MainActivity, "测试通知已发送", Toast.LENGTH_SHORT).show()
            }
        }
        root.addView(testButton, marginBottom(18))

        val infoCard = card()
        addSectionTitle(infoCard, "怎么使用")
        addStep(infoCard, "1", "打开通知权限", "允许本应用读取手机通知")
        addStep(infoCard, "2", "连接同一个 Wi‑Fi", "手机和 Windows 电脑在同一网络")
        addStep(infoCard, "3", "保持本 App 正常运行", "收到通知后电脑会自动弹窗")
        root.addView(infoCard, marginBottom(14))

        val privacyCard = card()
        addSectionTitle(privacyCard, "隐私保护")
        privacyCard.addView(TextView(this).apply {
            text = "✓  只同步 App 名称\n✓  不同步聊天正文\n✓  不经过云服务器\n✓  仅在局域网内传输"
            textSize = 14f
            setTextColor(subText)
            setLineSpacing(dp(4).toFloat(), 1f)
        })
        root.addView(privacyCard)

        setContentView(scroll)
    }

    private fun addSectionTitle(parent: LinearLayout, titleText: String) {
        parent.addView(TextView(this).apply {
            text = titleText
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(text)
            setPadding(0, 0, 0, dp(12))
        })
    }

    private fun addStep(parent: LinearLayout, number: String, titleText: String, desc: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(5), 0, dp(9))
        }
        val badge = TextView(this).apply {
            text = number
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(blue)
            background = rounded(Color.rgb(232, 239, 255), 10)
        }
        row.addView(badge, LinearLayout.LayoutParams(dp(32), dp(32)))
        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        texts.addView(TextView(this).apply {
            text = titleText
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(text)
        })
        texts.addView(TextView(this).apply {
            text = desc
            textSize = 12.5f
            setTextColor(subText)
            setPadding(0, dp(2), 0, 0)
        })
        row.addView(texts)
        parent.addView(row)
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = rounded(Color.WHITE, 16)
    }

    private fun rounded(color: Int, radius: Int, stroke: Int? = null): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        stroke?.let { setStroke(dp(1), it) }
    }

    private fun marginBottom(value: Int): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = dp(value) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onResume() {
        super.onResume()
        val enabled = NotificationSyncService.isEnabled(this)
        statusText.text = if (enabled) "通知监听已开启" else "等待开启通知权限"
        statusDot.setTextColor(if (enabled) Color.rgb(34, 197, 94) else Color.rgb(245, 158, 11))
    }
}
