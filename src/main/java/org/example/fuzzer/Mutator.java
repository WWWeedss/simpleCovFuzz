package org.example.fuzzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Mutator {
    private final String outputDir;
    //供splice变异算子使用
    private List<Seed> existingSeeds = new ArrayList<>();
    private final Integer maxSizeOfExistingSeeds = 100;
    private long seedCount = 0;

    public Mutator(String outputDir) {
        this.outputDir = outputDir;
        createDirectories();
    }

    public Mutator(){
        this.outputDir = "src/main/java/org/example/output_dir";
        createDirectories();
    }

    private void createDirectories() {
        Path mutateDir = Paths.get(outputDir, "mutate-seed");
        try {
            Path outputPath = Paths.get(outputDir).toAbsolutePath();
            Files.createDirectories(outputPath);
            if (!Files.exists(mutateDir)) {
                Files.createDirectories(mutateDir);
            }
        } catch (IOException e) {
            System.err.println("无法创建目录：" + e.getMessage());
        }
    }

    public void addExistingSeeds(List<Seed> newExistingSeeds) {
        // 合并现有种子列表和新种子列表
        List<Seed> combinedSeeds = new ArrayList<>(existingSeeds);
        combinedSeeds.addAll(newExistingSeeds);

        // 如果合并后的总数超过最大限制，随机选取100个种子
        if (combinedSeeds.size() > maxSizeOfExistingSeeds) {
            // 随机打乱合并后的种子列表
            Collections.shuffle(combinedSeeds);
            // 选择前100个种子
            combinedSeeds = combinedSeeds.subList(0, Math.min(100, combinedSeeds.size()));
        }

        // 更新现有种子列表为选择的种子
        existingSeeds = combinedSeeds;
    }

    public List<Seed> mutate(Seed originalSeed) throws IOException {
        List<Seed> mutatedSeeds = new LinkedList<>();
        Random random = new Random();

        for (int i = 0; i < originalSeed.getEnergy(); i++) {
            byte[] seedData = Files.readAllBytes(Paths.get(originalSeed.getSeedPath()));
            int mutationType = random.nextInt(5);  // 随机选择一种变异算子

            switch (mutationType) {
                case 0 -> bitflipMutation(seedData);
                case 1 -> arithMutation(seedData);
                case 2 -> interestMutation(seedData);
                case 3 -> havocMutation(seedData);
                case 4 -> spliceMutation(seedData); // 注意 splice 可能用到多个种子
            }

            // 将变异后的种子存储为文件
            String mutatedPath = saveMutatedSeed(seedData, seedCount++);
            mutatedSeeds.add(new Seed(mutatedPath));
        }
        return mutatedSeeds;
    }

    private String saveMutatedSeed(byte[] seedData, long index) throws IOException {
        String mutatedPath = outputDir + "/mutate-seed/mutated_" + index + ".bin";
        Files.write(Paths.get(mutatedPath), seedData);
        return mutatedPath;
    }

    private static void bitflipMutation(byte[] seedData) {
        Random random = new Random();
        int byteIndex = random.nextInt(seedData.length);
        int bitIndex = random.nextInt(8);

        // 翻转指定字节的某个位
        seedData[byteIndex] ^= (byte) (1 << bitIndex);
    }

    private static void arithMutation(byte[] seedData) {
        Random random = new Random();
        int byteIndex = random.nextInt(seedData.length);
        int operation = random.nextInt(2); // 0 = 加法, 1 = 减法
        int value = random.nextInt(128);  // 随机加/减幅度

        if (operation == 0) {
            seedData[byteIndex] += (byte) value;
        } else {
            seedData[byteIndex] -= (byte) value;
        }
    }

    private static void interestMutation(byte[] seedData) {
        Random random = new Random();
        int byteIndex = random.nextInt(seedData.length);

        // 随机选择一个感兴趣值
        int interestValue = INTEREST_VALUES[random.nextInt(INTEREST_VALUES.length)];

        // 替换指定位置
        seedData[byteIndex] = (byte) interestValue;
    }


    private static final int[] INTEREST_VALUES = {
            0, 1, -1, 255, 256, 0x7FFFFFFF, 0x80000000, 0xFFFFFFFF
    };

    private static void havocMutation(byte[] seedData) {
        Random random = new Random();
        int mutations = random.nextInt(16) + 1; // 随机选择1到16次变异操作

        for (int i = 0; i < mutations; i++) {
            int mutationType = random.nextInt(3); // 0 = bitflip, 1 = arith, 2 = interest

            switch (mutationType) {
                case 0 -> bitflipMutation(seedData);
                case 1 -> arithMutation(seedData);
                case 2 -> interestMutation(seedData);
            }
        }
    }


    private void spliceMutation(byte[] seedData) {
        if (existingSeeds.isEmpty()) return; // 如果没有其他种子，直接返回

        Random random = new Random();
        Seed randomSeed = existingSeeds.get(random.nextInt(existingSeeds.size()));

        try {
            byte[] otherSeedData = Files.readAllBytes(Paths.get(randomSeed.getSeedPath()));

            // 随机选择拼接点
            int splicePoint = random.nextInt(Math.min(seedData.length, otherSeedData.length));

            // 从 splicePoint 开始，用其他种子的内容覆盖当前种子的部分数据
            System.arraycopy(otherSeedData, splicePoint, seedData, splicePoint,
                    Math.min(seedData.length - splicePoint, otherSeedData.length - splicePoint));
        } catch (IOException e) {
            System.err.println("读取种子文件时发生错误: " + e.getMessage());
        }
    }
}
