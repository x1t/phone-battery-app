@echo off
echo 创建Android发布签名证书...

:: 使用系统中的keytool创建证书
keytool -genkey -v -keystore battery-release-key.keystore -alias battery_key -keyalg RSA -keysize 2048 -validity 10000 -storepass batteryapp123 -keypass batteryapp123 -dname "CN=Battery App Developer, OU=Development, O=Battery App, L=Beijing, ST=Beijing, C=CN"

echo.
echo 证书创建完成！
echo 文件位置: battery-release-key.keystore
echo 别名: battery_key
echo 密码: batteryapp123
pause