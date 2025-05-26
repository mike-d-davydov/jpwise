package com.functest.jpwise.algo;

import static org.testng.Assert.*;

import com.functest.jpwise.core.*;
import java.util.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests that compare the results of the old and new pairwise algorithms to ensure they generate
 * equivalent test cases.
 */
public class PairwiseAlgorithmComparisonTest {
  private TestInput browserInput;
  private TestInput simpleInput;
  private TestInput complexInput;

  @BeforeMethod
  public void setUp() {
    // Set up browser testing input with compatibility rules
    List<CompatibilityPredicate> browserOsRules =
        Arrays.asList(
            (v1, v2) -> {
              // Only apply Safari-macOS rule if we're dealing with browser and OS
              if (v1.getParentParameter().getName().equals("browser")
                      && v2.getParentParameter().getName().equals("os")
                  || v2.getParentParameter().getName().equals("browser")
                      && v1.getParentParameter().getName().equals("os")) {
                // Ensure v1 is the browser value
                if (v2.getParentParameter().getName().equals("browser")) {
                  EquivalencePartition<?> temp = v1;
                  v1 = v2;
                  v2 = temp;
                }
                // Safari only works with macOS
                if (v1.getName().equals("Safari")) {
                  return v2.getName().equals("macOS");
                }
              }
              // All other combinations are compatible
              return true;
            });

    TestParameter browser =
        new TestParameter(
            "browser",
            Arrays.<EquivalencePartition<?>>asList(
                SimpleValue.of("Chrome", "116.0.5845.96"),
                SimpleValue.of("Firefox", "118.0.2"),
                SimpleValue.of("Safari", "17.0")),
            browserOsRules);

    TestParameter os =
        new TestParameter(
            "os",
            Arrays.<EquivalencePartition<?>>asList(
                SimpleValue.of("Windows", "10.0.19045"),
                SimpleValue.of("macOS", "14.1"),
                SimpleValue.of("Linux", "6.5.7")));

    TestParameter resolution =
        new TestParameter(
            "resolution",
            Arrays.<EquivalencePartition<?>>asList(
                SimpleValue.of("HD", "1920x1080"), SimpleValue.of("QHD", "2560x1440")));

    browserInput = new TestInput();
    browserInput.add(browser);
    browserInput.add(os);
    browserInput.add(resolution);

    // Set up simple input for basic comparison
    TestParameter color =
        new TestParameter(
            "color",
            Arrays.<EquivalencePartition<?>>asList(
                SimpleValue.of("Primary", "red"), SimpleValue.of("Secondary", "blue")));
    TestParameter size =
        new TestParameter(
            "size",
            Arrays.<EquivalencePartition<?>>asList(
                SimpleValue.of("Compact", "small"), SimpleValue.of("Extended", "large")));
    simpleInput = new TestInput();
    simpleInput.add(color);
    simpleInput.add(size);

    // Set up complex input with more parameters and values
    TestParameter userRole =
        new TestParameter(
            "userRole",
            Arrays.<EquivalencePartition<?>>asList(
                SimpleValue.of("Admin", "FULL_ACCESS"),
                SimpleValue.of("Manager", "DEPARTMENT_ACCESS"),
                SimpleValue.of("User", "BASIC_ACCESS")));

    TestParameter department =
        new TestParameter(
            "department",
            Arrays.<EquivalencePartition<?>>asList(
                SimpleValue.of("Sales", "REVENUE_CRITICAL"),
                SimpleValue.of("Engineering", "TECHNICAL"),
                SimpleValue.of("Support", "CUSTOMER_FACING")));

    TestParameter accessLevel =
        new TestParameter(
            "accessLevel",
            Arrays.<EquivalencePartition<?>>asList(
                SimpleValue.of("Full", "ALL_OPERATIONS"),
                SimpleValue.of("ReadWrite", "MODIFY_CONTENT"),
                SimpleValue.of("ReadOnly", "VIEW_CONTENT")));

    complexInput = new TestInput();
    complexInput.add(userRole);
    complexInput.add(department);
    complexInput.add(accessLevel);
  }

