package com.javaevaluation.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.javaevaluation.dto.CodeTask;
import com.javaevaluation.dto.ExecutionResult;
import com.javaevaluation.entity.TestCase;
import com.javaevaluation.mapper.TestCaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Value("${docker.image:openjdk:11}")
    private String dockerImage;

    @Value("${docker.timeout:30}")
    private int timeoutSeconds;

    @Value("${docker.memory:256m}")
    private String memoryLimit;

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    /**
     * 执行评测任务
     */
    public ExecutionResult executeTask(CodeTask task) {
        ExecutionResult result = new ExecutionResult();
        result.setTaskId(task.getTaskId());

        try {
            // 1. 读取源代码文件
            String sourceCode = readSourceCode(task.getFilePaths());
            if (sourceCode == null) {
                result.setCompileStatus("FAILED");
                result.setErrorMessage("无法读取源代码文件");
                return result;
            }

            // 2. 获取测试用例
            List<TestCase> testCases = testCaseMapper.findByHomeworkId(task.getHomeworkId());
            if (testCases.isEmpty()) {
                result.setCompileStatus("FAILED");
                result.setErrorMessage("没有找到测试用例");
                return result;
            }

            // 3. 在Docker中编译和执行
            return executeInDocker(task, sourceCode, testCases);

        } catch (Exception e) {
            log.error("执行任务失败: taskId={}, error={}", task.getTaskId(), e.getMessage(), e);
            result.setCompileStatus("ERROR");
            result.setErrorMessage(e.getMessage());
            return result;
        }
    }

    /**
     * 读取源代码
     */
    private String readSourceCode(String[] filePaths) {
        if (filePaths == null || filePaths.length == 0) {
            return null;
        }

        StringBuilder code = new StringBuilder();
        for (String filePath : filePaths) {
            try {
                Path path = Paths.get(uploadPath, filePath);
                if (Files.exists(path)) {
                    String content = Files.readString(path, StandardCharsets.UTF_8);
                    code.append("// File: ").append(filePath).append("\n");
                    code.append(content).append("\n\n");
                }
            } catch (Exception e) {
                log.error("读取文件失败: {}", filePath, e);
            }
        }
        return code.length() > 0 ? code.toString() : null;
    }

    /**
     * 在Docker中执行
     */
    private ExecutionResult executeInDocker(CodeTask task, String sourceCode, List<TestCase> testCases) {
        ExecutionResult result = new ExecutionResult();
        result.setTaskId(task.getTaskId());

        String containerId = null;
        String workDir = "/tmp/eval_" + UUID.randomUUID().toString().replace("-", "");

        try {
            // 1. 创建工作目录
            File localWorkDir = new File(workDir);
            if (!localWorkDir.mkdirs()) {
                throw new RuntimeException("无法创建工作目录");
            }

            // 2. 写入源代码文件
            String className = extractClassName(sourceCode);
            File sourceFile = new File(localWorkDir, className + ".java");
            Files.writeString(sourceFile.toPath(), sourceCode, StandardCharsets.UTF_8);

            // 3. 创建Docker容器
            CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withMemory(parseMemory(memoryLimit))
                            .withBinds(new Bind(localWorkDir.getAbsolutePath(), new Volume("/app")))
                            .withAutoRemove(true))
                    .withWorkingDir("/app")
                    .withCmd("tail", "-f", "/dev/null")  // 保持容器运行
                    .exec();
            containerId = container.getId();
            dockerClient.startContainerCmd(containerId).exec();

            // 4. 编译代码
            ExecutionResult compileResult = compileInContainer(containerId, className);
            if (!"SUCCESS".equals(compileResult.getCompileStatus())) {
                return compileResult;
            }

            // 5. 执行测试用例
            int passed = 0;
            int total = testCases.size();
            List<String> testResults = new ArrayList<>();

            for (TestCase testCase : testCases) {
                TestResult testResult = runTestCase(containerId, className, testCase);
                testResults.add(String.format("测试用例[%s]: %s",
                        testCase.getName(),
                        testResult.passed ? "通过" : "失败"));
                if (testResult.passed) {
                    passed++;
                }
            }

            // 6. 设置结果
            result.setCompileStatus("SUCCESS");
            result.setTestPassed(passed);
            result.setTestTotal(total);
            result.setTestScore(calculateScore(passed, total));
            result.setOutput(String.join("\n", testResults));

            log.info("评测完成: taskId={}, passed={}/{}", task.getTaskId(), passed, total);

        } catch (Exception e) {
            log.error("Docker执行失败: {}", e.getMessage(), e);
            result.setCompileStatus("ERROR");
            result.setErrorMessage(e.getMessage());
        } finally {
            // 清理容器
            if (containerId != null) {
                try {
                    dockerClient.stopContainerCmd(containerId).exec();
                } catch (Exception ignored) {
                }
            }
            // 清理工作目录
            deleteDirectory(new File(workDir));
        }

        return result;
    }

    /**
     * 在容器中编译代码
     */
    private ExecutionResult compileInContainer(String containerId, String className) {
        ExecutionResult result = new ExecutionResult();

        try {
            // 执行javac编译
            ExecCreateCmdResponse execCreate = dockerClient.execCreateCmd(containerId)
                    .withCmd("javac", className + ".java")
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            String output = executeCommand(containerId, execCreate);

            // 检查编译结果
            if (output.contains("error") || output.contains("错误")) {
                result.setCompileStatus("COMPILE_ERROR");
                result.setErrorMessage(output);
            } else {
                result.setCompileStatus("SUCCESS");
            }

        } catch (Exception e) {
            result.setCompileStatus("ERROR");
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    /**
     * 执行单个测试用例
     */
    private TestResult runTestCase(String containerId, String className, TestCase testCase) {
        TestResult result = new TestResult();

        try {
            // 执行java程序
            ExecCreateCmdResponse execCreate = dockerClient.execCreateCmd(containerId)
                    .withCmd("java", "-cp", ".", className)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .exec();

            String output = executeCommandWithInput(containerId, execCreate, testCase.getInput());

            // 比较输出
            String expected = testCase.getExpectedOutput().trim();
            String actual = output.trim();
            result.passed = expected.equals(actual);
            result.output = output;

        } catch (Exception e) {
            result.passed = false;
            result.output = "执行异常: " + e.getMessage();
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
            log.error("执行命令失败", e);
        }
        return output.toString();
    }

    /**
     * 执行命令（带输入）
     */
    private String executeCommandWithInput(String containerId, ExecCreateCmdResponse execCreate, String input) {
        // 简化实现，实际需要处理stdin
        return executeCommand(containerId, execCreate);
    }

    /**
     * 从源代码中提取类名
     */
    private String extractClassName(String sourceCode) {
        // 简单提取public class后面的类名
        String pattern = "public\\s+class\\s+(\\w+)";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(sourceCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Main";  // 默认类名
    }

    /**
     * 计算分数（按通过比例计算，满分100分）
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