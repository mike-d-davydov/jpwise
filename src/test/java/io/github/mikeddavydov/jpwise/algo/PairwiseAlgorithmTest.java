package io.github.mikeddavydov.jpwise.algo;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

import io.github.mikeddavydov.jpwise.core.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit tests for {@link PairwiseAlgorithm} class. */
public class PairwiseAlgorithmTest {
  private TestInput input;
  private TestParameter browser;
  private TestParameter os;
  private TestParameter resolution;

  @BeforeMethod
  public void setUp() {
    // Define browser-OS compatibility rules
    List<CompatibilityPredicate> browserOsRules =
        asList(
            (v1, v2) -> {
              // Only apply Safari-macOS rule if we're dealing with browser and OS
              if (!(v1.getParentParameter().getName().equals("browser")
                  && v2.getParentParameter().getName().equals("os"))) {
                return true;
              }

              // Safari only works with macOS
              if (v1.getName().equals("Safari")) {
                return v2.getName().equals("macOS");
              }
              // All other combinations are compatible
              return true;
            });

    // Create test parameters with inlined partition creation
    browser =
        TestParameter.of(
            "browser",
            asList(SimpleValue.of("Chrome"), SimpleValue.of("Firefox"), SimpleValue.of("Safari")),
            browserOsRules);
    os =
        TestParameter.of(
            "os", SimpleValue.of("Windows"), SimpleValue.of("macOS"), SimpleValue.of("Linux"));
    resolution =
        TestParameter.of("resolution", SimpleValue.of("1920x1080"), SimpleValue.of("2560x1440"));

    // Create test input
    input = new TestInput();
    input.add(browser);
    input.add(os);
    input.add(resolution);
  }

  @Test
  public void testBasicPairwiseGeneration() {
    TestGenerator generator = new TestGenerator(input);
    generator.generate(new PairwiseAlgorithm());

    CombinationTable result = generator.result();
    assertNotNull(result, "Should generate results");
    assertTrue(result.size() > 0, "Should generate some combinations");

    // Verify that all combinations are valid
    for (Combination combination : result.combinations()) {
      assertTrue(combination.isFilled(), "All combinations should be complete");
      assertTrue(
          combination.checkNoConflicts(new PairwiseAlgorithm()), "Combinations should be valid");
    }
  }

  @Test
  public void testCompatibilityRules() {
    TestGenerator generator = new TestGenerator(input);
    generator.generate(new PairwiseAlgorithm());

    CombinationTable result = generator.result();

    // Verify that Safari only appears with macOS
    for (Combination combination : result.combinations()) {
      EquivalencePartition browserValue = combination.getValue(0);
      EquivalencePartition osValue = combination.getValue(1);

      if (browserValue.getName().equals("Safari")) {
        assertEquals(osValue.getName(), "macOS", "Safari should only be combined with macOS");
      }
    }
  }

  @Test
  public void testPairCoverage() {
    TestGenerator generator = new TestGenerator(input);
    generator.generate(new PairwiseAlgorithm());

    CombinationTable result = generator.result();

    // Get all parameter pairs
    Set<String> pairs = new HashSet<>();
    for (Combination combination : result.combinations()) {
      // Browser-OS pairs
      pairs.add(combination.getValue(0).getName() + ":" + combination.getValue(1).getName());
      // Browser-Resolution pairs
      pairs.add(combination.getValue(0).getName() + ":" + combination.getValue(2).getName());
      // OS-Resolution pairs
      pairs.add(combination.getValue(1).getName() + ":" + combination.getValue(2).getName());
    }

    // Verify that we have all valid pairs
    // Chrome/Firefox should have pairs with all OS and resolutions
    assertTrue(pairs.contains("Chrome:Windows"), "Should have Chrome-Windows pair");
    assertTrue(pairs.contains("Chrome:macOS"), "Should have Chrome-macOS pair");
    assertTrue(pairs.contains("Chrome:Linux"), "Should have Chrome-Linux pair");
    assertTrue(pairs.contains("Firefox:Windows"), "Should have Firefox-Windows pair");
    assertTrue(pairs.contains("Firefox:macOS"), "Should have Firefox-macOS pair");
    assertTrue(pairs.contains("Firefox:Linux"), "Should have Firefox-Linux pair");

    // Safari should only have pairs with macOS
    assertTrue(pairs.contains("Safari:macOS"), "Should have Safari-macOS pair");
    assertFalse(pairs.contains("Safari:Windows"), "Should not have Safari-Windows pair");
    assertFalse(pairs.contains("Safari:Linux"), "Should not have Safari-Linux pair");
  }

  @Test
  public void testDifferentJumpValues() {
    int[] jumpValues = {2, 3, 4, 5};
    for (int jump : jumpValues) {
      TestGenerator generator = new TestGenerator(input);
      generator.generate(new PairwiseAlgorithm(jump));

      CombinationTable result = generator.result();
      assertNotNull(result, "Should generate results with jump=" + jump);
      assertTrue(result.size() > 0, "Should generate some combinations with jump=" + jump);

      // Verify that all combinations are valid
      for (Combination combination : result.combinations()) {
        assertTrue(combination.isFilled(), "All combinations should be complete");
        assertTrue(
            combination.checkNoConflicts(new PairwiseAlgorithm()), "Combinations should be valid");
      }
    }
  }
}
