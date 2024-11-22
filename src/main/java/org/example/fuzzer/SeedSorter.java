package org.example.fuzzer;

import java.util.*;

public class SeedSorter {

    /**
     * 按覆盖率对种子进行排序
     *
     * @param seeds 种子队列
     * @return 排序后的种子队列
     */
    public Queue<Seed> sortByCoverage(Queue<Seed> seeds) {
        // 将 Queue 转换为 List
        List<Seed> seedList = new ArrayList<>(seeds);

        // 按照覆盖率降序排序
        seedList.sort(Comparator.comparingLong(Seed::getCoverage).reversed());

        // 将排序后的 List 转换回 Queue
        return new LinkedList<>(seedList);
    }

    /**
     * 按执行时间对种子进行排序
     *
     * @param seeds 种子队列
     * @return 排序后的种子队列
     */
    public Queue<Seed> sortByExecutionTime(Queue<Seed> seeds) {
        // 将 Queue 转换为 List
        List<Seed> seedList = new ArrayList<>(seeds);

        // 按照执行时间升序排序
        seedList.sort(Comparator.comparingLong(Seed::getExecuteTime));

        // 将排序后的 List 转换回 Queue
        return new LinkedList<>(seedList);
    }
}
