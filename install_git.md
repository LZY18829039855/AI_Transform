# Git 下载和安装指南

## 官方下载地址

### Windows 版本
**官方下载页面：** https://git-scm.com/download/win

**直接下载链接：**
- 64位版本：https://github.com/git-for-windows/git/releases/download/v2.43.0.windows.1/Git-2.43.0-64-bit.exe
- 32位版本：https://github.com/git-for-windows/git/releases/download/v2.43.0.windows.1/Git-2.43.0-32-bit.exe

## 安装步骤

### 方法1：使用官方安装程序（推荐）

1. **访问下载页面**
   - 打开浏览器，访问：https://git-scm.com/download/win
   - 页面会自动检测您的系统并显示下载链接

2. **下载安装程序**
   - 点击下载按钮，下载 Git for Windows 安装程序
   - 文件名为类似 `Git-2.43.0-64-bit.exe` 的格式

3. **运行安装程序**
   - 双击下载的 `.exe` 文件
   - 按照安装向导进行安装

4. **安装选项建议**
   - **选择组件：** 保持默认选择即可
   - **默认编辑器：** 可以选择 VS Code 或其他编辑器
   - **PATH 环境：** 选择 "Git from the command line and also from 3rd-party software"（推荐）
   - **行尾转换：** 选择 "Checkout Windows-style, commit Unix-style line endings"（推荐）
   - **终端模拟器：** 选择 "Use Windows' default console window"
   - **其他选项：** 保持默认即可

5. **完成安装**
   - 安装完成后，重启 PowerShell 或命令提示符
   - 验证安装：运行 `git --version`

### 方法2：使用包管理器

#### 使用 Chocolatey
```powershell
# 以管理员身份运行 PowerShell
choco install git
```

#### 使用 Winget（Windows 10/11）
```powershell
winget install --id Git.Git -e --source winget
```

#### 使用 Scoop
```powershell
scoop install git
```

## 验证安装

安装完成后，打开新的 PowerShell 或命令提示符窗口，运行：

```bash
git --version
```

如果显示版本号（如 `git version 2.43.0.windows.1`），说明安装成功。

## 配置 Git

首次使用 Git，需要配置用户信息：

```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

## 后续步骤

安装完成后，您可以：
1. 运行 `git_push.bat` 脚本推送代码
2. 或手动执行 Git 命令推送代码

## 常见问题

### Q: 安装后无法识别 git 命令？
A: 需要重启 PowerShell 或命令提示符窗口，或者重启计算机。

### Q: 如何更新 Git？
A: 下载最新版本的安装程序，直接运行安装即可，会自动更新。

### Q: 安装后如何使用？
A: 可以在 PowerShell、命令提示符或 Git Bash 中使用 Git 命令。

