package io.github.mikeddavydov.jpwise.algo;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.github.mikeddavydov.jpwise.JPWise;
import io.github.mikeddavydov.jpwise.core.CombinationTable;
import io.github.mikeddavydov.jpwise.core.CyclingPartition;
import io.github.mikeddavydov.jpwise.core.SimpleValue;

public class PairwiseAlgorithmJumpTest {

    @DataProvider(name = "jumpValues")
    public Object[][] jumpValues() {
        return new Object[][] {
                { 2 },
                { 3 },
                { 4 },
                { 5 }
        };
    }

    @Test(dataProvider = "jumpValues")
    public void testPairwiseGenerationWithDifferentJumps(int jump) {
        // Create test data with 10 parameters
        CombinationTable result = JPWise.builder()
                // Browser parameter with 3 partitions and Safari-macOS rule
                .parameter("Browser",
                        Arrays.asList(
                                CyclingPartition.of("Chrome", Arrays.asList("latest", "previous")),
                                SimpleValue.of("Firefox"),
                                SimpleValue.of("Safari")),
                        Arrays.asList((ep1, ep2) -> {
                            if (ep1.getName().equals("Safari") && !ep2.getName().equals("macOS")) {
                                return false;
                            }
                            return true;
                        }))
                // OS parameter with 3 partitions
                .parameter("OS",
                        SimpleValue.of("Windows", "11"),
                        SimpleValue.of("Windows", "10"),
                        SimpleValue.of("macOS", "14.1"))
                // Device parameter with 2 partitions and Mobile-4K rule
                .parameter("Device",
                        Arrays.asList(
                                SimpleValue.of("Desktop"),
                                SimpleValue.of("Mobile")),
                        Arrays.asList((ep1, ep2) -> {
                            if (ep1.getName().equals("Mobile") && ep2.getName().equals("3840x2160")) {
                                return false;
                            }
                            return true;
                        }))
                // Screen resolution with 3 partitions and 10-bit color rule
                .parameter("Resolution",
                        Arrays.asList(
                                SimpleValue.of("1920x1080"),
                                SimpleValue.of("2560x1440"),
                                SimpleValue.of("3840x2160")),
                        Arrays.asList((ep1, ep2) -> {
                            if (ep1.getName().equals("10-bit") && !ep2.getName().equals("3840x2160")) {
                                return false;
                            }
                            return true;
                        }))
                // Color depth with 2 partitions
                .parameter("ColorDepth",
                        SimpleValue.of("8-bit"),
                        SimpleValue.of("10-bit"))
                // Network speed with 3 partitions
                .parameter("Network",
                        SimpleValue.of("4G"),
                        SimpleValue.of("5G"),
                        SimpleValue.of("WiFi"))
                // Language with 2 partitions
                .parameter("Language",
                        SimpleValue.of("English"),
                        SimpleValue.of("Spanish"))
                // Time zone with 2 partitions
                .parameter("Timezone",
                        SimpleValue.of("UTC"),
                        SimpleValue.of("EST"))
                // Theme with 2 partitions
                .parameter("Theme",
                        SimpleValue.of("Light"),
                        SimpleValue.of("Dark"))
                // Font size with 2 partitions
                .parameter("FontSize",
                        SimpleValue.of("Normal"),
                        SimpleValue.of("Large"))
                .generatePairwise(jump);

        // Verify results
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.combinations(), "Combinations should not be null");

        // Log the number of combinations for each jump value
        System.out.printf("Jump value %d generated %d combinations%n",
                jump, result.combinations().size());

        // Verify that all combinations are valid
        for (var combination : result.combinations()) {
            assertNotNull(combination, "Combination should not be null");
            assertEquals(combination.size(), 10,
                    "Each combination should have 10 parameters");

            // Verify that all values in the combination are set
            for (int i = 0; i < combination.size(); i++) {
                assertNotNull(combination.getValue(i),
                        "Parameter " + i + " should have a value");
            }
        }
    }
}