package org.example.fuzzer;

import java.util.Random;

public class Mutator {
    public static void mutate(byte[] seedData) {
        Random random = new Random();
        int mutationType = random.nextInt(5);  // 随机选择一种变异算子

        switch (mutationType) {
            case 0:
                bitflipMutation(seedData);
                break;
            case 1:
                arithMutation(seedData);
                break;
            case 2:
                interestMutation(seedData);
                break;
            case 3:
                havocMutation(seedData);
                break;
            case 4:
                spliceMutation(seedData);
                break;
        }
    }

    private static void bitflipMutation(byte[] seedData) {

    }
    private static void arithMutation(byte[] seedData) {

    }
    private static void interestMutation(byte[] seedData) {

    }
    private static void havocMutation(byte[] seedData) {

    }
    private static void spliceMutation(byte[] seedData) {
    }
}
