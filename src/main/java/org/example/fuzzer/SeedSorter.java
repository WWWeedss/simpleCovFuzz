package org.example.fuzzer;

import java.util.*;

public class SeedSorter {

    /**
     * 按覆盖率对种子进行排序
     *
     * @param seeds 种子队列
     * @return 排序后的种子队列
     */
    public void sortByCoverage(List<Seed> seeds) {
        seeds.sort(Comparator.comparingLong(Seed::getCoverage).reversed());
    }

    /**
     * 按执行时间对种子进行排序
     *
     * @param seeds 种子队列
     * @return 排序后的种子队列
     */
    public void sortByExecutionTime(List<Seed> seeds) {
        seeds.sort(Comparator.comparingLong(Seed::getExecuteTime));
    }
    /**
     * 按启发式评分对种子进行排序
     *
     * @param seeds 种子队列
     * @return 排序后的种子队列
     */
    //当前未使用
    public void sortByHeuristic(List<Seed> seeds) {
        seeds.sort((s1, s2) -> {
            long score1 = computeHeuristicScore(s1);
            long score2 = computeHeuristicScore(s2);
            return Long.compare(score2, score1); // 从大到小排序
        });
    }

    private long computeHeuristicScore(Seed seed) {
        // 启发式评分：结合覆盖度增量和执行时间等因素
        long coverageWeight = 10;
        long executionTimeWeight = 5;

        return coverageWeight * seed.getIncrementalCoverage() - executionTimeWeight * seed.getExecuteTime();
    }

}
