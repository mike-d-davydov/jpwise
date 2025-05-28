package io.github.mikeddavydov.jpwise;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.github.mikeddavydov.jpwise.core.CombinationTable;
import io.github.mikeddavydov.jpwise.core.CompatibilityPredicate;
import io.github.mikeddavydov.jpwise.core.CyclingPartition;
import io.github.mikeddavydov.jpwise.core.EquivalencePartition;
import io.github.mikeddavydov.jpwise.core.SimpleValue;
import io.github.mikeddavydov.jpwise.core.TestParameter;

/**
 * Demonstrates how to use JPWise with TestNG's data provider feature. This test is intended to
 * mirror the JPWiseQuickDemoTest example from README.md.
 */
public class JPWiseReadMeDataProviderDemoTest {
  private static final Logger log = LoggerFactory.getLogger(JPWiseReadMeDataProviderDemoTest.class);

  @DataProvider(name = "jpwiseTestData")
  public Object[][] getTestDataFromJPWise() {
    log.info(
        "JpWiseReadMeDataProviderDemoTest: Generating data for DataProvider 'jpwiseTestData'...");
    List<CompatibilityPredicate> browserRules =
        Arrays.asList(
            (EquivalencePartition ep1, EquivalencePartition ep2) -> {
              if (ep1.getName().equals("Safari") && !ep2.getName().equals("macOS")) {
                return false;
              }
              return true;
            });

    TestParameter browserParam =
        TestParameter.of(
            "Browser",
            Arrays.asList(
                CyclingPartition.of("Chrome", Arrays.asList("latest", "previous")),
                SimpleValue.of("Safari")),
            browserRules);

    TestParameter osParam =
        TestParameter.of("OS", SimpleValue.of("macOS"), SimpleValue.of("Windows"));

    CombinationTable combinations;
    try {
      log.debug(
          "JPWISE_DEBUG: JpWiseReadMeDataProviderDemoTest.getTestDataFromJPWise calling JPWise.builder()...");
      combinations = JPWise.builder().parameter(browserParam).parameter(osParam).generatePairwise();
      log.debug(
          "JPWISE_DEBUG: JpWiseReadMeDataProviderDemoTest.getTestDataFromJPWise call to generatePairwise() completed.");
      log.info(
          "JpWiseReadMeDataProviderDemoTest: Generated {} combinations for DataProvider.",
          combinations.size());
    } catch (Throwable t) {
      log.error(
          "JPWISE_DEBUG: ERROR during JPWise.builder()...generatePairwise() "
              + "in JpWiseReadMeDataProviderDemoTest DataProvider:",
          t);
      log.error("Error generating test data in JpWiseReadMeDataProviderDemoTest DataProvider", t);
      throw t;
    }
    return combinations.asDataProvider();
  }

  @Test(dataProvider = "jpwiseTestData")
  public void testFeatureWithVariedConfigs(String description, String browser, String os) {
    log.info("Testing: {}", description);
    if ("Safari".equals(browser)) {
      if (!"macOS".equals(os)) {
        throw new AssertionError(
            "Safari should only be paired with macOS according to rules, but found OS: " + os);
      }
    }
  }
}
