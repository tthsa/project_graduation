package com.javaevaluation.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class DockerConfig {

    @Value("${docker.host:unix:///var/run/docker.sock}")
    private String dockerHost;

    @Bean
    public DockerClient dockerClient() {
        log.info("🐳 初始化 Docker 客户端, host: {}", dockerHost);

        try {
            // Docker 配置
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost(dockerHost)
                    .build();

            // ✅ 关键修复：使用 ZerodepDockerHttpClient
            DockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .maxConnections(100)
                    .connectionTimeout(Duration.ofSeconds(30))
                    .responseTimeout(Duration.ofSeconds(45))
                    .build();

            // 创建 Docker 客户端
            DockerClient client = DockerClientImpl.getInstance(config, httpClient);

            // 测试连接
            client.pingCmd().exec();
            log.info("✅ Docker 连接成功");

            return client;
        } catch (Exception e) {
            log.error("❌ Docker 连接失败: {}", e.getMessage());
            throw new RuntimeException("Docker 连接失败", e);
        }
    }
}