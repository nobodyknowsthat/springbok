package com.anonymous.test.benchmark;

import org.junit.Test;

public class SyntheticDataGeneratorTest {

    @Test
    public void nextRandomTrajectoryPoint() {

        for (int i = 0; i < 100; i++) {
            System.out.println(SyntheticDataGenerator.nextRandomTrajectoryPoint());
        }

    }
}