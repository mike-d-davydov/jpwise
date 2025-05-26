package io.github.mikeddavydov.core;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.mikeddavydov.algo.CombinatorialAlgorithm;
import io.github.mikeddavydov.algo.PairwiseAlgorithm;

/** Unit tests for {@link TestGenerator} class. */
public class TestGeneratorTest {
  private TestGenerator generator;
  private TestInput input;
  private SimpleValue chrome;
  private SimpleValue firefox;
  private SimpleValue windows;
  private SimpleValue linux;
  private TestParameter browser;
  private TestParameter os;

  @BeforeMethod
  public void setUp() {
    // Create test values
    chrome = SimpleValue.of("Chrome");
    firefox = SimpleValue.of("Firefox");
    windows = SimpleValue.of("Windows");
    linux = SimpleValue.of("Linux");

    // Create test parameters
    browser = new TestParameter("browser", Arrays.<EquivalencePartition>asList(chrome, firefox));
    os = new TestParameter("os", Arrays.<EquivalencePartition>asList(windows, linux));

    // Create test input
    input = new TestInput();
    input.add(browser);
    input.add(os);

    // Create generator
    generator = new TestGenerator(input);
  }

  @Test
  public void testConstructor() {
    assertNotNull(generator.input(), "Input should be set");
    assertNotNull(generator.result(), "Result table should be initialized");
    assertEquals(generator.result().size(), 0, "Result table should be empty");
  }

  @Test
  public void testInput() {
    TestInput retrievedInput = generator.input();
    assertEquals(retrievedInput.size(), 2, "Should have correct number of parameters");
    assertEquals(retrievedInput.get(0), browser, "Should have browser parameter");
    assertEquals(retrievedInput.get(1), os, "Should have os parameter");
  }

  @Test
  public void testResult() {
    CombinationTable result = generator.result();
    assertNotNull(result, "Result should not be null");
    assertEquals(result.size(), 0, "Result should start empty");
  }

  @Test
  public void testSpan() {
    // For 2 parameters with 2 values each, we should have 4 possible pairs:
    // Chrome-Windows, Chrome-Linux, Firefox-Windows, Firefox-Linux
    assertEquals(generator.span(), 4, "Should calculate correct number of possible pairs");
  }

  @Test
  public void testGenerateWithPairwiseAlgorithm() {
    generator.generate(new PairwiseAlgorithm());
    CombinationTable result = generator.result();

    assertNotNull(result, "Should generate results");
    assertTrue(result.size() > 0, "Should generate some combinations");

    // Verify that all combinations are valid
    for (Combination combination : result.combinations()) {
      assertTrue(combination.isFilled(), "All combinations should be complete");
      assertNotNull(combination.getValue(0), "Should have browser value");
      assertNotNull(combination.getValue(1), "Should have os value");
    }
  }

  @Test
  public void testGenerateWithCombinatorialAlgorithm() {
    generator.generate(new CombinatorialAlgorithm(), 99);
    CombinationTable result = generator.result();

    assertNotNull(result, "Should generate results");
    assertEquals(result.size(), 4, "Should generate all possible combinations");

    // Verify that we have all possible combinations
    boolean foundChromeWindows = false;
    boolean foundChromeMacOS = false;
    boolean foundFirefoxWindows = false;
    boolean foundFirefoxMacOS = false;

    for (Combination combination : result.combinations()) {
      assertTrue(combination.isFilled(), "All combinations should be complete");

      EquivalencePartition browserValue = combination.getValue(0);
      EquivalencePartition osValue = combination.getValue(1);

      if (browserValue.equals(chrome) && osValue.equals(windows)) {
        foundChromeWindows = true;
      } else if (browserValue.equals(chrome) && osValue.equals(linux)) {
        foundChromeMacOS = true;
      } else if (browserValue.equals(firefox) && osValue.equals(windows)) {
        foundFirefoxWindows = true;
      } else if (browserValue.equals(firefox) && osValue.equals(linux)) {
        foundFirefoxMacOS = true;
      }
    }

    assertTrue(foundChromeWindows, "Should generate Chrome-Windows combination");
    assertTrue(foundChromeMacOS, "Should generate Chrome-Linux combination");
    assertTrue(foundFirefoxWindows, "Should generate Firefox-Windows combination");
    assertTrue(foundFirefoxMacOS, "Should generate Firefox-Linux combination");
  }

  @Test
  public void testGenerateWithCompatibilityRules() {
    // Create a rule that Firefox is only compatible with Linux
    List<CompatibilityPredicate> rules =
        Arrays.asList(
            (v1, v2) -> {
              // Only apply rules if we're dealing with browser and OS parameters
              if (!(v1.getParentParameter().getName().equals("browser")
                  && v2.getParentParameter().getName().equals("os"))) {
                return true;
              }

              // Firefox only works with Linux
              if (v1.getName().equals("Firefox")) {
                return v2.getName().equals("Linux");
              }
              return true;
            });

    TestParameter browserWithRules =
        new TestParameter("browser", Arrays.<EquivalencePartition>asList(chrome, firefox), rules);

    TestInput inputWithRules = new TestInput();
    inputWithRules.add(browserWithRules);
    inputWithRules.add(os);

    TestGenerator generatorWithRules = new TestGenerator(inputWithRules);
    generatorWithRules.generate(new CombinatorialAlgorithm(), 99);

    CombinationTable result = generatorWithRules.result();

    // Verify that Firefox only appears with Linux
    for (Combination combination : result.combinations()) {
      if (combination.getValue(0).equals(firefox)) {
        assertEquals(combination.getValue(1), linux, "Firefox should only be combined with Linux");
      }
    }
  }

  @Test
  public void testGenerateWithCustomNWise() {
    // Test that nwise parameter is passed correctly
    final int[] nwiseUsed = new int[1];

    GenerationAlgorithm mockAlgorithm =
        new GenerationAlgorithm() {
          @Override
          public void generate(TestGenerator testGenerator, int nwise) {
            nwiseUsed[0] = nwise;
          }
        };

    generator.generate(mockAlgorithm, 3);
    assertEquals(nwiseUsed[0], 3, "Should pass correct nwise value to algorithm");
  }
}
