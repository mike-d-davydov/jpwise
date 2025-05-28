package io.github.mikeddavydov.jpwise.algo;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.annotations.Test;

import io.github.mikeddavydov.jpwise.JPWise;
import io.github.mikeddavydov.jpwise.core.Combination;
import io.github.mikeddavydov.jpwise.core.CombinationTable;
import io.github.mikeddavydov.jpwise.core.EquivalencePartition;
import io.github.mikeddavydov.jpwise.core.SimpleValue;

public class PairwiseAlgorithmRuleTest {

  @Test
  public void testPairwiseGenerationWithRules() {
    JPWise.InputBuilder builder =
        JPWise.builder()
            .parameter(
                "Browser",
                Arrays.<EquivalencePartition>asList(
                    SimpleValue.of("Chrome"), SimpleValue.of("Firefox"), SimpleValue.of("Safari")),
                Arrays.asList(
                    (ep1, ep2) -> { // ep1: Browser, ep2: Other
                      if (ep1.getName().equals("Safari")
                          && ep2.getParentParameter().getName().equals("OS")) {
                        return ep2.getName().equals("macOS");
                      }
                      return true;
                    }))
            .parameter(
                "OS", // OS rule can be empty if Browser's rule covers it due to symmetric
                // check
                Arrays.<EquivalencePartition>asList(
                    SimpleValue.of("Windows"), SimpleValue.of("macOS")),
                new ArrayList<>())
            .parameter(
                "Device",
                Arrays.<EquivalencePartition>asList(
                    SimpleValue.of("Desktop"), SimpleValue.of("Mobile")),
                Arrays.asList(
                    (ep1, ep2) -> { // ep1: Device, ep2: Other
                      if (ep1.getName().equals("Mobile")
                          && ep2.getParentParameter().getName().equals("Resolution")) {
                        return !ep2.getName().equals("3840x2160");
                      }
                      return true;
                    }))
            .parameter(
                "Resolution", // Resolution rules can be empty if other params cover them
                Arrays.<EquivalencePartition>asList(
                    SimpleValue.of("1920x1080"), SimpleValue.of("3840x2160")), // This is 4K
                new ArrayList<>())
            .parameter(
                "ColorDepth",
                Arrays.<EquivalencePartition>asList(
                    SimpleValue.of("8-bit"), SimpleValue.of("10-bit")),
                Arrays.asList(
                    (ep1, ep2) -> { // ep1: ColorDepth, ep2: Other
                      if (ep1.getName().equals("10-bit")
                          && ep2.getParentParameter().getName().equals("Resolution")) {
                        return ep2.getName().equals("3840x2160");
                      }
                      return true;
                    }))
            .parameter(
                "Network",
                Arrays.<EquivalencePartition>asList(
                    SimpleValue.of("WiFi"), SimpleValue.of("Ethernet")),
                new ArrayList<>())
            .parameter(
                "Language",
                Arrays.<EquivalencePartition>asList(SimpleValue.of("EN"), SimpleValue.of("FR")),
                new ArrayList<>())
            .parameter(
                "Timezone",
                Arrays.<EquivalencePartition>asList(
                    SimpleValue.of("UTC+0"), SimpleValue.of("UTC+2")),
                new ArrayList<>())
            .parameter(
                "Theme",
                Arrays.<EquivalencePartition>asList(
                    SimpleValue.of("Light"), SimpleValue.of("Dark")),
                new ArrayList<>())
            .parameter(
                "FontSize",
                Arrays.<EquivalencePartition>asList(
                    SimpleValue.of("Small"), SimpleValue.of("Large")),
                new ArrayList<>());

    CombinationTable table = builder.generatePairwise();
    // Assert all combinations are valid
    for (Combination combination : table.combinations()) {
      EquivalencePartition[] values = combination.getValues();
      String browser = combination.getValue(0).getName(); // Browser
      String os = combination.getValue(1).getName(); // OS
      String device = combination.getValue(2).getName(); // Device
      String resolution = combination.getValue(3).getName(); // Resolution
      String colorDepth = combination.getValue(4).getName(); // ColorDepth

      // Verify Safari-macOS rule
      if ("Safari".equals(browser)) {
        org.testng.Assert.assertEquals(
            os, "macOS", "Safari (" + browser + ") should only be paired with macOS, not " + os);
      }

      // Verify Mobile-4K rule
      if ("Mobile".equals(device)) {
        org.testng.Assert.assertNotEquals(
            resolution,
            "3840x2160",
            "Mobile device ("
                + device
                + ") should not be paired with 4K resolution ("
                + resolution
                + ")");
      }

      // Verify 10-bit color depth rule (10-bit implies 4K)
      if ("10-bit".equals(colorDepth)) {
        org.testng.Assert.assertEquals(
            resolution,
            "3840x2160",
            "10-bit color depth ("
                + colorDepth
                + ") should only be paired with 4K resolution, not "
                + resolution);
      }
    }
  }
}
