package org.example;

import org.example.fuzzer.Executor;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String targetPath = "src/main/java/org/example/fuzz_targets/target2/readelf";
        String seedDirPath = "src/main/java/org/example/input/target2seed";
        String outputDirPath = "src/main/java/org/example/output_dir";
        String[] targetArgs = null;  // 命令行参数
        Boolean isInputFile = false; // 是否是输入文件

        // 输入示例
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Please enter the target path:");
            targetPath = scanner.nextLine();
            System.out.println("Please enter the seed directory path:");
            seedDirPath = scanner.nextLine();
            System.out.println("Do you need to provide extra arguments for the target program? (y/n)");
            String response = scanner.nextLine();
            if (response.equalsIgnoreCase("y")) {
                System.out.println("Please enter the arguments (space separated):");
                String argsInput = scanner.nextLine();
                targetArgs = argsInput.split(" "); // 解析命令行参数
            }
            System.out.println("Are the seeds input files? (y/n)");
            response = scanner.nextLine();
            if (response.equalsIgnoreCase("y")) {
                isInputFile = true;
            }
        }

        // 创建 Executor 实例，传入目标程序路径和参数
        Executor executor = new Executor(targetPath, targetArgs, seedDirPath, isInputFile, outputDirPath);
        try {
            executor.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
