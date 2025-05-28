package io.github.mikeddavydov.jpwise.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import io.github.mikeddavydov.jpwise.JPWise;

public class RulePreprocessorTest {

  @Test
  public void testRulePropagation() {
    // Create test input with known rules
    TestInput input =
        JPWise.builder()
            .parameter(
                TestParameter.of(
                    "Browser",
                    Arrays.asList(
                        SimpleValue.of("Chrome"),
                        SimpleValue.of("Firefox"),
                        SimpleValue.of("Safari")),
                    Arrays.asList(
                        (ep1, ep2) -> { // ep1: Browser partition, ep2: Other
                          // parameter's partition
                          // This rule states: If Browser is "Safari" AND the other
                          // parameter (ep2) is of type "OS",
                          // then that "OS" partition's name must be "macOS".
                          if (ep1.getName().equals("Safari")
                              && ep2.getParentParameter().getName().equals("OS")) {
                            return ep2.getName().equals("macOS");
                          }
                          return true; // Default to compatible if the condition isn't
                          // met.
                        })))
            .parameter(
                TestParameter.of(
                    "OS", Arrays.asList(SimpleValue.of("Windows"), SimpleValue.of("macOS"))))
            .parameter(
                TestParameter.of(
                    "Device", Arrays.asList(SimpleValue.of("Desktop"), SimpleValue.of("Mobile"))))
            .build();

    // Run preprocessor
    RulePreprocessor preprocessor = new RulePreprocessor();
    TestInput processedInput = preprocessor.preprocess(input);

    // Verify that rules were propagated correctly
    List<TestParameter> parameters = processedInput.getTestParameters();

    // Browser parameter should have the original rule
    TestParameter browserParam = parameters.get(0);
    assertEquals(browserParam.getDependencies().size(), 1, "Browser should have one rule");

    // OS parameter should have the same rule
    TestParameter osParam = parameters.get(1);
    assertEquals(osParam.getDependencies().size(), 1, "OS should have one rule");
    assertTrue(
        osParam.getDependencies().containsAll(browserParam.getDependencies()),
        "OS should have the same rule as Browser");

    // Device parameter should not have any rules
    TestParameter deviceParam = parameters.get(2);
    assertTrue(deviceParam.getDependencies().isEmpty(), "Device should not have any rules");
  }
}
