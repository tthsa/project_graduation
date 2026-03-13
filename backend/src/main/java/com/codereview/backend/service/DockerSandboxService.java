package com.codereview.backend.service;

import com.codereview.backend.dto.CodeTask;
import com.codereview.backend.dto.ExecutionResult;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
@Slf4j
@Service
@RequiredArgsConstructor

public class DockerSandboxService {

    private final DockerClient dockerClient;

    @Value("${docker.image:java-sandbox:latest}")
    private String dockerImage;

    /**
     * 在 Docker 容器中执行代码
     *
     * @param task 代码任务
     * @return 执行结果
     */
    public ExecutionResult executeInSandbox(CodeTask task) {
        String containerId = null;
        long startTime = System.currentTimeMillis();

        try {
            // 1. 创建容器
            containerId = createContainer(task);
            log.info("🐳 创建容器: containerId={}", containerId);

            // 2. 启动容器
            dockerClient.startContainerCmd(containerId).exec();
            log.info("▶️ 启动容器: containerId={}", containerId);

            // 3. 复制代码到容器
            copyCodeToContainer(containerId, task);
            log.info("📝 复制代码到容器: className={}", task.getClassName());

            // 4. 编译代码
            ExecutionResult compileResult = compileCode(containerId, task);
            if (!compileResult.getSuccess()) {
                return compileResult;
            }

            // 5. 运行代码
            ExecutionResult runResult = runCode(containerId, task);

            // 6. 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            runResult.setExecutionTime(executionTime);

            return runResult;

        } catch (Exception e) {
            log.error("❌ 执行失败: {}", e.getMessage(), e);
            return ExecutionResult.builder()
                    .taskId(task.getTaskId())
                    .success(false)
                    .error("执行失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        } finally {
            // 7. 清理容器
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId)
                            .withForce(true)
                            .exec();
                    log.info("🗑️ 清理容器: containerId={}", containerId);
                } catch (Exception e) {
                    log.warn("⚠️ 清理容器失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 创建容器
     */
    private String createContainer(CodeTask task) {
        // 资源限制配置
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withMemory((long) task.getMemoryLimit() * 1024 * 1024) // MB -> Bytes
                .withCpuCount(1L)
                .withNetworkMode("none") // 禁用网络
                .withAutoRemove(false);

        // 创建容器
        CreateContainerResponse response = dockerClient.createContainerCmd(dockerImage)
                .withHostConfig(hostConfig)
                .withTty(true)
                .withStdinOpen(true)
                .withUser("sandbox") // 使用非 root 用户
                .exec();

        return response.getId();
    }

    /**
     * 复制代码到容器
     */
    private void copyCodeToContainer(String containerId, CodeTask task) throws Exception {
        // 创建 Java 文件内容
        String javaCode = task.getCode();

        // 使用 exec 命令创建文件
        String createFileCmd = String.format(
                "mkdir -p /sandbox && echo '%s' > /sandbox/%s.java",
                escapeShell(javaCode),
                task.getClassName()
        );

        executeCommand(containerId, new String[]{"sh", "-c", createFileCmd});
    }

    /**
     * 编译代码
     */
    private ExecutionResult compileCode(String containerId, CodeTask task) throws Exception {
        String compileCmd = String.format(
                "cd /sandbox && javac %s.java 2>&1",
                task.getClassName()
        );

        CommandResult result = executeCommand(containerId, new String[]{"sh", "-c", compileCmd});

        if (result.getExitCode() != 0) {
            return ExecutionResult.builder()
                    .taskId(task.getTaskId())
                    .success(false)
                    .error("编译错误:\n" + result.getOutput())
                    .exitCode(result.getExitCode())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        return ExecutionResult.builder()
                .taskId(task.getTaskId())
                .success(true)
                .message("编译成功")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 运行代码
     */
    private ExecutionResult runCode(String containerId, CodeTask task) throws Exception {
        String runCmd = String.format(
                "cd /sandbox && timeout %d java -Xmx%dM %s 2>&1",
                task.getTimeLimit(),
                task.getMemoryLimit(),
                task.getClassName()
        );

        CommandResult result = executeCommand(containerId, new String[]{"sh", "-c", runCmd});

        // 检查是否超时
        boolean success = result.getExitCode() == 0;
        String error = null;

        if (result.getExitCode() == 124) {
            success = false;
            error = "执行超时（超过 " + task.getTimeLimit() + " 秒）";
        } else if (result.getExitCode() != 0) {
            error = result.getOutput();
        }

        return ExecutionResult.builder()
                .taskId(task.getTaskId())
                .success(success)
                .output(success ? result.getOutput() : null)
                .error(error)
                .exitCode(result.getExitCode())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 在容器中执行命令
     */
    private CommandResult executeCommand(String containerId, String[] cmd) throws Exception {
        // 创建 exec 命令
        ExecCreateCmdResponse execCreate = dockerClient.execCreateCmd(containerId)
                .withCmd(cmd)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        // 执行命令并获取输出
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        dockerClient.execStartCmd(execCreate.getId())
                .exec(new ExecStartResultCallback(outputStream, errorStream))
                .awaitCompletion(30, TimeUnit.SECONDS);

        // 获取退出码
        Long exitCode = dockerClient.inspectExecCmd(execCreate.getId())
                .exec()
                .getExitCodeLong();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        String error = errorStream.toString(StandardCharsets.UTF_8);

        return new CommandResult(exitCode != null ? exitCode.intValue() : -1, output + error);
    }

    /**
     * 转义 Shell 特殊字符
     */
    private String escapeShell(String str) {
        return str.replace("'", "'\\''");
    }

    /**
     * 命令执行结果
     */
    private static class CommandResult {
        private final int exitCode;
        private final String output;

        public CommandResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }
    }

    /**
     * Exec 输出回调
     */
    private static class ExecStartResultCallback
            extends com.github.dockerjava.api.async.ResultCallback.Adapter<Frame>
            implements Closeable {

        private final ByteArrayOutputStream outputStream;
        private final ByteArrayOutputStream errorStream;

        public ExecStartResultCallback(ByteArrayOutputStream outputStream, ByteArrayOutputStream errorStream) {
            this.outputStream = outputStream;
            this.errorStream = errorStream;
        }

        @Override
        public void onNext(Frame frame) {
            if (frame != null && frame.getPayload() != null) {
                byte[] payload = frame.getPayload();
                if (frame.getStreamType() == StreamType.STDOUT) {
                    outputStream.write(payload, 0, payload.length);
                } else if (frame.getStreamType() == StreamType.STDERR) {
                    errorStream.write(payload, 0, payload.length);
                }
            }
        }

        @Override
        public void close() {
            // 清理资源
        }
    }
}
