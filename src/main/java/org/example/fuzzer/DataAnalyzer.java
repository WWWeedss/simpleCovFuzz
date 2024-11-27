package org.example.fuzzer;

import java.io.IOException;

public class DataAnalyzer {
    public static void runDataAnalysisScript() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("analyze_coverage.py");
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Data analysis script failed with exit code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}