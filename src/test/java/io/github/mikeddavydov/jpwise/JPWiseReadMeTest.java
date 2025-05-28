package io.github.mikeddavydov.jpwise;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.github.mikeddavydov.jpwise.core.CombinationTable;
import io.github.mikeddavydov.jpwise.core.SimpleValue;

public class JPWiseReadMeTest {
  private static final CombinationTable DEMO_COMBINATIONS = generateJPWiseData();

  private static CombinationTable generateJPWiseData() {
    // No rules for this simplified version
    // List<CompatibilityPredicate> browserRules = Arrays.asList(
    // (ep1, ep2) -> {
    // if (ep1.getName().equals("Safari") && !ep2.getName().equals("macOS")) {
    // return false; // Safari is incompatible with non-macOS
    // }
    // return true; // Otherwise compatible
    // });

    return JPWise.builder()
        .parameter(
            "Browser", SimpleValue.of("Chrome"), SimpleValue.of("Firefox")
            // No rules
            )
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
    assertNotNull(description, "Description should not be null");
    assertNotNull(browser, "Browser should not be null");
    assertNotNull(os, "OS should not be null");

    // Verify Safari-macOS rule
    if (browser.equals("Safari")) {
      assertEquals(os, "macOS", "Safari should only be paired with macOS");
    }
  }
}
