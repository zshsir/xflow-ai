# 部署清单

部署到生产环境前，请确保完成以下检查：

## 📦 部署文件清单

### 项目根目录
- [x] `README.md` - 项目说明
- [x] `DEPLOYMENT.md` - 详细部署文档
- [x] `docker-compose.yml` - Docker Compose 编排
- [x] `.env.example` - 环境变量示例
- [x] `.gitignore` - Git 忽略规则
- [x] `nginx/nginx.conf` - Nginx 配置
- [x] `scripts/deploy.sh` - 一键部署脚本
- [x] `scripts/backup.sh` - 数据库备份脚本

### 后端
- [x] `my-admin-backend/Dockerfile` - 后端 Docker 镜像
- [x] `my-admin-backend/.dockerignore`
- [x] `my-admin-backend/src/main/resources/application.properties.example`

### 前端
- [x] `my-admin-frontend/Dockerfile` - 前端 Docker 镜像
- [x] `my-admin-frontend/.dockerignore`
- [x] `my-admin-frontend/nginx.conf`
- [x] `my-admin-frontend/.env.example`

## 🚀 快速部署（5 步）

### 第 1 步：服务器准备
```bash
# 安装 Docker
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
systemctl enable docker && systemctl start docker

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 第 2 步：上传代码
```bash
# 在本地
scp -r xflow-ai/ user@your-server:/opt/
```

### 第 3 步：配置环境
```bash
# 在服务器
cd /opt/xflow-ai
cp .env.example .env
nano .env  # 修改 MYSQL_ROOT_PASSWORD 和 JWT_SECRET
```

### 第 4 步：准备 SSL 证书
```bash
# 上传 SSL 证书到 nginx/ssl/
# cert.pem - 证书
# key.pem  - 私钥
```

### 第 5 步：执行部署
```bash
chmod +x scripts/*.sh
sudo ./scripts/deploy.sh
```

## 🔐 部署后必做

1. **修改默认密码**：用 `admin/admin123` 登录后立即修改
2. **配置防火墙**：
   ```bash
   # 开放 80/443
   sudo ufw allow 80/tcp
   sudo ufw allow 443/tcp
   # 关闭 3306、8080 外网访问
   ```
3. **设置自动备份**：
   ```bash
   sudo crontab -e
   # 添加：0 3 * * * /opt/xflow-ai/scripts/backup.sh
   ```
4. **修改数据库默认密码**

## 📊 监控命令

```bash
# 查看容器状态
docker-compose ps

# 实时日志
docker-compose logs -f

# 资源使用
docker stats

# 进入后端容器
docker exec -it xflow-backend sh

# 进入 MySQL 容器
docker exec -it xflow-mysql bash
mysql -uroot -p
```

## 🔄 升级部署

```bash
# 拉取最新代码
cd /opt/xflow-ai
git pull  # 或重新上传代码

# 重新构建并启动
docker-compose build
docker-compose up -d

# 查看日志确认
docker-compose logs -f backend
```

## ⚠️ 安全检查清单

- [ ] 修改了默认管理员密码
- [ ] 修改了 MySQL root 密码
- [ ] 修改了 JWT 密钥（使用强随机字符串）
- [ ] 配置了 SSL/HTTPS
- [ ] 防火墙只开放 80/443
- [ ] 数据库不暴露外网
- [ ] 定期备份数据库
- [ ] 日志定期清理
