# 第一阶段：构建
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# 配置阿里云 Maven 镜像（加速下载）
RUN mkdir -p /root/.m2 && \
    echo '<settings><mirrors><mirror><id>aliyun</id><mirrorOf>central</mirrorOf><name>Aliyun Maven</name><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

# 先复制 pom 文件，利用 Docker 缓存层
COPY pom.xml ./pom.xml
COPY backend/pom.xml ./backend/pom.xml

# 下载依赖（这一层会被缓存！）
RUN mvn dependency:go-offline -pl backend -am -B

# 复制源代码
COPY backend/src ./backend/src

# 构建（移除 -o 参数，允许下载缺失的依赖）
RUN mvn clean package -pl backend -am -DskipTests

# 第二阶段：运行
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 从 builder 复制 jar 包
COPY --from=builder /app/backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
