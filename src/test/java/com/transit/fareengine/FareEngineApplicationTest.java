package com.transit.fareengine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@code FareEngineApplication}.
 *
 * <p>These tests validate the command-line argument handling logic.
 */
public final class FareEngineApplicationTest {

    @Test
    void rejectsMissingCommandLineArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> FareEngineApplication.main(new String[0]));
    }

    @Test
    void rejectsExtraCommandLineArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> FareEngineApplication.main(
                        new String[]{"input.csv", "output.csv", "extra"}));
    }
}
