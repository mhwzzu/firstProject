# 两个人的漫游簿

一个面向两人的私密出行决策与共同回忆应用。它会结合双方偏好、城市、天气、时间与季节，主动推荐“今晚去哪里”和“下个周末去哪”，并将心动、略过、计划和已完成行程沉淀为后续推荐信号。

当前版本采用 Atlas Noir「夜幕地图」产品界面，包含独立账户、双人空间、自然语言决策、实时内容证据、高德地点/路线、可展开天气卡、心愿、共同计划、足迹地图、回忆时间线和双方偏好设置。

推荐页支持“换一批”和“更新近期攻略”。配置内容发现服务后，系统会自动搜索小红书、抖音、微博和知乎近期公开索引内容，将标题、短摘要和原文链接作为推荐证据；不需要用户手工收藏链接。

## 技术栈

- Vue 3 + Vite
- Spring Boot 2.7（兼容 Java 8）
- PostgreSQL 16（Docker）
- H2（无需 Docker 的本地快速体验）

## 本地启动（推荐）

复制 `.env.example` 为 `.env.local`，填写高德 Web 服务 Key 和博查 Web Search API Key：

```properties
AMAP_KEY=你的高德Web服务Key
BOCHA_API_KEY=你的博查APIKey
DISCOVERY_ENABLED=true
```

`.env.local` 已被 Git 忽略，后端会自动读取，不需要每次设置 PowerShell 环境变量。然后在仓库根目录运行：

```powershell
.\start-local.ps1
```

脚本会在后台启动前后端并打开 `http://localhost:5173`。停止服务：

```powershell
.\stop-local.ps1
```

## 分别启动

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认使用内存数据库，地址为 `http://localhost:8080`。它不会自动写入示例账户或空间。

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

地点、天气与可跳转路线由后端代理高德 **Web 服务** API，推荐效果需要配置高德 Web 服务 Key：

```powershell
$env:AMAP_KEY="你的高德Key"
mvn spring-boot:run
```

没有配置 `AMAP_KEY` 时，系统会明确显示降级状态并使用可解释的季节与偏好规则生成候选；不会伪造实时天气或具体店铺事实。

在高德控制台创建 Key 时请选择“**Web服务**”平台；Android、iOS 和 Web 端（JS API）Key 不能替代后端的 Web 服务 Key。配置后需要重启后端，再点击“更新共同推荐”。前端地图上的“使用我的位置”由浏览器授权，拒绝授权时会明确改为按城市中心估算距离。

## 自动发现近期攻略

默认内容发现适配器使用博查 Web Search API 查询四个平台已经公开索引的页面。配置写入根目录 `.env.local`：

```powershell
DISCOVERY_ENABLED=true
BOCHA_API_KEY=你的博查APIKey
DISCOVERY_CACHE_MINUTES=60
DISCOVERY_RESULT_LIMIT=3
```

然后在“下一次”点击“更新近期灵感”。页面会展示当前已连接来源、更新时间和可跳转的原文证据。没有配置 Key 或某个平台临时失败时，推荐、地图和路线仍可使用，并明确显示降级状态。

发现服务遵循以下边界：不绕过登录、验证码和平台访问控制；不使用个人 Cookie；不复制整篇正文、视频或图片。后续可以在统一的 `ContentDiscoveryProvider` 接口下增加知乎、微博和抖音官方适配器，不影响现有推荐流程。

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
