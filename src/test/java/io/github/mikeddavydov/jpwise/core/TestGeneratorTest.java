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
  private TestInput testInput;

  @BeforeMethod
  public void setUp() {
    // Create test parameters
    browser =
        new TestParameter(
            "browser",
            Arrays.asList(
                SimpleValue.of("Chrome"), SimpleValue.of("Firefox"), SimpleValue.of("Safari")));
    os =
        new TestParameter(
            "os",
            Arrays.asList(
                SimpleValue.of("Windows"), SimpleValue.of("macOS"), SimpleValue.of("Linux")));

    // Create test input
    testInput = new TestInput();
    testInput.add(browser);
    testInput.add(os);

    // Create generator
    generator = new TestGenerator(testInput);
  }

  @Test
  public void testConstructor() {
    assertNotNull(generator, "Should create generator");
    assertEquals(generator.getInput().size(), 2, "Should have 2 parameters");
  }

  @Test
  public void testInput() {
    TestInput currentInput = generator.getInput();
    assertNotNull(currentInput, "Should get input");
    assertEquals(currentInput.size(), 2, "Should have 2 parameters");
    assertEquals(
        currentInput.get(0).getName(), browser.getName(), "First parameter name should match");
    assertEquals(
        currentInput.get(0).getPartitions().size(),
        browser.getPartitions().size(),
        "First parameter partition count should match");
    assertEquals(currentInput.get(1).getName(), os.getName(), "Second parameter name should match");
    assertEquals(
        currentInput.get(1).getPartitions().size(),
        os.getPartitions().size(),
        "Second parameter partition count should match");
  }

  @Test
  public void testGenerateWithPairwiseAlgorithm() {
    TestGenerator generator = new TestGenerator(testInput);
    CombinationTable result = generator.generate(new PairwiseAlgorithm());

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
    TestGenerator generator = new TestGenerator(testInput);
    CombinationTable result = generator.generate(new CombinatorialAlgorithm(99));

    assertNotNull(result, "Should generate results");
    assertTrue(result.size() > 0, "Should generate some combinations");

    // Verify that all combinations are valid
    for (Combination combination : result.combinations()) {
      assertTrue(combination.isFilled(), "All combinations should be complete");
      assertNotNull(combination.getValue(0), "Should have browser value");
      assertNotNull(combination.getValue(1), "Should have os value");
    }
  }
}
