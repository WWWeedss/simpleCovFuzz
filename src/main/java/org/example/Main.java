package org.example;

// import org.example.fuzzer.Executor;

// //import java.util.Scanner;

// public class Main {
//     public static void main(String[] args) {
//         String targetPath = "src/main/java/org/example/fuzz_targets/target2/readelf";
//         String seedDirPath = "src/main/java/org/example/input/target2seed";
//         String outputDirPath = "src/main/java/org/example/output_dir";
//         String[] targetArgs = null;  // 命令行参数
//         Boolean isInputFile = false; // 是否是输入文件
        
//         // 输入示例
//         // try (Scanner scanner = new Scanner(System.in)) {
//         //     System.out.println("Please enter the target path:");
//         //     targetPath = scanner.nextLine();
//         //     System.out.println("Please enter the seed directory path:");
//         //     seedDirPath = scanner.nextLine();
//         //     System.out.println("Do you need to provide extra arguments for the target program? (y/n)");
//         //     String response = scanner.nextLine();
//         //     if (response.equalsIgnoreCase("y")) {
//         //         System.out.println("Please enter the arguments (space separated):");
//         //         String argsInput = scanner.nextLine();
//         //         targetArgs = argsInput.split(" "); // 解析命令行参数
//         //     }
//         //     System.out.println("Are the seeds input files? (y/n)");
//         //     response = scanner.nextLine();
//         //     if (response.equalsIgnoreCase("y")) {
//         //         isInputFile = true;
//         //     }
//         // }

//         // 创建 Executor 实例，传入目标程序路径和参数
//         Executor executor = new Executor(targetPath, targetArgs, seedDirPath, isInputFile, outputDirPath);
//         try {
//             executor.execute();
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
// }
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.example.fuzzer.Executor;

public class Main {
    public static void main(String[] args) {
        String[] targetPaths = {
            "src/main/java/org/example/fuzz_targets/target1/cxxfilt",
            "src/main/java/org/example/fuzz_targets/target2/readelf",
            "src/main/java/org/example/fuzz_targets/target3/nm-new",
            "src/main/java/org/example/fuzz_targets/target4/objdump",
            "src/main/java/org/example/fuzz_targets/target5/djpeg",
            "src/main/java/org/example/fuzz_targets/target7/xmllint",
            "src/main/java/org/example/fuzz_targets/target8/lua",
            "src/main/java/org/example/fuzz_targets/target9/mjs",
            "src/main/java/org/example/fuzz_targets/target10/tcpdump"
        };

        String[] seedDirPaths = {
            "src/main/java/org/example/input/target1seed",
            "src/main/java/org/example/input/target2seed",
            "src/main/java/org/example/input/target3seed",
            "src/main/java/org/example/input/target4seed",
            "src/main/java/org/example/input/target5seed",
            "src/main/java/org/example/input/target7seed",
            "src/main/java/org/example/input/target8seed",
            "src/main/java/org/example/input/target9seed",
            "src/main/java/org/example/input/target10seed"
        };

        String outputDirPath = "src/main/java/org/example/output_dir";

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < targetPaths.length; i++) {
            String targetPath = targetPaths[i];
            String seedDirPath = seedDirPaths[i];
            int threadId = i; // 使用线程ID作为唯一标识符
            executorService.submit(() -> {
                String uniqueOutputDirPath = outputDirPath + "/thread_" + threadId;
                Executor executor = new Executor(targetPath, null, seedDirPath, false, uniqueOutputDirPath);
                try {
                    executor.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
    }
}