  @Test
  public void testSimpleInputComparison() {
    // Generate test cases with both algorithms
    TestGenerator oldGen = new TestGenerator(simpleInput);
    oldGen.generate(new LegacyPairwiseAlgorithm(3));
    CombinationTable oldResults = oldGen.result();

    TestGenerator newGen = new TestGenerator(simpleInput);
    newGen.generate(new PairwiseAlgorithm(3));
    CombinationTable newResults = newGen.result();

    // Compare results
    assertValidResults(oldResults, "Old algorithm results should be valid");
    assertValidResults(newResults, "New algorithm results should be valid");
    assertEquivalentCoverage(oldResults, newResults, simpleInput);
  }

  @Test
  public void testBrowserInputComparison() {
    // Generate test cases with both algorithms
    TestGenerator oldGen = new TestGenerator(browserInput);
    oldGen.generate(new LegacyPairwiseAlgorithm(3));
    CombinationTable oldResults = oldGen.result();

    TestGenerator newGen = new TestGenerator(browserInput);
    newGen.generate(new PairwiseAlgorithm(3));
    CombinationTable newResults = newGen.result();

    // Compare results
    assertValidResults(oldResults, "Old algorithm results should be valid");
    assertValidResults(newResults, "New algorithm results should be valid");
    assertEquivalentCoverage(oldResults, newResults, browserInput);
    assertCompatibilityRulesRespected(
        oldResults, "Old algorithm should respect compatibility rules");
    assertCompatibilityRulesRespected(
        newResults, "New algorithm should respect compatibility rules");
  }

  @Test
  public void testComplexInputComparison() {
    // Generate test cases with both algorithms
    TestGenerator oldGen = new TestGenerator(complexInput);
    oldGen.generate(new LegacyPairwiseAlgorithm(3));
    CombinationTable oldResults = oldGen.result();

    TestGenerator newGen = new TestGenerator(complexInput);
    newGen.generate(new PairwiseAlgorithm(3));
    CombinationTable newResults = newGen.result();

    // Compare results
    assertValidResults(oldResults, "Old algorithm results should be valid");
    assertValidResults(newResults, "New algorithm results should be valid");
    assertEquivalentCoverage(oldResults, newResults, complexInput);
  }

  @Test
  public void testDifferentJumpValues() {
    int[] jumpValues = {2, 3, 4, 5};
    for (int jump : jumpValues) {
      // Generate with old algorithm
      TestGenerator oldGen = new TestGenerator(browserInput);
      oldGen.generate(new LegacyPairwiseAlgorithm(jump));
      CombinationTable oldResults = oldGen.result();

      // Generate with new algorithm
      TestGenerator newGen = new TestGenerator(browserInput);
      newGen.generate(new PairwiseAlgorithm(jump));
      CombinationTable newResults = newGen.result();

      // Compare results
      assertValidResults(oldResults, "Old algorithm results should be valid with jump=" + jump);
      assertValidResults(newResults, "New algorithm results should be valid with jump=" + jump);
      assertEquivalentCoverage(oldResults, newResults, browserInput);
    }
  }

  // Helper methods

  private void assertValidResults(CombinationTable results, String message) {
    assertNotNull(results, message + " - Results should not be null");
    assertTrue(results.size() > 0, message + " - Should generate some combinations");

    // Verify each combination is complete
    for (Combination combination : results.combinations()) {
      assertTrue(combination.isFilled(), message + " - All combinations should be complete");
      assertNotNull(combination.getKey(), message + " - All combinations should have valid keys");
    }
  }

