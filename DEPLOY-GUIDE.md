# 阿里云 ECS 手动部署指南

## 服务器信息
- IP: 101.200.163.69
- OS: Ubuntu 22.04 64位
- SSH: `ssh root@101.200.163.69`（密码: Mhw666.com）

---

## 第一步：SSH 连接到服务器

在本地终端执行：
```bash
ssh root@101.200.163.69
```
输入密码: `Mhw666.com`

---

## 第二步：添加 2GB Swap（防止构建时内存不足）

你的服务器只有 2GB 内存，Docker 构建 Maven 项目时会吃紧，必须先加 swap：

```bash
fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
```

验证：
```bash
free -h
# 应该能看到 Swap: 2.0G
```

---

## 第三步：安装 Docker

依次复制粘贴执行以下命令：

```bash
# 1. 更新 apt 索引
apt-get update

# 2. 安装必要依赖
apt-get install -y ca-certificates curl gnupg

# 3. 创建 keyring 目录
install -m 0755 -d /etc/apt/keyrings

# 4. 添加 Docker 官方 GPG 密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# 5. 设置权限
chmod a+r /etc/apt/keyrings/docker.gpg

# 6. 添加 Docker 软件源
echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu jammy stable" > /etc/apt/sources.list.d/docker.list

# 7. 再次更新 apt 索引
apt-get update

# 8. 安装 Docker + Compose 插件
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

安装完成后验证：
```bash
docker --version
# 应显示: Docker version 27.x 或更高

docker compose version
# 应显示: Docker Compose version v2.x
```

---

## 第四步：阿里云安全组放行 80 端口

浏览器访问网站需要 80 端口，当前安全组只有 22(SSH) 开放。

1. 打开阿里云控制台 → ECS → 安全组
2. 找到你实例的安全组，点"配置规则"
3. 入方向 → 手动添加：
   - 优先级: 90
   - 协议类型: TCP
   - 端口范围: 80/80
   - 授权对象: 0.0.0.0/0
   - 描述: Web HTTP

---

## 第五步：通知我

完成上面四步后，告诉我"Docker 装好了"，我会：
1. 通过 SSH 把项目文件传到服务器
2. 运行 `docker compose build` 构建镜像
3. 运行 `docker compose up -d` 启动容器
4. 测试网站是否正常访问

---

## 部署后的访问信息（预告）

| 项目 | 值 |
|------|-----|
| 网站地址 | http://101.200.163.69 |
| 登录用户名 | mhwzzu |
| 登录密码 | love-journey-2026 |
| 数据库 | PostgreSQL 16（Docker 持久化 volume） |
| 地图搜索 | 未配置高德Key，暂不可用（其他功能正常） |
