package org.example;

import org.example.fuzzer.Executor;

public class Main {
    public static void main(String[] args) {
        String targetPath = "src/main/java/org/example/fuzz_targets/target1/cxxfilt";
        String seedDirPath = "src/main/java/org/example/input/target1seed";

        Executor executor = new Executor(targetPath, seedDirPath);
        try {
            executor.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}