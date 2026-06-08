# 多阶段构建
FROM node:18-alpine AS builder

WORKDIR /build

# 复制 package 文件
COPY package*.json ./

# 安装依赖
RUN npm ci --no-audit --no-fund

# 复制源码
COPY . .

# 构建生产版本
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# 运行时镜像
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
