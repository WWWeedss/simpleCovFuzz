package org.example.fuzzer;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class Executor {
    private final String targetPath;
    private final Queue<Seed> seedQueue; // 种子队列
    private long executionCount = 0; // 执行次数
    private long totalExecutionTime = 0; // 总执行时间（纳秒）
    private long crashCount = 0; // 崩溃次数
    private final Set<String> coveragePaths = new HashSet<>(); // 记录所有覆盖路径
    private final SeedSorter seedSorter = new SeedSorter(); // 种子排序器
    private Logger logger = new Logger(); // 日志记录器
    private Mutator mutator = new Mutator(); // 变异器

    public Executor(String targetPath, String seedDirPath) {
        this.targetPath = targetPath;
        this.seedQueue = new LinkedList<>();
        initialSeedQueue(seedDirPath);
    }

    public Executor(String targetPath, String seedDirPath, String outputDirPath) {
        this.targetPath = targetPath;
        this.seedQueue = new LinkedList<>();
        this.logger = new Logger(outputDirPath);
        this.mutator = new Mutator(outputDirPath);

        initialSeedQueue(seedDirPath);
    }

    private void initialSeedQueue(String seedDirPath) {
        File seedDir = new File(seedDirPath);
        File[] files = seedDir.listFiles();
        if (files != null) {
            for (File file : files) {
                Seed seed = new Seed(file.getAbsolutePath());
                seedQueue.add(seed);
            }
        }
    }
    /**
     * 执行所有种子，并统计执行结果。
     */
    public void execute() throws IOException {
        List<Seed> allProcessedSeeds = new LinkedList<>();

        while (!seedQueue.isEmpty()) {
            Queue<Seed> nexSeedQueue = new LinkedList<>();
            List<Seed> curProcessedSeeds = new LinkedList<>();

            while(!seedQueue.isEmpty()) {
                Seed seed = seedQueue.poll();
                if (seed == null) continue;

                // 执行种子并统计信息
                try {
                    executeSeed(seed);
                } catch (IOException | InterruptedException e) {
                    System.err.println("执行种子时发生错误: " + e.getMessage());
                    seed.setCrash(true); // 标记为崩溃种子

                    // 记录崩溃种子
                    logger.logCrashSeed(seed);

                } finally {
                    curProcessedSeeds.add(seed);

                    // 记录普通种子
                    logger.logCommonSeed(seed);
                }
            }

            //对已执行过的种子进行排序,确立变异优先级
            seedSorter.sortByCoverage(curProcessedSeeds);

            //对已执行过的种子进行能量调度，确立变异子代数
            EnergyScheduler.energySchedule(curProcessedSeeds);

            //将已执行过的种子加入到总的已执行种子中
            allProcessedSeeds.addAll(curProcessedSeeds);
            mutator.addExistingSeeds(curProcessedSeeds);

            //进行变异操作
            for(Seed processedSeed : allProcessedSeeds){
                if(processedSeed.getEnergy() > 0){
                    List<Seed> mutatedSeeds = mutator.mutate(processedSeed);
                    nexSeedQueue.addAll(mutatedSeeds);
                    processedSeed.updateEnergy(processedSeed.getEnergy() - 1);
                }
            }

            //清理能量为0的seed
            allProcessedSeeds.removeIf(seed -> seed.getEnergy() == 0);

            // 定期清理那些覆盖度增量为零或负数的种子
            allProcessedSeeds.removeIf(seed -> seed.getIncrementalCoverage() <= 0);

            seedQueue.addAll(nexSeedQueue);

            //可以在执行时打印信息
            printInfo();
        }
    }

    /**
     * 执行单个种子并更新统计信息。
     *
     * @param seed 要执行的种子
     */
    private void executeSeed(Seed seed) throws IOException, InterruptedException {
        long startTime = System.nanoTime();

        // 创建一个 ProcessBuilder 来启动目标程序
        ProcessBuilder processBuilder = new ProcessBuilder("afl-showmap", "-o", "-", "--", targetPath);
        Process process = processBuilder.start();

        // 将种子数据传递给目标程序的标准输入
        try (OutputStream os = process.getOutputStream();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
            writer.write(new String(Files.readAllBytes(seed.getFile().toPath())));
            writer.flush();
        }

        // 读取目标程序的输出并解析覆盖路径
        long incrementCoverage = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 使用正则表达式验证行格式
                if (line.matches("\\d{6}:\\d+")) {
                    // 提取路径标识并存储到 coveragePaths 中
                    if(!coveragePaths.contains(line.split(":")[0])){
                        incrementCoverage++;
                    }
                    coveragePaths.add(line.split(":")[0]);
                }
            }
        }

        int exitCode = process.waitFor();
        long endTime = System.nanoTime();

        // 更新种子信息
        seed.updateExecutionTime((endTime - startTime) / 1_000_000); // 转换为毫秒
        seed.updateCoverage(coveragePaths.size()); // 更新当前覆盖度
        seed.updateIncrementalCoverage(incrementCoverage);
        seed.setCrash(exitCode != 0); // 非零退出码表示崩溃

        // 更新总体统计
        executionCount++;
        if (!seed.isCrash()) {
            totalExecutionTime += seed.getExecuteTime();
        } else {
            logger.logCrashSeed(seed);
            crashCount++;
        }
    }

    /**
     * 打印统计信息
     */
    public void printInfo() {
        System.out.println("执行次数：" + executionCount);
        System.out.println("总执行时间：" + totalExecutionTime + " 毫秒");
        System.out.println("崩溃次数：" + crashCount);
        System.out.println("覆盖路径总数：" + coveragePaths.size());
    }
}
