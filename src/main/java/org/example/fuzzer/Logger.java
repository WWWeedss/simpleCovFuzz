package org.example.fuzzer;

public class Logger {
    String outputDirPath = "/output_dir"; // 输出目录
    public Logger(String outputDirPath) {
        this.outputDirPath = outputDirPath;
    }

    public void logCommonSeed(Seed seed) {
        // 记录普通种子
    }

    public void logCrashSeed(Seed seed) {
        // 记录崩溃种子
    }
}
