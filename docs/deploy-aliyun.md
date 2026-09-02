# 阿里云 ECS 部署说明

本项目的生产部署使用 Docker Compose：

- `frontend`：Nginx 服务 Vue 静态文件，并把 `/api` 反代到后端
- `backend`：Spring Boot，使用 `postgres` profile
- `database`：PostgreSQL 16，数据保存在 Docker volume `journey-postgres`

## 服务器要求

- 阿里云 ECS，建议 1 核 2G 起步
- 系统建议 Ubuntu 22.04 / Debian 12 / CentOS Stream
- 已放行安全组入站端口：`22`、`80`
- 如使用域名和 HTTPS，额外放行 `443`
- 已安装 Docker 和 Docker Compose 插件

## 首次部署

```bash
git clone https://github.com/mhwzzu/firstProject.git
cd firstProject
cp .env.example .env
```

编辑 `.env`，至少修改：

```env
POSTGRES_PASSWORD=你的数据库强密码
AMAP_KEY=你的高德Web服务Key
APP_SECURE_COOKIE=false
WEB_PORT=80
```

`APP_SECURE_COOKIE` 仅在域名和 HTTPS 已配置完成后设为 `true`；纯 HTTP 的测试环境保持 `false`，否则浏览器不会发送会话 Cookie。

启动：

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

访问：

```text
http://你的服务器公网IP/
```

## 更新部署

```bash
cd firstProject
git pull --ff-only
docker compose -f docker-compose.prod.yml up -d --build
```

## 常用排查

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend
docker compose -f docker-compose.prod.yml logs -f frontend
docker compose -f docker-compose.prod.yml logs -f database
```
