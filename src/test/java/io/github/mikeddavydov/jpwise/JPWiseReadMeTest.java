package io.github.mikeddavydov.jpwise;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import io.github.mikeddavydov.jpwise.core.Combination;
import io.github.mikeddavydov.jpwise.core.CombinationTable;
import io.github.mikeddavydov.jpwise.core.EquivalencePartition;
import io.github.mikeddavydov.jpwise.core.SimpleValue;

// Class name kept as JPWiseReadMeTest
public class JPWiseReadMeTest {

  @Test
  public void testBasicUsageExample() {
    // Generate combinations - Code from README Basic Usage section
    CombinationTable results =
        JPWise.builder()
            .parameter(
                "browser", // Name from README
                SimpleValue.of("Chrome"), // Value from README
                SimpleValue.of("Firefox") // Value from README
                )
            .parameter(
                "os", // Name from README
                SimpleValue.of("Windows", "11"), // Values from README
                SimpleValue.of("macOS", "14.1") // Values from README
                )
            .generatePairwise();

    // Alternatively, to generate a limited set of all combinations (combinatorial):
    // CombinationTable combinatorialResults = JPWise.builder()...generateCombinatorial(4);

    // Basic assertions to ensure the example works in a test context
    assertNotNull(results, "Results should not be null");
    assertTrue(results.size() > 0, "Should generate some combinations");

    // Use the results - Code from README Basic Usage section
    for (Combination combination : results.combinations()) {
      EquivalencePartition browserEP = combination.getValues()[0];
      EquivalencePartition osEP = combination.getValues()[1];
      System.out.printf(
          "Browser: %s, OS: %s%n", // Matches README
          browserEP.getValue(), osEP.getValue());
    }
  }
}
