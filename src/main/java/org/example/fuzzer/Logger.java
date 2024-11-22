package org.example.fuzzer;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private final String outputDirPath; // 输出目录路径

    // 使用指定的目录路径初始化 Logger
    public Logger(String outputDirPath) {
        this.outputDirPath = outputDirPath;
        createDirectories();
    }

    // 默认构造函数，使用默认的相对路径
    public Logger() {
        this.outputDirPath = "src/main/java/org/example/output_dir"; // 设置为绝对路径
        createDirectories();
    }

    // 创建必要的目录：queue 和 crash
    private void createDirectories() {
        Path queueDir = Paths.get(outputDirPath, "queue");
        Path crashDir = Paths.get(outputDirPath, "crash");

        try {
            // 将路径转换为绝对路径
            Path outputPath = Paths.get(outputDirPath).toAbsolutePath();
            Files.createDirectories(outputPath); // 创建根目录

            // 创建 queue 和 crash 子目录
            if (!Files.exists(queueDir)) {
                Files.createDirectories(queueDir);
            }
            if (!Files.exists(crashDir)) {
                Files.createDirectories(crashDir);
            }
        } catch (IOException e) {
            System.err.println("无法创建目录：" + e.getMessage());
        }
    }

    // 记录普通种子信息
    public void logCommonSeed(Seed seed) {
        String fileName = generateFileName(seed);
        Path filePath = Paths.get(outputDirPath, "queue", fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            recordSeedInfo(seed, writer);
        } catch (IOException e) {
            System.err.println("记录普通种子信息时发生错误: " + e.getMessage());
        }
    }

    // 记录崩溃种子信息
    public void logCrashSeed(Seed seed) {
        String fileName = generateFileName(seed);
        Path filePath = Paths.get(outputDirPath, "crash", fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            recordSeedInfo(seed, writer);
        } catch (IOException e) {
            System.err.println("记录崩溃种子信息时发生错误: " + e.getMessage());
        }
    }

    private void recordSeedInfo(Seed seed, BufferedWriter writer) throws IOException {
        writer.write("Seed Path: " + seed.getFile().getAbsolutePath() + "\n");
        writer.write("Execution Time: " + seed.getExecuteTime() + " ms\n");
        writer.write("Coverage Increment: " + seed.getIncrementalCoverage() + "\n");
        writer.write("Energy: " + seed.getEnergy() + "\n");
        writer.write("Is Crash: " + seed.isCrash() + "\n");
        writer.write("Timestamp: " + getCurrentTimestamp() + "\n");
    }

    // 生成文件名，使用种子文件名加时间戳
    private String generateFileName(Seed seed) {
        String baseName = seed.getFile().getName();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        return baseName + "_" + timestamp + ".log";
    }

    // 获取当前时间戳
    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
