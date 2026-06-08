#!/bin/bash
# 一键部署脚本

set -e

echo "=========================================="
echo "  XFlow Todo 一键部署"
echo "=========================================="

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo "Error: Docker 未安装"
    exit 1
fi

# 检查 .env 文件
if [ ! -f .env ]; then
    echo "Error: .env 文件不存在"
    echo "请先复制 .env.example 为 .env 并填写配置"
    echo "  cp .env.example .env"
    echo "  nano .env"
    exit 1
fi

# 创建必要目录
mkdir -p mysql/data mysql/conf
mkdir -p logs/backend logs/nginx
mkdir -p nginx/ssl
mkdir -p backups

# 生成 JWT 密钥（如果未设置）
if grep -q "Replace_This" .env; then
    echo "正在生成 JWT 密钥..."
    JWT_SECRET=$(openssl rand -base64 48)
    sed -i "s|JWT_SECRET=.*|JWT_SECRET=$JWT_SECRET|" .env
    echo "JWT 密钥已生成并保存到 .env"
fi

# 检查 SSL 证书
if [ ! -f nginx/ssl/cert.pem ] || [ ! -f nginx/ssl/key.pem ]; then
    echo "警告: SSL 证书不存在，HTTPS 将无法工作"
    echo "请将证书文件放到 nginx/ssl/ 目录:"
    echo "  - cert.pem (证书)"
    echo "  - key.pem  (私钥)"
    echo ""
    read -p "是否继续部署（不启用 HTTPS）? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# 构建并启动
echo "正在构建 Docker 镜像..."
docker-compose build

echo "正在启动服务..."
docker-compose up -d

# 等待服务启动
echo "等待服务启动..."
sleep 15

# 检查状态
echo ""
echo "=========================================="
echo "  服务状态"
echo "=========================================="
docker-compose ps

echo ""
echo "=========================================="
echo "  部署完成！"
echo "=========================================="
echo "默认管理员账户: admin / admin123"
echo "请访问 http://your-domain.com 登录"
echo "登录后请立即修改默认密码！"
echo ""
echo "常用命令:"
echo "  查看日志: docker-compose logs -f"
echo "  停止服务: docker-compose down"
echo "  重启服务: docker-compose restart"
echo "=========================================="
