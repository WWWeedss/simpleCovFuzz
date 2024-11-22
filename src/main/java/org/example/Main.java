package org.example;

import org.example.fuzzer.Executor;

public class Main {
    public static void main(String[] args) {
        String basicPath = "src/main/java/org/example/";
        Executor executor = new Executor(basicPath+"fuzz_targets/target1/cxxfilt",
                basicPath+"input/target1seed");
        try{
            executor.execute();
            executor.printInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}