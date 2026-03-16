package com.codereview.backend.service;

import com.codereview.backend.dto.CodeTask;
import com.codereview.backend.dto.ExecutionResult;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DockerSandboxService {

    @Value("${docker.image:java-sandbox:latest}")
    private String dockerImage;

    @Value("${docker.timeout:30}")
    private int timeoutSeconds;

    @Value("${docker.memory:64}")
    private long memoryLimitMB;

    @Autowired
    private DockerClient dockerClient;

    /**
     * 供 TaskProcessor 调用的统一执行入口
     */
    public ExecutionResult executeInSandbox(CodeTask task) {
        ExecutionResult result = executeJavaCode(
                task.getCode(),
                task.getClassName(),
                ""
        );
        // 设置 taskId 和 timestamp
        result.setTaskId(task.getTaskId());
        if (result.getTimestamp() == null) {
            result.setTimestamp(System.currentTimeMillis());
        }
        return result;

    }

    /**
     * 执行 Java 代码
     */
    public ExecutionResult executeJavaCode(String javaCode, String className, String input) {
        long startTime = System.currentTimeMillis();
        String containerId = null;

        try {
            // 1. 创建容器
            containerId = createContainer();
            log.info("创建容器成功: {}", containerId);

            // 2. 使用 Base64 安全地写入代码
            copyCodeToContainer(containerId, javaCode, className);

            // 3. 编译代码
            ExecutionResult compileResult = compileCode(containerId, className);
            if (!compileResult.getSuccess()) {
                return compileResult;
            }

            // 4. 执行代码
            ExecutionResult runResult = runCode(containerId, className, input);
            runResult.setExecutionTime(System.currentTimeMillis() - startTime);
            return runResult;

        } catch (Exception e) {
            log.error("执行代码失败", e);
            return ExecutionResult.builder()
                    .success(false)
                    .error("执行失败: " + e.getMessage())
                    .executionTime(System.currentTimeMillis() - startTime)
                    .build();
        } finally {
            // 5. 清理容器
            if (containerId != null) {
                removeContainer(containerId);
            }
        }
    }

    /**
     * 创建容器
     */
    private String createContainer() {
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withMemory(memoryLimitMB * 1024 * 1024)
                .withCpuCount(1L);

        CreateContainerResponse response = dockerClient.createContainerCmd(dockerImage)
                .withHostConfig(hostConfig)
                .withTty(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        String containerId = response.getId();
        dockerClient.startContainerCmd(containerId).exec();
        return containerId;
    }

    /**
     * 使用 Base64 安全地复制代码到容器
     */
    private void copyCodeToContainer(String containerId, String javaCode, String className) {
        try {
            String encodedCode = Base64.getEncoder().encodeToString(
                    javaCode.getBytes(StandardCharsets.UTF_8)
            );

            String cmd = String.format(
                    "mkdir -p /sandbox && echo '%s' | base64 -d > /sandbox/%s.java",
                    encodedCode, className
            );

            executeCommand(containerId, cmd);
            log.info("代码已写入容器: /sandbox/{}.java", className);

        } catch (Exception e) {
            log.error("复制代码到容器失败", e);
            throw new RuntimeException("复制代码失败: " + e.getMessage());
        }
    }

    /**
     * 编译代码
     */
    private ExecutionResult compileCode(String containerId, String className) {
        try {
            String compileCmd = String.format("javac /sandbox/%s.java", className);
            String output = executeCommand(containerId, compileCmd);

            return ExecutionResult.builder()
                    .success(true)
                    .output(output)
                    .build();

        } catch (Exception e) {
            return ExecutionResult.builder()
                    .success(false)
                    .error("编译错误:\n" + e.getMessage())
                    .build();
        }
    }

    /**
     * 执行代码
     */
    private ExecutionResult runCode(String containerId, String className, String input) {
        try {
            String runCmd = String.format("cd /sandbox && java %s", className);
            String output = executeCommand(containerId, runCmd);

            return ExecutionResult.builder()
                    .success(true)
                    .output(output.trim())
                    .build();

        } catch (Exception e) {
            return ExecutionResult.builder()
                    .success(false)
                    .error("运行错误:\n" + e.getMessage())
                    .build();
        }
    }

    /**
     * 在容器中执行命令
     */
    private String executeCommand(String containerId, String command) throws Exception {
        ExecCreateCmdResponse execCreate = dockerClient.execCreateCmd(containerId)
                .withCmd("sh", "-c", command)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        dockerClient.execStartCmd(execCreate.getId())
                .exec(new ExecStartResultCallback(outputStream, errorStream))
                .awaitCompletion(timeoutSeconds, TimeUnit.SECONDS);

        String output = outputStream.toString(StandardCharsets.UTF_8);
        String error = errorStream.toString(StandardCharsets.UTF_8);

        if (!error.isEmpty()) {
            throw new RuntimeException(error);
        }

        return output;
    }

    /**
     * 清理容器
     */
    private void removeContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).withTimeout(0).exec();
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
            log.info("容器已清理: {}", containerId);
        } catch (Exception e) {
            log.warn("清理容器失败: {}", e.getMessage());
        }
    }
}