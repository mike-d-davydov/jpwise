package io.github.mikeddavydov.jpwise.algo;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.github.mikeddavydov.jpwise.JPWise;
import io.github.mikeddavydov.jpwise.core.Combination;
import io.github.mikeddavydov.jpwise.core.CombinationTable;
import io.github.mikeddavydov.jpwise.core.CyclingPartition;
import io.github.mikeddavydov.jpwise.core.SimpleValue;

public class CombinatorialAlgorithmLimitTest {

  @DataProvider(name = "combinationLimits")
  public Object[][] combinationLimits() {
    return new Object[][] {
      {1000}, // Test with 1000 combinations
      {10000} // Test with 10000 combinations
    };
  }

  @Test(dataProvider = "combinationLimits")
  public void testCombinatorialGenerationWithDifferentLimits(int limit) {
    long startTime = System.currentTimeMillis();

    // Create test data with 10 parameters
    CombinationTable result =
        JPWise.builder()
            // Browser parameter with 3 partitions and Safari-macOS rule
            .parameter(
                "Browser",
                Arrays.asList(
                    CyclingPartition.of("Chrome", Arrays.asList("latest", "previous")),
                    SimpleValue.of("Firefox"),
                    SimpleValue.of("Safari")),
                Arrays.asList(
                    (ep1, ep2) -> {
                      if (ep1.getName().equals("Safari")
                          && ep2.getParentParameter().getName().equals("OS")) {
                        return ep2.getName().equals("macOS");
                      }
                      return true;
                    }))
            // OS parameter with 3 partitions and Safari-macOS rule
            .parameter(
                "OS",
                Arrays.asList(
                    SimpleValue.of("Windows", "11"),
                    SimpleValue.of("Windows", "10"),
                    SimpleValue.of("macOS", "14.1")),
                Arrays.asList(
                    (ep1, ep2) -> {
                      // If other is Browser=Safari, this OS (ep1) must be macOS.
                      if (ep2.getParentParameter().getName().equals("Browser")
                          && ep2.getName().equals("Safari")) {
                        return ep1.getName().equals("macOS");
                      }
                      return true;
                    }))
            // Device parameter with 2 partitions and Mobile-4K rule
            .parameter(
                "Device",
                Arrays.asList(SimpleValue.of("Desktop"), SimpleValue.of("Mobile")),
                Arrays.asList(
                    (ep1, ep2) -> {
                      if (ep1.getName().equals("Mobile") && ep2.getName().equals("Resolution")) {
                        return !ep2.getValue().equals("3840x2160");
                      }
                      return true;
                    }))
            // Screen resolution with 3 partitions and Mobile-4K rule
            .parameter(
                "Resolution",
                Arrays.asList(
                    SimpleValue.of("1920x1080"),
                    SimpleValue.of("2560x1440"),
                    SimpleValue.of("3840x2160")),
                Arrays.asList(
                    (ep1, ep2) -> {
                      // Mobile-4K rule
                      if (ep1.getParentParameter().getName().equals("Resolution")
                          && ep2.getParentParameter().getName().equals("Device")
                          && ep2.getName().equals("Mobile")) {
                        return !ep1.getValue().equals("3840x2160");
                      }
                      // 10-bit with 4K rule (Resolution's perspective)
                      if (ep1.getParentParameter().getName().equals("Resolution")
                          && ep2.getParentParameter().getName().equals("ColorDepth")
                          && ep2.getName().equals("10-bit")) {
                        return ep1.getValue().equals("3840x2160");
                      }
                      return true;
                    }))
            // Color depth with 2 partitions and 10-bit color rule
            .parameter(
                "ColorDepth",
                Arrays.asList(SimpleValue.of("8-bit"), SimpleValue.of("10-bit")),
                Arrays.asList(
                    (ep1, ep2) -> {
                      // 10-bit with 4K rule (ColorDepth's perspective)
                      if (ep1.getParentParameter().getName().equals("ColorDepth")
                          && ep1.getName().equals("10-bit")
                          && ep2.getParentParameter().getName().equals("Resolution")) {
                        return ep2.getValue().equals("3840x2160");
                      }
                      return true;
                    }))
            // Network speed with 3 partitions
            .parameter(
                "Network", SimpleValue.of("4G"), SimpleValue.of("5G"), SimpleValue.of("WiFi"))
            // Language with 2 partitions
            .parameter("Language", SimpleValue.of("English"), SimpleValue.of("Spanish"))
            // Time zone with 2 partitions
            .parameter("Timezone", SimpleValue.of("UTC"), SimpleValue.of("EST"))
            // Theme with 2 partitions
            .parameter("Theme", SimpleValue.of("Light"), SimpleValue.of("Dark"))
            // Font size with 2 partitions
            .parameter("FontSize", SimpleValue.of("Normal"), SimpleValue.of("Large"))
            .generateCombinatorial(limit);

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // Verify results
    assertNotNull(result, "Result should not be null");
    assertNotNull(result.combinations(), "Combinations should not be null");

    // Log the number of combinations and timing for each limit
    System.out.printf(
        "Combination limit %d generated %d combinations in %d ms%n",
        limit, result.combinations().size(), duration);

    // Verify that all combinations are valid
    for (var combination : result.combinations()) {
      assertNotNull(combination, "Combination should not be null");
      assertEquals(combination.size(), 10, "Each combination should have 10 parameters");

      // Verify that all values in the combination are set
      for (int i = 0; i < combination.size(); i++) {
        assertNotNull(combination.getValue(i), "Parameter " + i + " should have a value");
      }

      // Verify compatibility rules
      verifyCompatibilityRules(combination);
    }
  }

  private void verifyCompatibilityRules(Combination combination) {
    // Get parameter values
    String browser = combination.getValue(0).getName();
    String os = combination.getValue(1).getName();
    String device = combination.getValue(2).getName();
    String resolution = combination.getValue(3).getName();
    String colorDepth = combination.getValue(4).getName();

    // Verify Safari-macOS rule
    if ("Safari".equals(browser)) {
      assertEquals(os, "macOS", "Safari should only be paired with macOS");
    }

    // Verify Mobile-4K rule
    if ("Mobile".equals(device)) {
      assert !"3840x2160".equals(resolution) : "Mobile should not be paired with 4K resolution";
    }

    // Verify 10-bit color depth rule
    if ("10-bit".equals(colorDepth)) {
      assertEquals(
          resolution, "3840x2160", "10-bit color depth should only be paired with 4K resolution");
    }
  }
}
