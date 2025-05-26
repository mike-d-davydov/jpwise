package io.github.mikeddavydov;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.mikeddavydov.algo.CombinatorialAlgorithm;
import io.github.mikeddavydov.algo.PairwiseAlgorithm;
import io.github.mikeddavydov.core.*;

/** Tests for the JPWise facade class. */
public class JPWiseTest {
  private TestParameter browser;
  private TestParameter os;
  private TestParameter resolution;
  private SimpleValue chrome;
  private SimpleValue firefox;
  private SimpleValue safari;
  private SimpleValue windows;
  private SimpleValue macOS;
  private SimpleValue linux;
  private SimpleValue hd;
  private SimpleValue uhd;

  @BeforeMethod
  public void setUp() {
    // Create test values
    chrome = SimpleValue.of("Chrome", "116.0");
    firefox = SimpleValue.of("Firefox", "118.0");
    safari = SimpleValue.of("Safari", "17.0");
    windows = SimpleValue.of("Windows", "11");
    macOS = SimpleValue.of("macOS", "14.1");
    linux = SimpleValue.of("Linux", "6.5");
    hd = SimpleValue.of("HD", "1920x1080");
    uhd = SimpleValue.of("4K", "3840x2160");

    // Create test parameters
    browser =
        new TestParameter("browser", Arrays.<EquivalencePartition>asList(chrome, firefox, safari));
    os = new TestParameter("os", Arrays.<EquivalencePartition>asList(windows, macOS, linux));
    resolution = new TestParameter("resolution", Arrays.<EquivalencePartition>asList(hd, uhd));
  }

  @Test
  public void testDirectPairwiseGeneration() {
    CombinationTable results = JPWise.generatePairwise(browser, os);
    assertNotNull(results, "Should generate results");
    assertTrue(results.size() > 0, "Should generate some combinations");
  }

  @Test
  public void testDirectPairwiseGenerationWithCollection() {
    List<TestParameter> params = Arrays.asList(browser, os, resolution);
    CombinationTable results = JPWise.generatePairwise(params);
    assertNotNull(results, "Should generate results");
    assertTrue(results.size() > 0, "Should generate some combinations");
  }

  @Test
  public void testDirectPairwiseGenerationWithAlgorithm() {
    PairwiseAlgorithm algorithm = new PairwiseAlgorithm(4);
    CombinationTable results = JPWise.generatePairwise(algorithm, browser, os);
    assertNotNull(results, "Should generate results");
    assertTrue(results.size() > 0, "Should generate some combinations");
  }

  @Test
  public void testDirectCombinatorialGeneration() {
    CombinationTable results = JPWise.generateCombinatorial(browser, os);
    assertNotNull(results, "Should generate results");
    assertTrue(results.size() > 0, "Should generate some combinations");
  }

  @Test
  public void testDirectCombinatorialGenerationWithLimit() {
    List<TestParameter> params = Arrays.asList(browser, os);
    CombinationTable results = JPWise.generateCombinatorial(params, 4);
    assertNotNull(results, "Should generate results");
    assertEquals(results.size(), 4, "Should respect combination limit");
  }

  @Test
  public void testDirectCombinatorialGenerationWithAlgorithm() {
    CombinatorialAlgorithm algorithm = new CombinatorialAlgorithm();
    CombinationTable results = JPWise.generateCombinatorial(algorithm, 6, browser, os);
    assertNotNull(results, "Should generate results");
    assertTrue(results.size() <= 6, "Should respect combination limit");
  }

  @Test
  public void testBuilderFactoryMethods() {
    // Test builder()
    assertNotNull(JPWise.builder(), "builder() should return new instance");

    // Test withParameters(varargs)
    CombinationTable results1 = JPWise.withParameters(browser, os).generatePairwise();
    assertNotNull(results1, "Should generate results with varargs parameters");

    // Test withParameters(collection)
    List<TestParameter> params = Arrays.asList(browser, os);
    CombinationTable results2 = JPWise.withParameters(params).generatePairwise();
    assertNotNull(results2, "Should generate results with collection parameters");
  }

  @Test
  public void testInlineParameterCreation() {
    CombinationTable results =
        JPWise.builder()
            .parameter("browser", chrome, firefox)
            .parameter("os", windows, macOS)
            .generatePairwise();

    assertNotNull(results, "Should generate results with inline parameters");
    assertTrue(results.size() > 0, "Should generate some combinations");
  }

