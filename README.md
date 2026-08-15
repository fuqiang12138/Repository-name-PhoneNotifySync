# PhoneNotifySync

小米15 → Windows 11 通知同步工具。

目标：制作个人使用的手机通知同步软件。

## 当前真实状态

所有进度以 GitHub 已提交代码、实际编译结果和测试结果为准。

### Android 客户端

已存在：

- Kotlin Android 工程基础结构
- MainActivity
- 通知同步原型代码
- UDP 通信原型

未完成：

- 稳定通知捕获方案
- 小米 HyperOS 适配优化
- WebSocket 通信
- 加密配对
- 完整 UI 重构
- APK 编译发布

### Windows 客户端

未完成：

- C# WinUI 3 工程
- 通知接收模块
- Apple 风格通知界面
- 后台运行服务
- EXE 安装包

## 当前开发阶段

阶段1：修复基础工程和编译环境

阶段2：完成 Android 通知同步核心

阶段3：完成 Windows 客户端

阶段4：真机测试并发布 APK / EXE

不会把计划功能标记为已完成。