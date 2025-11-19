# GitHub 推送超时问题排查指南

## 已完成的配置

✅ 已增加 HTTP 超时时间到 300 秒
✅ 已增加 POST buffer 大小到 500MB
✅ 已禁用低速连接限制

## 问题排查步骤

### 1. 测试网络连接
```powershell
# 测试 GitHub 连接
ping github.com
ping api.github.com

# 测试 HTTPS 连接
curl -I https://github.com
```

### 2. 检查是否需要配置代理

如果你在公司网络或需要代理访问外网：

#### 查看系统代理设置
```powershell
# 查看环境变量
echo $env:HTTP_PROXY
echo $env:HTTPS_PROXY
```

#### 配置 Git 代理（如果需要）
```powershell
# 如果使用 HTTP 代理
git config --global http.proxy http://proxy.example.com:8080
git config --global https.proxy http://proxy.example.com:8080

# 如果使用 SOCKS5 代理
git config --global http.proxy socks5://127.0.0.1:1080
git config --global https.proxy socks5://127.0.0.1:1080

# 取消代理（如果不需要）
git config --global --unset http.proxy
git config --global --unset https.proxy
```

### 3. 尝试使用 SSH 代替 HTTPS（推荐）

SSH 通常比 HTTPS 更稳定，特别是在网络不稳定的情况下。

#### 步骤：
1. **生成 SSH 密钥**（如果还没有）
```powershell
ssh-keygen -t ed25519 -C "your_email@example.com"
```

2. **添加 SSH 密钥到 GitHub**
   - 复制公钥内容：`cat ~/.ssh/id_ed25519.pub`
   - 登录 GitHub → Settings → SSH and GPG keys → New SSH key
   - 粘贴公钥并保存

3. **测试 SSH 连接**
```powershell
ssh -T git@github.com
```

4. **更改远程仓库地址为 SSH**
```powershell
git remote set-url origin git@github.com:LZY18829039855/AI_Transform.git
```

### 4. 检查推送的文件大小

大文件可能导致超时，检查是否有大文件：
```powershell
# 查看待推送的文件大小
git ls-files | ForEach-Object { Get-Item $_ | Select-Object Name, Length }
```

如果文件很大，考虑使用 Git LFS：
```powershell
git lfs install
git lfs track "*.largefile"
```

### 5. 分批推送

如果有很多提交，可以尝试分批推送：
```powershell
# 查看待推送的提交
git log origin/main..HEAD

# 推送最近的几个提交
git push origin HEAD~5:main
```

### 6. 使用详细模式查看错误

```powershell
# 使用详细模式推送，查看具体错误
git push -v origin main
# 或
GIT_CURL_VERBOSE=1 GIT_TRACE=1 git push origin main
```

### 7. 检查防火墙/安全软件

- 检查 Windows 防火墙是否阻止 Git
- 检查公司安全软件是否拦截
- 临时关闭防火墙测试

### 8. 使用 GitHub CLI 测试

```powershell
# 安装 GitHub CLI (如果还没有)
# 然后测试连接
gh auth status
```

## 常见解决方案总结

### 方案 A：增加超时（已完成）
✅ 已配置

### 方案 B：配置代理（如果需要）
根据你的网络环境配置代理

### 方案 C：使用 SSH（最推荐）
更稳定，不受 HTTPS 超时影响

### 方案 D：使用 VPN
如果网络环境限制，使用 VPN 可能解决问题

## 验证配置

查看当前 Git 配置：
```powershell
git config --global --list | findstr -i "http\|proxy\|timeout"
```

## 测试推送

配置完成后，尝试推送：
```powershell
git push origin main
```

如果仍然超时，使用详细模式查看具体错误：
```powershell
GIT_CURL_VERBOSE=1 GIT_TRACE=1 git push origin main
```





