# 🔋 超轻量电池报告器

一个专为手机自动化设计的超小APK应用（预计< 50KB）

## 📱 功能特性

- **打开即发送**：应用启动自动发送电量报告
- **手机标识**：可自定义手机名称区分不同设备
- **本地保存**：标识保存到txt文件和SharedPreferences
- **自动关闭**：发送完成后2秒自动退出
- **完美自动化**：专为Tasker等自动化工具设计
- **超轻量级**：原生Java开发，无第三方依赖

## 🚀 使用方法（自动化模式）

1. 安装APK到手机
2. **打开应用即自动发送电量报告**（无需手动操作）
3. 发送成功后应用自动关闭
4. 完美配合Tasker等自动化工具调用

## 📡 API请求格式

```bash
curl --location --request POST 'http://1.14.165.245:7020/fs1?date=1' \
--data-urlencode 'text=[手机标识]电量[百分比]%!'
```

**示例**：如果手机标识为"主人手机"，电量为85%，则发送：
```
text=主人手机电量85%!
```

## 🛠️ 构建说明

### 在线构建（推荐）

无需安装Android Studio，使用在线构建服务：

1. **GitHub Actions**: 上传到GitHub，自动构建
2. **Replit**: 在线Android开发环境
3. **AppCenter**: 微软免费构建服务

### 本地构建

如需本地构建，需要：

- Android SDK
- Gradle
- JDK 8+

## 📦 APK优化

- 使用ProGuard代码混淆压缩
- 启用资源收缩
- 无AppCompat库，减小体积
- 最小SDK版本16（Android 4.1+）

## 🎯 自动化集成

完美配合各种手机自动化工具：

- Tasker
- Automate
- MacroDroid
- Termux

## 📋 权限说明

- `INTERNET`: 发送HTTP请求
- `BATTERY_STATS`: 读取电池信息
- `WRITE_EXTERNAL_STORAGE`: 保存手机标识到txt文件
- `READ_EXTERNAL_STORAGE`: 读取已保存的手机标识

## 📝 手机标识功能
- 首次使用需设置手机标识（如："主人手机"、"客厅手机"等）
- 标识会保存到应用私有目录的`phone_id.txt`文件
- 同时保存到SharedPreferences，下次打开自动加载
- 电量报告中会包含手机标识，便于区分不同设备
