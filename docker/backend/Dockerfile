# 第一阶段：构建
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# 先复制父项目 pom.xml（关键！）
COPY pom.xml ./pom.xml
COPY backend/pom.xml ./backend/pom.xml
COPY backend/src ./backend/src

# 在父项目层级构建，但只构建 backend 模块
RUN mvn clean package -pl backend -am -DskipTests

# 第二阶段：运行
FROM eclipse-temurin:17-jdk

WORKDIR /app

# 从 backend 模块的 target 目录复制 jar 包
COPY --from=builder /app/backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]