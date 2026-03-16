# ===== 第一步：选择基础镜像 =====
# eclipse-temurin 是官方推荐的 OpenJDK 发行版
FROM eclipse-temurin:17-jdk

LABEL authors="thsa"
LABEL description="Java Code Execution Sandbox"

# ===== 第二步：安装必要工具 =====
RUN apt-get update && apt-get install -y \
    coreutils \
    && rm -rf /var/lib/apt/lists/*

# ===== 第三步：创建安全用户 =====
# 为什么不用 root？防止用户代码执行危险操作！
RUN useradd -m -s /bin/bash sandbox

# ===== 第四步：创建工作目录 =====
RUN mkdir -p /sandbox && chown sandbox:sandbox /sandbox

# ===== 第五步：设置工作目录 =====
WORKDIR /sandbox

# ===== 第六步：切换用户 =====
USER sandbox

# ===== 第七步：保持容器运行 =====
ENTRYPOINT ["sleep", "infinity"]