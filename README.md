# 两个人的漫游簿

一个面向两人的私密出行决策与共同回忆应用。它会结合双方偏好、城市、天气、时间与季节，主动推荐“今晚去哪里”和“下个周末去哪”，并将投票、计划和已完成行程沉淀为后续推荐信号。

现在包含独立账户、双人空间邀请码、偏好引导、可解释的自动推荐、共同计划、计划完成回忆，以及通过高德地图 Web API 获取地点与天气事实。

## 技术栈

- Vue 3 + Vite
- Spring Boot 2.7（兼容 Java 8）
- PostgreSQL 16（Docker）
- H2（无需 Docker 的本地快速体验）

## 本地启动

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认使用内存数据库并自动加载示例记录，地址为 `http://localhost:8080`。

首次打开前端后，使用邮箱创建自己的账户，再创建双人空间或粘贴另一位成员生成的邀请码。密码仅以单向哈希形式保存在数据库中；请使用至少 8 位的强密码。

如需使用 PostgreSQL：

```bash
docker compose up -d database
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

打开 `http://localhost:5173`。Vite 会将 `/api` 请求代理至后端。

## 高德地图搜索

地点、路线与天气事实由后端代理高德 Web 服务 API，推荐效果需要配置高德 Web 服务 Key：

```powershell
$env:AMAP_KEY="你的高德Key"
mvn spring-boot:run
```

没有配置 `AMAP_KEY` 时，系统会明确显示降级状态并使用可解释的季节与偏好规则生成候选；不会伪造实时天气或具体店铺事实。

## 验证

```bash
cd backend
mvn test

cd ../frontend
npm run build
```

## 数据说明

- H2 模式适用于本地开发；数据在后端重启后不会作为生产数据使用。
- PostgreSQL 模式的数据保存在 Docker volume `journey-postgres` 中。
- 正式部署前请复制 `.env.example` 为 `.env` 并修改数据库密码与 `AMAP_KEY`，不要提交 `.env`。在域名已启用 HTTPS 后，将 `APP_SECURE_COOKIE` 改为 `true`；纯 HTTP 的测试环境必须保持 `false`，否则浏览器不会发送会话 Cookie。

## 阿里云部署

项目已包含生产 Docker 配置：

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

完整步骤见 [docs/deploy-aliyun.md](docs/deploy-aliyun.md)。
