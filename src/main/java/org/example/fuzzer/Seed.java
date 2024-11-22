package org.example.fuzzer;

import java.io.File;

public class Seed {
    public String seedPath;
    private long coverage = 0;               // 当前运行的覆盖度
    private long incrementalCoverage = 0;     // 覆盖度增量（当前运行与上次运行的差值）
    private long executeTime = 0;
    private boolean isCrash = false;
    private long energy = 2;
    private boolean isInitialSeed = true;

    public Seed(String seedPath) {
        this.seedPath = seedPath;
    }

    public File getFile() {
        return new File(seedPath);
    }

    public long getCoverage() {
        return coverage;
    }

    // 更新当前覆盖度
    public void updateCoverage(long coverage) {
        // 上次运行的覆盖度
        long previousCoverage = this.coverage; // 保存上次覆盖度
        this.coverage = coverage;               // 更新当前覆盖度
        this.incrementalCoverage = this.coverage - previousCoverage; // 计算覆盖度增量
    }

    public long getIncrementalCoverage() {
        return incrementalCoverage;  // 返回覆盖度增量
    }

    public long getExecuteTime() {
        return executeTime;
    }

    public void updateExecutionTime(long executeTime) {
        this.executeTime = executeTime;
    }

    public boolean isCrash() {
        return isCrash;
    }

    public void setCrash(boolean isCrash) {
        this.isCrash = isCrash;
    }

    public long getEnergy() {
        return energy;
    }

    public void updateEnergy(long energy) {
        this.energy = energy;
    }

    public boolean isInitialSeed() {
        return isInitialSeed;
    }

    public void setInitialSeed(boolean isInitialSeed) {
        this.isInitialSeed = isInitialSeed;
    }
}
