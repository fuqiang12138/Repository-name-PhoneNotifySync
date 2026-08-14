using System.Net;
using System.Net.Sockets;
using System.Text;

namespace PhoneNotifyReceiver;

internal static class Program
{
    [STAThread]
    static void Main()
    {
        ApplicationConfiguration.Initialize();
        using var tray = new NotifyIcon { Icon = SystemIcons.Information, Visible = true, Text = "手机通知同步" };
        var menu = new ContextMenuStrip();
        menu.Items.Add("退出", null, (_, _) => Application.Exit());
        tray.ContextMenuStrip = menu;
        tray.ShowBalloonTip(2500, "手机通知同步", "电脑端已启动，等待手机通知……", ToolTipIcon.Info);
        using var listener = new UdpNotificationListener(tray);
        listener.Start();
        Application.Run();
        tray.Visible = false;
    }
}

internal sealed class UdpNotificationListener : IDisposable
{
    private const int Port = 39555;
    private readonly NotifyIcon tray;
    private readonly UdpClient udp = new(Port);
    private readonly CancellationTokenSource cts = new();

    public UdpNotificationListener(NotifyIcon tray) => this.tray = tray;

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
                tray.BalloonTipTitle = $"📱 {app}";
                tray.BalloonTipText = "收到新通知";
                tray.ShowBalloonTip(3500);
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
