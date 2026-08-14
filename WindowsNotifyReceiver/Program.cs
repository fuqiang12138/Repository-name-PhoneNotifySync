using System.Diagnostics;
using System.Drawing;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Windows.Forms;

namespace PhoneNotifyReceiver;

internal static class Program
{
    [STAThread]
    static void Main()
    {
        ApplicationConfiguration.Initialize();
        using var tray = new NotifyIcon
        {
            Icon = SystemIcons.Information,
            Visible = true,
            Text = "手机通知同步"
        };

        using var form = new MainForm(tray);
        tray.DoubleClick += (_, _) => form.ShowFromTray();

        var menu = new ContextMenuStrip();
        menu.Items.Add("打开主界面", null, (_, _) => form.ShowFromTray());
        menu.Items.Add("测试弹窗", null, (_, _) => form.ShowNotification("手机通知同步测试", "电脑端测试成功"));
        menu.Items.Add("放行 Windows 防火墙", null, (_, _) => FirewallHelper.AllowUdpPort());
        menu.Items.Add(new ToolStripSeparator());
        menu.Items.Add("退出", null, (_, _) => Application.Exit());
        tray.ContextMenuStrip = menu;

        using var listener = new UdpNotificationListener(form);
        listener.Start();

        form.ShowFromTray(false);
        Application.Run();
        tray.Visible = false;
    }
}

internal sealed class MainForm : Form
{
    private readonly Label last;

    public MainForm(NotifyIcon tray)
    {
        Text = "手机通知同步";
        StartPosition = FormStartPosition.CenterScreen;
        Size = new Size(520, 360);
        MinimumSize = new Size(520, 360);
        FormBorderStyle = FormBorderStyle.FixedSingle;
        MaximizeBox = false;

        var title = new Label
        {
            Text = "手机通知同步",
            Font = new Font("Microsoft YaHei UI", 22, FontStyle.Bold),
            AutoSize = true,
            Location = new Point(30, 25)
        };

        var status = new Label
        {
            Text = "● 电脑端正在监听 UDP 39555",
            Font = new Font("Microsoft YaHei UI", 12),
            AutoSize = true,
            Location = new Point(32, 78)
        };

        last = new Label
        {
            Text = "等待手机通知……",
            Font = new Font("Microsoft YaHei UI", 11),
            AutoSize = false,
            Size = new Size(450, 55),
            Location = new Point(32, 115)
        };

        var test = new Button
        {
            Text = "测试电脑弹窗",
            Size = new Size(180, 42),
            Location = new Point(32, 190)
        };
        test.Click += (_, _) => ShowNotification("手机通知同步测试", "电脑端测试成功");

        var firewall = new Button
        {
            Text = "放行 Windows 防火墙",
            Size = new Size(220, 42),
            Location = new Point(230, 190)
        };
        firewall.Click += (_, _) => FirewallHelper.AllowUdpPort();

        var info = new Label
        {
            Text = "使用方法：\n1. 手机和电脑连接同一个 Wi‑Fi\n2. 点击一次“放行 Windows 防火墙”\n3. 手机打开通知访问权限\n4. 点击手机 App 里的“测试电脑通知”",
            Font = new Font("Microsoft YaHei UI", 10),
            AutoSize = true,
            Location = new Point(32, 250)
        };

        Controls.AddRange([title, status, last, test, firewall, info]);
        FormClosing += (_, e) =>
        {
            if (e.CloseReason == CloseReason.UserClosing)
            {
                e.Cancel = true;
                Hide();
            }
        };
    }

    public void ShowFromTray(bool activate = true)
    {
        Show();
        if (activate)
        {
            WindowState = FormWindowState.Normal;
            Activate();
            BringToFront();
        }
    }

    public void SetLast(string text)
    {
        if (IsDisposed) return;
        if (InvokeRequired) { BeginInvoke(() => SetLast(text)); return; }
        last.Text = text;
    }

