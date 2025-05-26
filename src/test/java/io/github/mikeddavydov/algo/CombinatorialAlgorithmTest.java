package io.github.mikeddavydov.algo;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.mikeddavydov.core.*;

/** Unit tests for {@link CombinatorialAlgorithm} class. */
public class CombinatorialAlgorithmTest {
  private TestInput input;
  private SimpleValue chrome;
  private SimpleValue firefox;
  private SimpleValue safari;
  private SimpleValue windows;
  private SimpleValue macOS;
  private SimpleValue linux;
  private SimpleValue hd;
  private SimpleValue qhd;
  private TestParameter browser;
  private TestParameter os;
  private TestParameter resolution;

  @BeforeMethod
  public void setUp() {
    // Initialize test values
    chrome = SimpleValue.of("Chrome");
    firefox = SimpleValue.of("Firefox");
    safari = SimpleValue.of("Safari");
    windows = SimpleValue.of("Windows");
    macOS = SimpleValue.of("macOS");
    linux = SimpleValue.of("Linux");
    hd = SimpleValue.of("1920x1080");
    qhd = SimpleValue.of("2560x1440");

    // Define browser-OS compatibility rules
    List<CompatibilityPredicate> browserOsRules =
        Arrays.asList(
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

    // Create test parameters with compatibility rules
    browser =
        new TestParameter(
            "browser",
            Arrays.<EquivalencePartition>asList(chrome, firefox, safari),
            browserOsRules);
    os = new TestParameter("os", Arrays.<EquivalencePartition>asList(windows, macOS, linux));
    resolution = new TestParameter("resolution", Arrays.<EquivalencePartition>asList(hd, qhd));

    // Create test input
    input = new TestInput();
    input.add(browser);
    input.add(os);
    input.add(resolution);
  }

  @Test
  public void testBasicCombinatorialGeneration() {
    TestGenerator generator = new TestGenerator(input);
    generator.generate(new CombinatorialAlgorithm(), 99); // Use large number for full coverage

    CombinationTable result = generator.result();
    assertNotNull(result, "Should generate results");
    assertTrue(result.size() > 0, "Should generate some combinations");

    // Verify that all combinations are valid
    for (Combination combination : result.combinations()) {
      assertTrue(combination.isFilled(), "All combinations should be complete");
      assertTrue(
          combination.checkNoConflicts(new CombinatorialAlgorithm()),
          "Combinations should be valid");
    }
  }

  @Test
  public void testCompatibilityRules() {
    TestGenerator generator = new TestGenerator(input);
    generator.generate(new CombinatorialAlgorithm(), 99);

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
  public void testFullCoverage() {
    TestGenerator generator = new TestGenerator(input);
    generator.generate(new CombinatorialAlgorithm(), 99);

    CombinationTable result = generator.result();

    // Get all combinations
    Set<String> combinations = new HashSet<>();
    for (Combination combination : result.combinations()) {
      combinations.add(
          combination.getValue(0).getName()
              + ":"
              + combination.getValue(1).getName()
              + ":"
              + combination.getValue(2).getName());
    }

    // Verify that we have all valid combinations
    // Chrome/Firefox should have combinations with all OS and resolutions
    assertTrue(combinations.contains("Chrome:Windows:1920x1080"), "Should have Chrome-Windows-HD");
    assertTrue(combinations.contains("Chrome:Windows:2560x1440"), "Should have Chrome-Windows-QHD");
    assertTrue(combinations.contains("Chrome:macOS:1920x1080"), "Should have Chrome-macOS-HD");
    assertTrue(combinations.contains("Chrome:macOS:2560x1440"), "Should have Chrome-macOS-QHD");
    assertTrue(combinations.contains("Chrome:Linux:1920x1080"), "Should have Chrome-Linux-HD");
    assertTrue(combinations.contains("Chrome:Linux:2560x1440"), "Should have Chrome-Linux-QHD");

    assertTrue(
        combinations.contains("Firefox:Windows:1920x1080"), "Should have Firefox-Windows-HD");
    assertTrue(
        combinations.contains("Firefox:Windows:2560x1440"), "Should have Firefox-Windows-QHD");
    assertTrue(combinations.contains("Firefox:macOS:1920x1080"), "Should have Firefox-macOS-HD");
    assertTrue(combinations.contains("Firefox:macOS:2560x1440"), "Should have Firefox-macOS-QHD");
    assertTrue(combinations.contains("Firefox:Linux:1920x1080"), "Should have Firefox-Linux-HD");
    assertTrue(combinations.contains("Firefox:Linux:2560x1440"), "Should have Firefox-Linux-QHD");

    // Safari should only have combinations with macOS
    assertTrue(combinations.contains("Safari:macOS:1920x1080"), "Should have Safari-macOS-HD");
    assertTrue(combinations.contains("Safari:macOS:2560x1440"), "Should have Safari-macOS-QHD");
    assertFalse(
        combinations.contains("Safari:Windows:1920x1080"), "Should not have Safari-Windows-HD");
    assertFalse(
        combinations.contains("Safari:Windows:2560x1440"), "Should not have Safari-Windows-QHD");
    assertFalse(combinations.contains("Safari:Linux:1920x1080"), "Should not have Safari-Linux-HD");
    assertFalse(
        combinations.contains("Safari:Linux:2560x1440"), "Should not have Safari-Linux-QHD");
  }

  @Test
  public void testLimit() {
    // Test with a small limit
    TestGenerator generator = new TestGenerator(input);
    generator.generate(new CombinatorialAlgorithm(), 5);

    CombinationTable result = generator.result();
    assertNotNull(result, "Should generate results");
    assertEquals(result.size(), 5, "Should respect the limit");
  }

  @Test
  public void testSmallInput() {
    // Test with minimal input (2 parameters, 2 values each)
    TestInput smallInput = new TestInput();
    smallInput.add(
        new TestParameter("param1", Arrays.asList(SimpleValue.of("A"), SimpleValue.of("B"))));
    smallInput.add(
        new TestParameter("param2", Arrays.asList(SimpleValue.of("1"), SimpleValue.of("2"))));

    TestGenerator generator = new TestGenerator(smallInput);
    generator.generate(new CombinatorialAlgorithm(), 99);

    CombinationTable result = generator.result();
    assertEquals(result.size(), 4, "Should generate all possible combinations");

    // Verify all combinations
    Set<String> combinations = new HashSet<>();
    for (Combination combination : result.combinations()) {
      combinations.add(combination.getValue(0).getName() + ":" + combination.getValue(1).getName());
    }

    assertTrue(combinations.contains("A:1"), "Should have A-1 combination");
    assertTrue(combinations.contains("A:2"), "Should have A-2 combination");
    assertTrue(combinations.contains("B:1"), "Should have B-1 combination");
    assertTrue(combinations.contains("B:2"), "Should have B-2 combination");
  }
}
