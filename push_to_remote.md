# 推送到远端仓库步骤

## 远端仓库地址
https://github.com/LZY18829039855/AI_Transform.git

## 推送步骤

### 1. 初始化 Git 仓库（如果还没有初始化）
```bash
git init
```

### 2. 添加远程仓库
```bash
git remote add origin https://github.com/LZY18829039855/AI_Transform.git
```

如果已经存在 origin，可以更新：
```bash
git remote set-url origin https://github.com/LZY18829039855/AI_Transform.git
```

### 3. 添加所有文件
```bash
git add .
```

### 4. 提交更改
```bash
git commit -m "Initial commit: AI Transform Backend Service"
```

### 5. 推送到远端（首次推送）
```bash
git push -u origin main
```

或者如果默认分支是 master：
```bash
git push -u origin master
```

### 6. 如果推送失败（分支不存在），先创建并推送
```bash
git branch -M main
git push -u origin main
```

## 后续更新推送

```bash
git add .
git commit -m "描述你的更改"
git push
```

## 注意事项

1. 确保已安装 Git
2. 确保已配置 Git 用户信息：
   ```bash
   git config --global user.name "Your Name"
   git config --global user.email "your.email@example.com"
   ```
3. 如果使用 HTTPS，可能需要输入 GitHub 用户名和 Personal Access Token
4. 如果使用 SSH，需要配置 SSH 密钥