    public void ShowNotification(string title, string body)
    {
        if (InvokeRequired) { BeginInvoke(() => ShowNotification(title, body)); return; }
        SetLast($"最近通知：{title}\n{body}");
        var popup = new PopupForm(title, body);
        popup.FormClosed += (_, _) => popup.Dispose();
        popup.Show(this);
        popup.Activate();
        popup.WaitAndClose();
    }
}

internal sealed class PopupForm : Form
{
    private readonly System.Windows.Forms.Timer timer;

    public PopupForm(string title, string body)
    {
        FormBorderStyle = FormBorderStyle.FixedSingle;
        StartPosition = FormStartPosition.Manual;
        TopMost = true;
        ShowInTaskbar = false;
        Width = 360;
        Height = 120;
        var area = Screen.PrimaryScreen?.WorkingArea ?? new Rectangle(0, 0, 1920, 1080);
        Location = new Point(area.Right - Width - 20, area.Bottom - Height - 20);

        var titleLabel = new Label
        {
            Text = title,
            Font = new Font("Microsoft YaHei UI", 12, FontStyle.Bold),
            AutoSize = false,
            Size = new Size(320, 30),
            Location = new Point(18, 12)
        };
        var bodyLabel = new Label
        {
            Text = body,
            Font = new Font("Microsoft YaHei UI", 10),
            AutoSize = false,
            Size = new Size(320, 45),
            Location = new Point(18, 48)
        };
        Controls.Add(titleLabel);
        Controls.Add(bodyLabel);
        timer = new System.Windows.Forms.Timer { Interval = 3500 };
        timer.Tick += (_, _) => Close();
    }

    public void WaitAndClose() => timer.Start();

    protected override void OnFormClosed(FormClosedEventArgs e)
    {
        timer.Stop();
        timer.Dispose();
        base.OnFormClosed(e);
    }
}

internal sealed class UdpNotificationListener : IDisposable
{
    private const int Port = 39555;
    private readonly MainForm form;
    private readonly UdpClient udp;
    private readonly CancellationTokenSource cts = new();

    public UdpNotificationListener(MainForm form)
    {
        this.form = form;
        udp = new UdpClient(new IPEndPoint(IPAddress.Any, Port));
        udp.EnableBroadcast = true;
    }

    public void Start() => _ = Task.Run(ReceiveLoop);

    private async Task ReceiveLoop()
    {
        while (!cts.IsCancellationRequested)
        {
            try
            {
                var result = await udp.ReceiveAsync(cts.Token);
                var text = Encoding.UTF8.GetString(result.Buffer);
                var app = text.Split('|', 2)[0];
                if (string.IsNullOrWhiteSpace(app)) app = "手机";
                form.ShowNotification($"📱 {app}", "收到新通知");
            }
            catch (OperationCanceledException) { break; }
            catch { await Task.Delay(500); }
        }
    }

    public void Dispose()
    {
        cts.Cancel();
        udp.Dispose();
        cts.Dispose();
    }
}

internal static class FirewallHelper
{
    public static void AllowUdpPort()
    {
        try
        {
            var psi = new ProcessStartInfo
            {
                FileName = "netsh",
                Arguments = "advfirewall firewall add rule name=\"PhoneNotifySync UDP 39555\" dir=in action=allow protocol=UDP localport=39555 profile=private",
                UseShellExecute = true,
                Verb = "runas",
                WindowStyle = ProcessWindowStyle.Hidden
            };
            Process.Start(psi)?.WaitForExit();
            MessageBox.Show("已尝试放行 UDP 39555。现在可以回手机点击“测试电脑通知”。", "手机通知同步", MessageBoxButtons.OK, MessageBoxIcon.Information);
        }
        catch (Exception ex)
        {
            MessageBox.Show("没有获得管理员权限。请再次点击并在 Windows 弹出的权限窗口中选择“是”。\n\n" + ex.Message, "手机通知同步", MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }
    }
}
