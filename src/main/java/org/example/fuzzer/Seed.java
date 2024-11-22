package org.example.fuzzer;

import java.io.File;

public class Seed {
    public String seedPath;
    private long coverage = 0;
    private long executeTime = 0;
    private boolean isCrash = false;
    private long energy = 1;
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

    public void updateCoverage(long coverage) {
        this.coverage = coverage;
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
