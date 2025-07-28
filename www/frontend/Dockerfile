# 构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

# 安装 pnpm
RUN npm install -g pnpm

# 复制 package.json 和 pnpm-lock.yaml（利用 Docker 缓存）
COPY package.json pnpm-lock.yaml ./

# 安装依赖
RUN pnpm install --frozen-lockfile

# 复制源代码
COPY . .

# 构建应用
RUN pnpm run build

# 生产阶段 - 使用 nginx 提供静态文件服务
FROM nginx:alpine

# 安装必要的工具
RUN apk add --no-cache curl

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/nginx.conf

# 复制构建的静态文件
COPY --from=builder /app/dist /usr/share/nginx/html

# 创建非root用户
RUN addgroup -g 1001 -S ecowiki && \
    adduser -S ecowiki -u 1001 -G ecowiki

# 设置目录权限
RUN chown -R ecowiki:ecowiki /usr/share/nginx/html && \
    chown -R ecowiki:ecowiki /var/cache/nginx && \
    chown -R ecowiki:ecowiki /var/log/nginx && \
    chown -R ecowiki:ecowiki /etc/nginx/conf.d

# 修改 nginx 配置文件权限
RUN touch /var/run/nginx.pid && \
    chown -R ecowiki:ecowiki /var/run/nginx.pid

USER ecowiki

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:80/ || exit 1

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
