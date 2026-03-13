#!/bin/bash

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "🚀 开始构建项目..."

# 1. 构建后端
echo "📦 构建 Spring Boot 后端..."
cd backend
mvn clean package -DskipTests
cd ..

# 2. 准备 Docker 构建上下文
echo "📋 准备 Docker 构建上下文..."
mkdir -p docker/backend
cp backend/target/*.jar docker/backend/app.jar

# 3. 创建 Redis 配置目录
mkdir -p docker/redis

# 4. 构建 Docker 镜像
echo "🐳 构建 Docker 镜像..."
cd docker
docker-compose build

echo "✅ 构建完成！"
echo "💡 启动命令: cd docker && docker-compose up -d"