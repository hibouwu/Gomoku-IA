# 设置下载URL和安装路径
$jdkUrl = "https://download.oracle.com/java/23/latest/jdk-23_windows-x64_bin.exe"
$installerPath = "$env:TEMP\jdk-23_installer.exe"
$installPath = "C:\Program Files\Java\jdk-23"

# 下载JDK安装程序
Write-Host "正在下载JDK 23..."
Invoke-WebRequest -Uri $jdkUrl -OutFile $installerPath

# 安装JDK
Write-Host "正在安装JDK 23..."
Start-Process -FilePath $installerPath -ArgumentList "/s" -Wait

# 设置环境变量
Write-Host "正在配置环境变量..."
$currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
if (-not $currentPath.Contains($installPath)) {
    $newPath = $installPath + "\bin;" + $currentPath
    [Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")
    [Environment]::SetEnvironmentVariable("JAVA_HOME", $installPath, "Machine")
}

# 清理安装文件
Remove-Item $installerPath

Write-Host "JDK 23安装完成！请重新打开终端以使环境变量生效。"
Write-Host "你可以通过运行 'java -version' 来验证安装。" 