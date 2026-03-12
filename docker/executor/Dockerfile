# ==================== Java执行器沙箱镜像 ====================
# 使用 Eclipse Temurin (OpenJDK官方推荐)
FROM eclipse-temurin:17-jdk

LABEL description="Java code execution sandbox"
LABEL version="1.0"
LABEL author="code-review-system"

# ==================== 创建目录结构 ====================
RUN mkdir -p /app/code /app/output /app/temp && \
    chown -R ubuntu:ubuntu /app

# ==================== 设置工作目录 ====================
WORKDIR /app

# ==================== 复制执行脚本 ====================
COPY --chown=ubuntu:ubuntu execute.sh /app/execute.sh
RUN chmod +x /app/execute.sh

# ==================== 切换到非root用户 ====================

USER ubuntu

# ==================== 设置环境变量 ====================
# JAVA_OPTS: Java 虚拟机参数
# -Xmx128m: 最大堆内存 128MB
# -Xms64m: 初始堆内存 64MB

ENV JAVA_OPTS="-Xmx128m -Xms64m"
ENV TIMEOUT=10

# ==================== 默认命令 ====================
CMD ["java", "-version"]