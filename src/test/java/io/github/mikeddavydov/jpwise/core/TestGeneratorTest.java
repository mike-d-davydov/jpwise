package io.github.mikeddavydov.jpwise.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.mikeddavydov.jpwise.algo.CombinatorialAlgorithm;
import io.github.mikeddavydov.jpwise.algo.PairwiseAlgorithm;

/** Unit tests for {@link TestGenerator} class. */
public class TestGeneratorTest {
  private TestGenerator generator;
  private TestParameter browser;
  private TestParameter os;

  @BeforeMethod
  public void setUp() {
    // Create test parameters
    browser = new TestParameter(
        "browser",
        Arrays.asList(
            SimpleValue.of("Chrome"),
            SimpleValue.of("Firefox"),
            SimpleValue.of("Safari")));
    os = new TestParameter(
        "os",
        Arrays.asList(
            SimpleValue.of("Windows"),
            SimpleValue.of("macOS"),
            SimpleValue.of("Linux")));

    // Create test input
    TestInput input = new TestInput();
    input.add(browser);
    input.add(os);

    // Create generator
    generator = new TestGenerator(input);
  }

  @Test
  public void testConstructor() {
    assertNotNull(generator, "Should create generator");
    assertEquals(generator.input().size(), 2, "Should have 2 parameters");
  }

  @Test
  public void testInput() {
    TestInput input = generator.input();
    assertNotNull(input, "Should get input");
    assertEquals(input.size(), 2, "Should have 2 parameters");
    assertEquals(input.get(0), browser, "Should have browser parameter");
    assertEquals(input.get(1), os, "Should have os parameter");
  }

  @Test
  public void testResult() {
    assertNotNull(generator.result(), "Should get result");
    assertEquals(generator.result().size(), 0, "Should start with empty result");
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
    assertTrue(result.size() > 0, "Should generate some combinations");

    // Verify that all combinations are valid
    for (Combination combination : result.combinations()) {
      assertTrue(combination.isFilled(), "All combinations should be complete");
      assertNotNull(combination.getValue(0), "Should have browser value");
      assertNotNull(combination.getValue(1), "Should have os value");
    }
  }

  @Test
  public void testGenerateWithCustomAlgorithm() {
    // Test that algorithm is called
    final boolean[] algorithmCalled = new boolean[1];

    GenerationAlgorithm mockAlgorithm = new GenerationAlgorithm() {
      @Override
      public void generate(TestGenerator testGenerator, int nwise) {
        algorithmCalled[0] = true;
      }
    };

    generator.generate(mockAlgorithm);
    assertTrue(algorithmCalled[0], "Should call algorithm");
  }

  @Test
  public void testGenerateWithCustomNWise() {
    // Test that nwise parameter is passed correctly
    final int[] nwiseUsed = new int[1];

    GenerationAlgorithm mockAlgorithm = new GenerationAlgorithm() {
      @Override
      public void generate(TestGenerator testGenerator, int nwise) {
        nwiseUsed[0] = nwise;
      }
    };

    generator.generate(mockAlgorithm, 3);
    assertEquals(nwiseUsed[0], 3, "Should pass correct nwise value to algorithm");
  }
}
