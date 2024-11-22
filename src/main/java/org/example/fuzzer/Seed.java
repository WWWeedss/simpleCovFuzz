package org.example.fuzzer;

import java.io.File;

public class Seed {
    public String seedPath;
    private long coverage = 0;               // 当前运行的覆盖度
    private long incrementalCoverage = 0;     // 覆盖度增量（当前运行与上次运行的差值）
    private long executeTime = 0;
    private boolean isCrash = false;
    private long energy = 2;

    public Seed(String seedPath) {
        this.seedPath = seedPath;
    }

    public String getSeedPath() {
        return seedPath;
    }

    public File getFile() {
        return new File(seedPath);
    }

    public long getCoverage() {
        return coverage;
    }

    public void updateCoverage(long coverage) {
        this.coverage = coverage;
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

    public void updateIncrementalCoverage(long incrementCoverage) {
        this.incrementalCoverage = incrementCoverage;
    }
}
