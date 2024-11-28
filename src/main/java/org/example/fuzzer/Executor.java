package org.example.fuzzer;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.concurrent.*;

public class Executor {
    private final String targetPath;
    private final String[] targetArgs; // 目标程序的命令行参数
    private final Boolean isInputFile; // 是否是输入文件
    private final Queue<Seed> seedQueue; // 种子队列
    private long executionCount = 0; // 执行次数
    private long totalExecutionTime = 0; // 总执行时间（纳秒）
    private long crashCount = 0; // 崩溃次数
    private final Set<String> coveragePaths = new HashSet<>(); // 记录所有覆盖路径
    private final SeedSorter seedSorter = new SeedSorter(); // 种子排序器
    private Logger logger = new Logger(); // 日志记录器
    private Mutator mutator = new Mutator(); // 变异器
    private final CoverageEvaluator coverageEvaluator; // 覆盖率评估器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // 定时器
    private final ScheduledExecutorService infoScheduler = Executors.newScheduledThreadPool(1); // 信息打印定时器
    private final ScheduledExecutorService analysisScheduler = Executors.newScheduledThreadPool(1); // 数据分析定时器
    private long startTime; // 开始时间

    public Executor(String targetPath, String targetArgs[], String seedDirPath, Boolean isInputFile, String outputDirPath) {
        this.targetPath = targetPath;
        this.targetArgs = targetArgs != null ? targetArgs : new String[]{}; // 如果没有命令行参数，则使用空数组
        this.isInputFile = isInputFile;
        this.seedQueue = new LinkedList<>();
        this.logger = new Logger(outputDirPath);
        this.mutator = new Mutator(outputDirPath);
        this.coverageEvaluator = new CoverageEvaluator(outputDirPath + "/coverage_curve.csv");
        initialSeedQueue(seedDirPath);
        startCoverageCurveScheduler();
        startInfoScheduler();
        startAnalysisScheduler();
        this.startTime = System.nanoTime();
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

    private void startCoverageCurveScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            coverageEvaluator.generateCoverageCurve();
            System.out.println("Coverage curve generated.");
        }, 10, 10, TimeUnit.SECONDS);
    }

    private void startInfoScheduler() {
        infoScheduler.scheduleAtFixedRate(() -> {
            printInfo();
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void startAnalysisScheduler() {
        analysisScheduler.scheduleAtFixedRate(() -> {
            DataAnalyzer.runDataAnalysisScript();
        }, 20, 20, TimeUnit.SECONDS);
    }

    public void execute() throws IOException {
        List<Seed> allProcessedSeeds = new LinkedList<>();
        while (!seedQueue.isEmpty()) {
            Queue<Seed> nexSeedQueue = new LinkedList<>();
            List<Seed> curProcessedSeeds = new LinkedList<>();
            while (!seedQueue.isEmpty()) {
                Seed seed = seedQueue.poll();
                if (seed == null) continue;
                // 执行种子并统计信息
                try {
                    executeSeed(seed);
                } catch (IOException | InterruptedException e) {
                    System.err.println("执行种子时发生错误: " + e.getMessage());
                    seed.setCrash(true); // 标记为崩溃种子
                    // 记录崩溃种子
                    crashCount++;
                    logger.logCrashSeed(seed);
                } finally {
                    curProcessedSeeds.add(seed);
                    // 记录普通种子
                    logger.logCommonSeed(seed);
                    // 记录覆盖率
                    coverageEvaluator.recordCoverage(coveragePaths);
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
            for (Seed processedSeed : allProcessedSeeds) {
                if (processedSeed.getEnergy() > 0) {
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
        }
        // 生成最终的覆盖率曲线
        coverageEvaluator.generateCoverageCurve();
        // 关闭定时器
        scheduler.shutdown();
        infoScheduler.shutdown();
        analysisScheduler.shutdown();
        // 调用数据分析脚本
        DataAnalyzer.runDataAnalysisScript();
    }

    private void executeSeed(Seed seed) throws IOException, InterruptedException {
        long startTime = System.nanoTime();

        // 创建命令行参数
        List<String> command = new ArrayList<>();
        command.add("afl-showmap");  // 固定命令 part
        command.add("-o");           // 输出重定向参数
        command.add("-");            // 输出到标准输出（由后续代码处理）
        command.add("--");           // 表示命令行参数分隔符
        command.add(targetPath);     // 添加目标程序路径

        // 如果有额外的命令行参数，添加到命令中
        if (targetArgs != null) {
            command.addAll(Arrays.asList(targetArgs));
        }

        if (!isInputFile) {
            command.add(seed.seedPath);  // 添加种子文件路径
        }
        // 创建一个 ProcessBuilder 来启动目标程序
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        if (isInputFile) {
            // 将种子数据传递给目标程序的标准输入
            try (OutputStream os = process.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
                writer.write(new String(Files.readAllBytes(seed.getFile().toPath())));
                writer.flush();
            }
        }

        // 读取目标程序的输出并解析覆盖路径
        long incrementCoverage = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 使用正则表达式验证行格式
                if (line.matches("\\d{6}:\\d+")) {
                    // 提取路径标识并存储到 coveragePaths 中
                    String pathId = line.split(":")[0];
                    if (!coveragePaths.contains(pathId)) {
                        incrementCoverage++;
                    }
                    coveragePaths.add(pathId);
                }
            }
        }

        int exitCode = process.waitFor();  // 等待进程执行完毕
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
        }
    }


    private void printInfo() {
        long currentTime = System.nanoTime();
        double elapsedTimeInSeconds = (currentTime - startTime) / 1_000_000_000.0;
        double speed = executionCount / elapsedTimeInSeconds;
        System.out.println("Execution Count: " + executionCount);
        System.out.println("Total Execution Time: " + totalExecutionTime + " ns");
        System.out.println("Crash Count: " + crashCount);
        System.out.println("Coverage Paths: " + coveragePaths.size());
        System.out.println("Current Test Speed: " + String.format("%.2f", speed) + " executions per second");
    }
}