  private void assertEquivalentCoverage(
      CombinationTable oldResults, CombinationTable newResults, TestInput input) {
    // Get all parameter pairs covered by each algorithm
    Set<String> oldPairs = extractAllPairs(oldResults, input);
    Set<String> newPairs = extractAllPairs(newResults, input);

    // Verify that both algorithms achieve sufficient coverage
    assertTrue(oldPairs.size() > 0, "Old algorithm should generate some pairs");
    assertTrue(newPairs.size() > 0, "New algorithm should generate some pairs");

    // Calculate expected coverage based on input parameters
    int expectedMinPairs = calculateMinimumExpectedPairs(input);
    assertTrue(
        oldPairs.size() >= expectedMinPairs,
        String.format(
            "Old algorithm should cover at least %d pairs, but only covered %d",
            expectedMinPairs, oldPairs.size()));
    assertTrue(
        newPairs.size() >= expectedMinPairs,
        String.format(
            "New algorithm should cover at least %d pairs, but only covered %d",
            expectedMinPairs, newPairs.size()));

    // Verify that both algorithms respect compatibility rules
    verifyCompatibilityRules(oldPairs, input);
    verifyCompatibilityRules(newPairs, input);
  }

  private int calculateMinimumExpectedPairs(TestInput input) {
    List<TestParameter> params = input.getTestParameters();
    int totalPairs = 0;

    // For each pair of parameters
    for (int i = 0; i < params.size(); i++) {
      for (int j = i + 1; j < params.size(); j++) {
        TestParameter param1 = params.get(i);
        TestParameter param2 = params.get(j);

        // Count compatible pairs between these parameters
        int compatiblePairs = 0;
        for (EquivalencePartition<?> v1 : param1.getPartitions()) {
          for (EquivalencePartition<?> v2 : param2.getPartitions()) {
            if (param1.areCompatible(v1, v2) && param2.areCompatible(v2, v1)) {
              compatiblePairs++;
            }
          }
        }
        totalPairs += compatiblePairs;
      }
    }
    return totalPairs;
  }

  private void verifyCompatibilityRules(Set<String> pairs, TestInput input) {
    List<TestParameter> params = input.getTestParameters();

    for (String pair : pairs) {
      String[] parts = pair.split(":");
      String param1Name = parts[0];
      String value1Name = parts[1];
      String param2Name = parts[2];
      String value2Name = parts[3];

      // Find the parameters
      TestParameter param1 = null;
      TestParameter param2 = null;
      for (TestParameter param : params) {
        if (param.getName().equals(param1Name)) param1 = param;
        if (param.getName().equals(param2Name)) param2 = param;
      }

      // Only check compatibility if either parameter has rules
      if (!param1.getDependencies().isEmpty() || !param2.getDependencies().isEmpty()) {
        // Find the values
        EquivalencePartition<?> value1 = param1.getPartitionByName(value1Name);
        EquivalencePartition<?> value2 = param2.getPartitionByName(value2Name);

        // Verify compatibility
        assertTrue(
            param1.areCompatible(value1, value2) && param2.areCompatible(value2, value1),
            String.format(
                "Pair %s:%s and %s:%s should be compatible",
                param1Name, value1Name, param2Name, value2Name));
      }
    }
  }

  private Set<String> extractAllPairs(CombinationTable results, TestInput input) {
    Set<String> pairs = new HashSet<>();
    List<TestParameter> params = input.getTestParameters();

    // For each combination
    for (Combination combination : results.combinations()) {
      // For each pair of parameters
      for (int i = 0; i < params.size(); i++) {
        for (int j = i + 1; j < params.size(); j++) {
          // Add the pair to the set
          String pair =
              String.format(
                  "%s:%s:%s:%s",
                  params.get(i).getName(),
                  combination.getValue(i).getName(),
                  params.get(j).getName(),
                  combination.getValue(j).getName());
          pairs.add(pair);
        }
      }
    }
    return pairs;
  }

  private void assertCompatibilityRulesRespected(CombinationTable results, String message) {
    // Check Safari-macOS rule
    for (Combination combination : results.combinations()) {
      if (combination.getValue(0).getName().equals("Safari")) {
        assertEquals(
            combination.getValue(1).getName(),
            "macOS",
            message + " - Safari should only be paired with macOS");
      }
    }
  }
}
