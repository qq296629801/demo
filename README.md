# iidp-backend-demo-ai

## Docker Compose 本地部署

```bash
# 1. 打包启动模块与业务 App
mvn -s ./settings.xml -DskipTests clean package

# 2. 可选：复制并按需修改端口/账号
cp .env.example .env

# 3. 启动 MySQL、Redis、MinIO 与 IIDP 应用
docker compose up -d --build

# 4. 查看应用日志
docker compose logs -f iidp-app
```

默认访问地址：

- IIDP 后端：http://localhost:8060
- Druid：http://localhost:8060/druid
- MinIO Console：http://localhost:9001

Docker 专用配置在 `docker/config/` 下，使用 compose 服务名 `mysql`、`redis`、`minio`。如果修改 `.env` 中的密码，也要同步调整 `docker/config/*.properties`。
