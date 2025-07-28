# 使用多阶段构建来优化镜像大小
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src src
RUN mvn clean package -DskipTests

# 运行时镜像
FROM eclipse-temurin:17-jre-alpine

# 安装必要的工具
RUN apk add --no-cache curl

# 创建非root用户
RUN addgroup -g 1001 -S ecowiki && \
    adduser -S ecowiki -u 1001 -G ecowiki

WORKDIR /app

# 复制构建的 JAR 文件
COPY --from=builder /app/target/*.jar app.jar

# 设置文件权限
RUN chown -R ecowiki:ecowiki /app
USER ecowiki

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 设置 JVM 参数和启动应用
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