  @Test
  public void testInlineParameterCreationWithRules() {
    // Define a rule that Safari only works with macOS
    List<CompatibilityPredicate> rules =
        Arrays.asList(
            (v1, v2) -> {
              // Only apply rules if we're dealing with browser and OS parameters
              if (!(v1.getParentParameter().getName().equals("browser")
                  && v2.getParentParameter().getName().equals("os"))) {
                return true;
              }

              // Safari only works with macOS
              if (v1.getName().equals("Safari")) {
                return v2.getName().equals("macOS");
              }
              return true;
            });

    CombinationTable results =
        JPWise.builder()
            .parameter(
                "browser", Arrays.<EquivalencePartition>asList(chrome, firefox, safari), rules)
            .parameter(
                "os",
                Arrays.<EquivalencePartition>asList(windows, macOS, linux),
                Arrays.<CompatibilityPredicate>asList())
            .generatePairwise();

    assertNotNull(results, "Should generate results with rules");
    assertTrue(results.size() > 0, "Should generate some combinations");

    // Verify Safari-macOS rule is respected
    for (Combination combination : results.combinations()) {
      if (combination.getValue(0).getName().equals("Safari")) {
        assertEquals(
            combination.getValue(1).getName(), "macOS", "Safari should only be paired with macOS");
      }
    }
  }

  @Test
  public void testFluentBuilderAPI() {
    CombinationTable results = JPWise.builder().parameter(browser).parameter(os).generatePairwise();

    assertNotNull(results, "Should generate results");
    assertTrue(results.size() > 0, "Should generate some combinations");
  }

  @Test
  public void testParametersVarargs() {
    CombinationTable results =
        JPWise.builder().parameters(browser, os, resolution).generatePairwise();

    assertNotNull(results, "Should generate results");
    assertTrue(results.size() > 0, "Should generate some combinations");
  }

  @Test
  public void testParametersCollection() {
    List<TestParameter> params = Arrays.asList(browser, os, resolution);
    CombinationTable results = JPWise.builder().parameters(params).generatePairwise();

    assertNotNull(results, "Should generate results");
    assertTrue(results.size() > 0, "Should generate some combinations");
  }

  @Test
  public void testBuildAndGenerateSeparately() {
    TestInput input = JPWise.builder().parameter(browser).parameter(os).build();

    CombinationTable results = JPWise.generatePairwise(input);

    assertNotNull(results, "Should generate results");
    assertTrue(results.size() > 0, "Should generate some combinations");
  }

  @Test
  public void testPairwiseWithCustomJump() {
    CombinationTable results =
        JPWise.builder().parameter(browser).parameter(os).generatePairwise(3);

    assertNotNull(results, "Should generate results");
    assertTrue(results.size() > 0, "Should generate some combinations");
  }

  @Test
  public void testCombinatorialWithLimit() {
    CombinationTable results =
        JPWise.builder().parameter(browser).parameter(os).generateCombinatorial(4);

    assertNotNull(results, "Should generate results");
    assertEquals(results.size(), 4, "Should respect combination limit");
  }

  @Test
  public void testCustomPairwiseAlgorithm() {
    TestInput input = JPWise.builder().parameter(browser).parameter(os).build();

    PairwiseAlgorithm algorithm = new PairwiseAlgorithm(5);
    CombinationTable results = JPWise.generatePairwise(input, algorithm);

    assertNotNull(results, "Should generate results");
    assertTrue(results.size() > 0, "Should generate some combinations");
  }

  @Test
  public void testCustomCombinatorialAlgorithm() {
    TestInput input = JPWise.builder().parameter(browser).parameter(os).build();

    CombinatorialAlgorithm algorithm = new CombinatorialAlgorithm();
    CombinationTable results = JPWise.generateCombinatorial(input, algorithm, 6);

    assertNotNull(results, "Should generate results");
    assertTrue(results.size() <= 6, "Should respect combination limit");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullParameter() {
    JPWise.builder().parameter(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullParametersArray() {
    JPWise.builder().parameters((TestParameter[]) null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullParametersCollection() {
    JPWise.builder().parameters((List<TestParameter>) null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullParameterName() {
    JPWise.builder().parameter(null, chrome, firefox);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullPartitionsArray() {
    JPWise.builder().parameter("browser", (EquivalencePartition[]) null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullRules() {
    JPWise.builder().parameter("browser", Arrays.asList(chrome, firefox), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidJumpValue() {
    JPWise.builder().parameter(browser).generatePairwise(0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidCombinatorialLimit() {
    JPWise.builder().parameter(browser).generateCombinatorial(0);
  }
}
