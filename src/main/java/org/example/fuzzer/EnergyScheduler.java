package org.example.fuzzer;

import java.util.Queue;

public class EnergyScheduler {

    /**
     * 根据种子的覆盖度增量调整能量
     * @param seedQueue 种子队列
     */
    public static void energySchedule(Queue<Seed> seedQueue) {
        for (Seed seed : seedQueue) {
            long incrementalCoverage = seed.getIncrementalCoverage();

            // 能量调度的基本逻辑
            int newEnergy = calculateEnergy(seed);

            // 更新种子的能量
            seed.updateEnergy(newEnergy);
        }
    }

    /**
     * 计算能量，根据种子的覆盖度增量来确定
     * @param  seed 种子
     * @return 计算后的能量值
     */
    private static int calculateEnergy(Seed seed) {
        // 获取原有的能量加权
        int currentEnergy = (int) ((int) seed.getEnergy()*0.9);

        // 计算基于覆盖度增量的调整值
        int energyIncrement;

        int incrementalCoverage = (int) seed.getIncrementalCoverage();

        if (incrementalCoverage > 0) {
            // 假设每 10 单位的覆盖度增量增加 1 能量
            energyIncrement = (int)(incrementalCoverage / 10);
        } else {
            // 如果覆盖度增量为 0 或负数，能量不增加
            energyIncrement = 0;
        }

        // 新的能量为当前能量加上增量
        int newEnergy = currentEnergy + energyIncrement;

        // 能量最大为 10
        if (newEnergy > 10) {
            newEnergy = 10;
        }

        // 能量最小为 1，保证种子始终有至少一点能量
        if (newEnergy < 1) {
            newEnergy = 1;
        }

        return newEnergy;
    }

    /**
     * 使用启发式策略调整能量
     * @param seedQueue 种子队列
     */
    //当前未使用
    public static void energyScheduleWithHeuristic(Queue<Seed> seedQueue) {
        for (Seed seed : seedQueue) {
            long incrementalCoverage = seed.getIncrementalCoverage();

            // 使用启发式策略计算能量
            int newEnergy = calculateEnergyWithHeuristic(seed, incrementalCoverage);

            // 更新种子的能量
            seed.updateEnergy(newEnergy);
        }
    }

    private static int calculateEnergyWithHeuristic(Seed seed, long incrementalCoverage) {
        int currentEnergy = (int) seed.getEnergy();
        int newEnergy = currentEnergy;

        // 启发式规则：高覆盖度增量奖励高能量
        if (incrementalCoverage > 100) {
            newEnergy += 2;  // 如果覆盖度增量大，增加能量
        } else if (incrementalCoverage < 10) {
            newEnergy -= 1;  // 覆盖度增量小的种子减少能量
        }

        // 如果种子执行时间过长，减少能量
        if (seed.getExecuteTime() > 5000) {  // 假设执行时间超过5秒的种子减少能量
            newEnergy -= 1;
        }

        // 规范化能量值
        return Math.max(1, Math.min(newEnergy, 10));  // 能量值限制在1到10之间
    }
}
