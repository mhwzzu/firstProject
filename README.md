# 两个人的漫游簿

一个用于记录情侣吃过的店、去过的地方和共同回忆，并推荐下一次旅行目的地的纪念系统。

现在包含私密登录、吃喝分类、奶茶地图，以及通过高德地图 Web API 搜索店铺/地址后保存坐标。

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

默认登录账号：

- 账号：`mhwzzu`
- 密码：`change-me-now`

可以在启动后端前通过环境变量修改：

```powershell
$env:APP_USERNAME="mhwzzu"
$env:APP_PASSWORD="你的新密码"
mvn spring-boot:run
```

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

店铺/地址搜索走后端代理接口 `/api/places/search`，需要配置高德 Web 服务 Key：

```powershell
$env:AMAP_KEY="你的高德Key"
mvn spring-boot:run
```

没有配置 `AMAP_KEY` 时，系统仍然可以手动填写地址、经纬度，并通过记录卡片里的“地图”链接打开高德地图坐标。

## 验证

```bash
cd backend
mvn test

cd ../frontend
npm run build
```

## 数据说明

- H2 模式的数据在后端重启后恢复为示例数据。
- PostgreSQL 模式的数据保存在 Docker volume `journey-postgres` 中。
- 正式部署前请复制 `.env.example` 为 `.env` 并修改数据库密码、登录密码和 `AMAP_KEY`，不要提交 `.env`。
