package com.javaevaluation.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.javaevaluation.dto.CodeTask;
import com.javaevaluation.dto.ExecutionResult;
import com.javaevaluation.entity.SubmissionFile;
import com.javaevaluation.entity.TestCase;
import com.javaevaluation.mapper.SubmissionFileMapper;
import com.javaevaluation.mapper.TestCaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Docker沙箱服务
 * 在Docker容器中安全执行Java代码
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DockerSandboxService {

    private final DockerClient dockerClient;
    private final TestCaseMapper testCaseMapper;
    private final SubmissionFileMapper submissionFileMapper;

    @Value("${docker.image:openjdk:11}")
    private String dockerImage;

    @Value("${docker.timeout:30}")
    private int timeoutSeconds;

    @Value("${docker.memory:256m}")
    private String memoryLimit;

    /**
     * 执行评测任务
     */
    public ExecutionResult executeTask(CodeTask task) {
        ExecutionResult result = new ExecutionResult();
        result.setTaskId(task.getTaskId());

        log.info("🚀 开始执行评测任务: taskId={}, submissionId={}", task.getTaskId(), task.getSubmissionId());

        try {
            // 1. 从数据库读取代码文件
            List<SubmissionFile> files = submissionFileMapper.findBySubmissionId(task.getSubmissionId());
            if (files == null || files.isEmpty()) {
                log.error("❌ 未找到提交的代码文件: submissionId={}", task.getSubmissionId());
                result.setCompileStatus("FAILED");
                result.setErrorMessage("未找到提交的代码文件");
                return result;
            }
            log.info("✅ 找到 {} 个代码文件", files.size());

            // 2. 获取测试用例
            List<TestCase> testCases = testCaseMapper.findByHomeworkId(task.getHomeworkId());
            if (testCases.isEmpty()) {
                log.error("❌ 没有找到测试用例: homeworkId={}", task.getHomeworkId());
                result.setCompileStatus("FAILED");
                result.setErrorMessage("没有找到测试用例");
                return result;
            }
            log.info("✅ 找到 {} 个测试用例", testCases.size());

            // 3. 在Docker中编译和执行
            return executeInDocker(task, files, testCases);

        } catch (Exception e) {
            log.error("❌ 执行任务失败: taskId={}, error={}", task.getTaskId(), e.getMessage(), e);
            result.setCompileStatus("ERROR");
            result.setErrorMessage(e.getMessage());
            return result;
        }
    }

    /**
     * 在Docker中执行
     */
    private ExecutionResult executeInDocker(CodeTask task, List<SubmissionFile> files, List<TestCase> testCases) {
        ExecutionResult result = new ExecutionResult();
        result.setTaskId(task.getTaskId());

        String containerId = null;
        String workDir = "/tmp/eval_" + UUID.randomUUID().toString().replace("-", "");

        log.info("🚀 开始Docker执行: workDir={}, image={}", workDir, dockerImage);

        try {
            // 1. 创建本地工作目录
            File localWorkDir = new File(workDir);
            if (!localWorkDir.mkdirs()) {
                log.error("❌ 无法创建工作目录: {}", workDir);
                throw new RuntimeException("无法创建工作目录");
            }
            log.info("✅ 工作目录创建成功: {}", workDir);

            // 2. 写入所有源代码文件到本地
            String mainClassName = null;
            for (SubmissionFile file : files) {
                File sourceFile = new File(localWorkDir, file.getFileName());
                Files.writeString(sourceFile.toPath(), file.getFileContent(), StandardCharsets.UTF_8);
                log.info("✅ 写入源文件: {}, 大小: {} bytes", file.getFileName(), file.getFileContent().length());

                // 查找包含main方法的类
                if (file.getFileContent().contains("public static void main")) {
                    mainClassName = extractClassName(file.getFileContent());
                    log.info("✅ 找到主类: {}", mainClassName);
                }
            }

            if (mainClassName == null) {
                log.error("❌ 未找到包含main方法的类");
                result.setCompileStatus("FAILED");
                result.setErrorMessage("未找到包含main方法的类");
                return result;
            }

            // 3. 创建Docker容器
            log.info("🐳 创建Docker容器: image={}", dockerImage);
            CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withMemory(parseMemory(memoryLimit))
                            .withAutoRemove(true))
                    .withWorkingDir("/sandbox")
                    .exec();
            containerId = container.getId();
            log.info("✅ 容器创建成功: containerId={}", containerId);

            // 4. 启动容器
            dockerClient.startContainerCmd(containerId).exec();
            log.info("✅ 容器启动成功");

            // 5. 复制文件到容器
            for (SubmissionFile file : files) {
                File sourceFile = new File(localWorkDir, file.getFileName());
                copyFileToContainer(containerId, sourceFile);
                log.info("✅ 复制文件到容器: {}", file.getFileName());
            }

            // 6. 编译所有代码
            log.info("🔨 开始编译代码...");
            ExecutionResult compileResult = compileAllInContainer(containerId, files);
            if (!"SUCCESS".equals(compileResult.getCompileStatus())) {
                log.error("❌ 编译失败: {}", compileResult.getErrorMessage());
                return compileResult;
            }
            log.info("✅ 编译成功");

            // 7. 执行测试用例
            int passed = 0;
            int total = testCases.size();
            List<String> testResults = new ArrayList<>();

            log.info("🧪 开始执行 {} 个测试用例...", total);
            for (TestCase testCase : testCases) {
                TestResult testResult = runTestCase(containerId, mainClassName, testCase, localWorkDir);
                testResults.add(String.format("测试用例[%s]: %s",
                        testCase.getName(),
                        testResult.passed ? "通过" : "失败"));
                log.info("🧪 测试用例[{}]: {}", testCase.getName(), testResult.passed ? "✅ 通过" : "❌ 失败");
                if (testResult.passed) {
                    passed++;
                }
            }

            // 8. 设置结果
            result.setCompileStatus("SUCCESS");
            result.setTestPassed(passed);
            result.setTestTotal(total);
            result.setTestScore(calculateScore(passed, total));
            result.setOutput(String.join("\n", testResults));

            log.info("🎉 评测完成: taskId={}, passed={}/{}, score={}", task.getTaskId(), passed, total, result.getTestScore());

        } catch (Exception e) {
            log.error("❌ Docker执行失败: {}", e.getMessage(), e);
            result.setCompileStatus("ERROR");
            result.setErrorMessage(e.getMessage());
        } finally {
            // 清理容器
            if (containerId != null) {
                try {
                    dockerClient.stopContainerCmd(containerId).exec();
                    log.info("🧹 容器已停止");
                } catch (Exception ignored) {
                }
            }
            // 清理工作目录
            deleteDirectory(new File(workDir));
            log.info("🧹 工作目录已清理");
        }

        return result;
    }

    /**
     * 复制文件到容器
     */
    private void copyFileToContainer(String containerId, File localFile) {
        try (ByteArrayOutputStream tarStream = new ByteArrayOutputStream();
             TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tarStream);
             FileInputStream fis = new FileInputStream(localFile)) {

            // 创建tar条目
            TarArchiveEntry entry = new TarArchiveEntry(localFile, localFile.getName());
            entry.setSize(localFile.length());
            tarOut.putArchiveEntry(entry);

            // 写入文件内容
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                tarOut.write(buffer, 0, len);
            }
            tarOut.closeArchiveEntry();
            tarOut.finish();

            // 复制到容器
            dockerClient.copyArchiveToContainerCmd(containerId)
                    .withTarInputStream(new ByteArrayInputStream(tarStream.toByteArray()))
                    .withRemotePath("/sandbox")
                    .exec();

        } catch (Exception e) {
            log.error("❌ 复制文件到容器失败: {}", e.getMessage(), e);
            throw new RuntimeException("复制文件到容器失败: " + e.getMessage());
        }
    }

    /**
     * 在容器中编译所有代码
     */
    private ExecutionResult compileAllInContainer(String containerId, List<SubmissionFile> files) {
        ExecutionResult result = new ExecutionResult();

        try {
            // 调试：列出容器内/sandbox目录内容
            ExecCreateCmdResponse debugExec = dockerClient.execCreateCmd(containerId)
                    .withCmd("ls", "-la", "/sandbox")
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            String debugOutput = executeCommand(containerId, debugExec);
            log.info("📁 容器内/sandbox目录内容:\n{}", debugOutput);

            // 构建javac命令，编译所有.java文件
            List<String> cmd = new ArrayList<>();
            cmd.add("javac");
            for (SubmissionFile file : files) {
                cmd.add(file.getFileName());
            }

            log.info("🔨 执行编译命令: {}", cmd);

            ExecCreateCmdResponse execCreate = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmd.toArray(new String[0]))
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            String output = executeCommand(containerId, execCreate);
            log.info("📝 编译输出: {}", output);

            // 检查编译结果
            if (output.contains("error") || output.contains("错误")) {
                result.setCompileStatus("COMPILE_ERROR");
                result.setErrorMessage(output);
            } else {
                result.setCompileStatus("SUCCESS");
            }

        } catch (Exception e) {
            log.error("❌ 编译异常: {}", e.getMessage(), e);
            result.setCompileStatus("ERROR");
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    /**
     * 执行单个测试用例
     */
    private TestResult runTestCase(String containerId, String className, TestCase testCase, File localWorkDir) {
        TestResult result = new TestResult();

        try {
            // 1. 将输入写入本地文件
            File inputFile = new File(localWorkDir, "input.txt");
            Files.writeString(inputFile.toPath(), testCase.getInput(), StandardCharsets.UTF_8);

            // 2. 复制输入文件到容器
            copyFileToContainer(containerId, inputFile);

            // 3. 在容器中执行
            ExecCreateCmdResponse execCreate = dockerClient.execCreateCmd(containerId)
                    .withCmd("sh", "-c", "java -cp . " + className + " < input.txt")
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            String output = executeCommand(containerId, execCreate);

            // 4. 比较输出
            String expected = testCase.getExpectedOutput().trim();
            String actual = output.trim();
            result.passed = expected.equals(actual);
            result.output = output;

            log.debug("测试用例[{}]: input={}, expected={}, actual={}, passed={}",
                    testCase.getName(), testCase.getInput(), expected, actual, result.passed);

        } catch (Exception e) {
            result.passed = false;
            result.output = "执行异常: " + e.getMessage();
            log.error("❌ 测试用例执行异常: {}", e.getMessage(), e);
        } finally {
            // 清理输入文件
            try {
                new File(localWorkDir, "input.txt").delete();
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    /**
     * 执行命令
     */
    private String executeCommand(String containerId, ExecCreateCmdResponse execCreate) {
        StringBuilder output = new StringBuilder();
        try {
            dockerClient.execStartCmd(execCreate.getId())
                    .exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<com.github.dockerjava.api.model.Frame>() {
                        @Override
                        public void onNext(com.github.dockerjava.api.model.Frame frame) {
                            output.append(new String(frame.getPayload()));
                        }
                    })
                    .awaitCompletion(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("❌ 执行命令失败", e);
        }
        return output.toString();
    }

    /**
     * 从源代码中提取类名
     */
    private String extractClassName(String sourceCode) {
        String pattern = "public\\s+class\\s+(\\w+)";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(sourceCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Main";
    }

    /**
     * 计算分数
     */
    private Integer calculateScore(int passed, int total) {
        if (total == 0) return 0;
        return (int) ((passed * 100.0) / total);
    }

    /**
     * 解析内存限制
     */
    private long parseMemory(String memory) {
        memory = memory.toLowerCase();
        if (memory.endsWith("m")) {
            return Long.parseLong(memory.replace("m", "")) * 1024 * 1024;
        } else if (memory.endsWith("g")) {
            return Long.parseLong(memory.replace("g", "")) * 1024 * 1024 * 1024;
        }
        return Long.parseLong(memory);
    }

    /**
     * 删除目录
     */
    private void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            dir.delete();
        }
    }

    /**
     * 测试结果内部类
     */
    private static class TestResult {
        boolean passed;
        String output;
    }
}