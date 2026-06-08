# 部署指南 (Deployment Guide)

## 📋 目录
1. [部署架构](#部署架构)
2. [方案一：Docker 部署（推荐）](#方案一docker-部署推荐)
3. [方案二：传统服务器部署](#方案二传统服务器部署)
4. [生产环境配置](#生产环境配置)
5. [Nginx 反向代理](#nginx-反向代理)
6. [HTTPS 配置](#https-配置)
7. [常见问题](#常见问题)

## 部署架构

```
┌─────────────────────────────────────────┐
│              用户浏览器                   │
└────────────────┬────────────────────────┘
                 │ HTTPS
                 ▼
┌─────────────────────────────────────────┐
│         Nginx (80/443)                  │
│    ┌──────────────┬──────────────┐      │
│    │ 静态文件      │ 反向代理     │      │
│    │ (前端)        │ /api → 后端 │      │
│    └──────────────┴──────────────┘      │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│      Spring Boot (8080)                 │
│         后端 API 服务                    │
└────────────────┬────────────────────────┘
                 │ JDBC
                 ▼
┌─────────────────────────────────────────┐
│         MySQL (3306)                    │
│           数据库                         │
└─────────────────────────────────────────┘
```

## 方案一：Docker 部署（推荐）

### 1. 服务器要求
- Linux 服务器（Ubuntu 20.04+ / CentOS 7+）
- 2 核 CPU，4GB 内存，40GB 硬盘
- 已安装 Docker 20+ 和 Docker Compose

### 2. 安装 Docker
```bash
# Ubuntu
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
systemctl enable docker
systemctl start docker

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 3. 项目打包

#### 后端打包
```bash
cd my-admin-backend
./mvnw clean package -DskipTests
# 生成 target/my-admin-backend-0.0.1-SNAPSHOT.jar
```

#### 前端打包
```bash
cd my-admin-frontend
# 修改 .env.production 中的 API 地址
echo "VITE_API_BASE_URL=https://your-domain.com/api" > .env.production
npm run build
# 生成 dist/ 目录
```

### 4. 服务器目录结构
```
/opt/xflow/
├── docker-compose.yml
├── nginx/
│   ├── nginx.conf
│   └── ssl/
│       ├── cert.pem
│       └── key.pem
├── mysql/
│   ├── data/
│   └── conf/
└── logs/
    ├── backend/
    └── nginx/
```

### 5. 创建 Dockerfile

**后端 `Dockerfile`**（放在 `my-admin-backend/` 根目录）：
```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/my-admin-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

**前端 `Dockerfile`**（放在 `my-admin-frontend/` 根目录）：
```dockerfile
FROM nginx:alpine

COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
```

### 6. 创建 docker-compose.yml

在服务器 `/opt/xflow/` 目录创建：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: xflow-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - ./mysql/data:/var/lib/mysql
      - ./mysql/conf:/etc/mysql/conf.d
    command:
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --default-authentication-plugin=mysql_native_password
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-p${MYSQL_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - xflow-net

  backend:
    build: ./my-admin-backend
    container_name: xflow-backend
    restart: always
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: 86400000
    ports:
      - "8080:8080"
    volumes:
      - ./logs/backend:/app/logs
    networks:
      - xflow-net

  frontend:
    build: ./my-admin-frontend
    container_name: xflow-frontend
    restart: always
    depends_on:
      - backend
    ports:
      - "80:80"
    networks:
      - xflow-net

  nginx:
    image: nginx:alpine
    container_name: xflow-nginx
    restart: always
    depends_on:
      - backend
      - frontend
    ports:
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
    networks:
      - xflow-net

networks:
  xflow-net:
    driver: bridge
```

### 7. 创建 .env 文件

在 `/opt/xflow/` 目录创建 `.env`：

```bash
# MySQL 配置
MYSQL_ROOT_PASSWORD=YourStrongPassword123!
MYSQL_DATABASE=xflow_db

# JWT 密钥（至少 32 字符，使用 openssl rand -base64 32 生成）
JWT_SECRET=your-super-secret-jwt-key-replace-this-with-random-string
```

### 8. Nginx 配置

创建 `/opt/xflow/nginx/nginx.conf`：

```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    sendfile        on;
    tcp_nopush      on;
    keepalive_timeout  65;
    client_max_body_size 20M;

    # gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/json application/javascript application/xml+rss application/atom+xml image/svg+xml;

    # HTTP 重定向到 HTTPS
    server {
        listen 80;
        server_name your-domain.com;
        return 301 https://$server_name$request_uri;
    }

    # HTTPS
    server {
        listen 443 ssl http2;
        server_name your-domain.com;

        ssl_certificate     /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;
        ssl_session_timeout 5m;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
        ssl_prefer_server_ciphers on;

        # 前端静态文件
        location / {
            root /usr/share/nginx/html;
            index index.html;
            try_files $uri $uri/ /index.html;

            # 缓存策略
            location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf)$ {
                expires 1y;
                add_header Cache-Control "public, immutable";
            }
        }

        # 后端 API 反向代理
        location /api/ {
            proxy_pass http://backend:8080/api/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_read_timeout 60s;
            proxy_connect_timeout 60s;
            proxy_buffering off;
        }
    }
}
```

### 9. 启动服务

```bash
cd /opt/xflow

# 启动所有服务
docker-compose up -d

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f backend

# 停止服务
docker-compose down
```

### 10. 初始化数据

容器启动后会自动创建表结构和默认管理员账户：
- 用户名：`admin`
- 密码：`admin123`

**首次登录后请立即修改密码！**

## 方案二：传统服务器部署

### 1. 服务器环境准备

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install -y openjdk-17-jdk nginx mysql-server

# CentOS
sudo yum install -y java-17-openjdk nginx mysql-server
```

### 2. 部署后端

```bash
# 创建目录
sudo mkdir -p /opt/xflow/backend
sudo mkdir -p /opt/xflow/logs

# 上传 jar 包
scp my-admin-backend-0.0.1-SNAPSHOT.jar user@server:/opt/xflow/backend/

# 创建 application.properties
sudo nano /opt/xflow/backend/application.properties
```

```properties
spring.application.name=my-admin-backend
spring.datasource.url=jdbc:mysql://localhost:3306/xflow_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=xflow_user
spring.datasource.password=YourStrongPassword
spring.jpa.hibernate.ddl-auto=update
jwt.secret=your-jwt-secret-at-least-32-chars
jwt.expiration=86400000
server.port=8080
```

### 3. 创建 systemd 服务

```bash
sudo nano /etc/systemd/system/xflow-backend.service
```

```ini
[Unit]
Description=XFlow Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=xflow
WorkingDirectory=/opt/xflow/backend
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod -Xms512m -Xmx1024m /opt/xflow/backend/my-admin-backend-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=append:/opt/xflow/logs/backend.log
StandardError=append:/opt/xflow/logs/backend-error.log

[Install]
WantedBy=multi-user.target
```

```bash
# 创建用户
sudo useradd -r -s /bin/false xflow
sudo chown -R xflow:xflow /opt/xflow

# 启动服务
sudo systemctl daemon-reload
sudo systemctl enable xflow-backend
sudo systemctl start xflow-backend
sudo systemctl status xflow-backend
```

### 4. 部署前端

```bash
# 上传 dist 目录
scp -r my-admin-frontend/dist/* user@server:/opt/xflow/frontend/

sudo chown -R www-data:www-data /opt/xflow/frontend
```

### 5. 配置 Nginx（参考方案一的配置）

```bash
sudo nano /etc/nginx/sites-available/xflow
```

启用配置：
```bash
sudo ln -s /etc/nginx/sites-available/xflow /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## 生产环境配置

### 后端生产环境配置

创建 `application-prod.properties`：

```properties
# 生产环境
spring.jpa.show-sql=false
spring.jpa.open-in-view=false

# 日志
logging.level.root=INFO
logging.level.com.example=INFO
logging.file.name=/app/logs/application.log
logging.file.max-size=100MB
logging.file.max-history=30

# 性能
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

## HTTPS 配置

### 使用 Let's Encrypt 免费证书

```bash
# 安装 certbot
sudo apt install certbot python3-certbot-nginx

# 申请证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo crontab -e
# 添加：0 3 * * * certbot renew --quiet
```

### 上传到 GitHub（可选）

将代码推送到 GitHub 私有仓库，方便在服务器上拉取更新。

## 数据库备份

### 创建备份脚本

```bash
sudo nano /opt/xflow/backup.sh
```

```bash
#!/bin/bash
BACKUP_DIR=/opt/xflow/backups
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

# 备份数据库
docker exec xflow-mysql mysqldump -uroot -p$MYSQL_PASSWORD xflow_db | gzip > $BACKUP_DIR/db_$DATE.sql.gz

# 保留最近 30 天备份
find $BACKUP_DIR -name "db_*.sql.gz" -mtime +30 -delete

echo "Backup completed: db_$DATE.sql.gz"
```

```bash
chmod +x /opt/xflow/backup.sh

# 添加定时任务
sudo crontab -e
# 每天凌晨 3 点备份
0 3 * * * /opt/xflow/backup.sh
```

## 监控与日志

```bash
# 查看容器状态
docker-compose ps

# 查看实时日志
docker-compose logs -f backend

# 查看资源使用
docker stats

# 查看 Nginx 日志
docker-compose logs -f nginx
```

## 常见问题

### 1. 端口被占用
```bash
# 查看端口占用
sudo lsof -i :8080
sudo netstat -tlnp | grep 8080
```

### 2. 数据库连接失败
```bash
# 进入 MySQL 容器
docker exec -it xflow-mysql bash
mysql -uroot -p
```

### 3. 前端 404
确保 Nginx 配置中有 `try_files $uri $uri/ /index.html;`

### 4. CORS 跨域问题
后端 `SecurityConfig` 中已配置 CORS，生产环境需要修改为正式域名。

### 5. 性能调优
```bash
# 调整 JVM 参数
JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

## 部署后检查清单

- [ ] 修改默认管理员密码
- [ ] 修改 JWT 密钥为强随机字符串
- [ ] 修改 MySQL root 密码
- [ ] 配置 HTTPS 证书
- [ ] 配置防火墙规则（开放 80/443，关闭 3306/8080 外网访问）
- [ ] 配置数据库自动备份
- [ ] 配置日志轮转
- [ ] 设置监控告警
- [ ] 测试登录、创建、删除、导出功能
- [ ] 检查移动端访问

## 升级部署

```bash
# 1. 拉取最新代码
cd /opt/xflow
git pull

# 2. 重新构建
docker-compose build

# 3. 重启服务
docker-compose up -d

# 4. 查看日志确认启动成功
docker-compose logs -f backend
```