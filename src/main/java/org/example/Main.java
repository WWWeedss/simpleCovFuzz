package org.example;

import org.example.fuzzer.Executor;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String targetPath = null;
        String seedDirPath = null;
        String outputDirPath = "src/main/java/org/example/output_dir";

        // 输入示例
        // src/main/java/org/example/fuzz_targets/target1/cxxfilt
        // src/main/java/org/example/input/target1seed
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Please enter the target path:");
            targetPath = scanner.nextLine();
            System.out.println("Please enter the seed directory path:");
            seedDirPath = scanner.nextLine();
        }

        Executor executor = new Executor(targetPath, seedDirPath, outputDirPath);
        try {
            executor.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}