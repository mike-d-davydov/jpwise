package io.github.mikeddavydov.jpwise;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.github.mikeddavydov.jpwise.core.CombinationTable;
import io.github.mikeddavydov.jpwise.core.CompatibilityPredicate;
import io.github.mikeddavydov.jpwise.core.CyclingPartition;
import io.github.mikeddavydov.jpwise.core.SimpleValue;

/**
 * Demonstrates how to use JPWise with TestNG's data provider feature. This test is intended to
 * mirror the JPWiseQuickDemoTest example from README.md.
 */
public class JpWiseReadMeDataProviderDemoTest {
  private static final CombinationTable DEMO_COMBINATIONS = generateJPWiseData();

  private static CombinationTable generateJPWiseData() {
    // Define a rule: "Safari" browser is only compatible with "macOS"
    // Rule is defined as per README, but not applied in the parameter definition below, to match
    // README
    List<CompatibilityPredicate> browserRules =
        Arrays.asList(
            (ep1,
                ep2) -> { // Inferred types for lambda, matching README's style if it were complete
              if (ep1.getName().equals("Safari") && !ep2.getName().equals("macOS")) {
                return false; // Safari is incompatible with non-macOS
              }
              return true; // Otherwise compatible
            });

    // Structure from README's JPWiseQuickDemoTest example
    return JPWise.builder()
        .parameter(
            "Browser",
            CyclingPartition.of("Chrome", Arrays.asList("latest", "previous")),
            SimpleValue.of("Safari")) // Rule not applied here, as per README example
        .parameter("OS", SimpleValue.of("macOS"), SimpleValue.of("Windows"))
        .generatePairwise();
  }

  @DataProvider(name = "jpwiseTestData")
  public Object[][] getTestDataFromJPWise() {
    return DEMO_COMBINATIONS.asDataProvider();
  }

  @Test(dataProvider = "jpwiseTestData")
  public void testFeatureWithVariedConfigs(String description, String browser, String os) {
    // description provides a summary (e.g., "Browser=Chrome(latest), OS=Windows")
    System.out.printf("Testing: %s%n", description); // Matches README example
    // Your test implementation here // Matches README example
  }
}
