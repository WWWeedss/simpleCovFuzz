package org.example.fuzzer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CoverageEvaluator {
    private final List<Integer> coverageData = new ArrayList<>();
    private final String outputFilePath;

    public CoverageEvaluator(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public void recordCoverage(Set<String> coveragePaths) {
        coverageData.add(coveragePaths.size());
    }

    public void generateCoverageCurve() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write("Execution, Coverage\n");
            for (int i = 0; i < coverageData.size(); i++) {
                writer.write((i + 1) + ", " + coverageData.get(i) + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing coverage curve: " + e.getMessage());
        }
    }
}