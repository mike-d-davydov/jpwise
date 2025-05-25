package com.functest.jpwise.algo;

import com.functest.jpwise.core.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.Assert.*;

/**
 * Tests for pairwise and combinatorial test case generation algorithms.
 * Validates that:
 * 1. Pairwise algorithm generates all possible pairs of equivalence classes
 * 2. Combinatorial algorithm generates all possible combinations of equivalence classes
 * 3. Both algorithms respect compatibility rules between equivalence classes
 */
public class AlgorithmTest {
    private TestInput simpleInput;
    private TestInput browserInput;
    private TestParameter browser;
    private TestParameter os;
    private TestParameter resolution;

    @BeforeMethod
    public void setUp() {
        // Set up simple test input with color and size equivalence classes
        TestParameter color = new TestParameter("color", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Primary", "red"),      // Primary color class, red value
            SimpleValue.of("Secondary", "blue")    // Secondary color class, blue value
        ));
        TestParameter size = new TestParameter("size", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Compact", "small"),    // Compact size class, small value
            SimpleValue.of("Extended", "large")    // Extended size class, large value
        ));
        simpleInput = new TestInput();
        simpleInput.add(color);
        simpleInput.add(size);

        // Set up browser testing input with compatibility rules between browser and OS families
        List<CompatibilityPredicate> browserOsRules = Arrays.asList(
            (v1, v2) -> {
                // Only apply rules if we're dealing with browser and OS parameters
                if (!v1.getParentParameter().getName().equals("browser") ||
                    !v2.getParentParameter().getName().equals("os")) {
                    return true;
                }

                String browserFamily = v1.getName();
                String osFamily = v2.getName();

                // Safari only works with macOS
                if (browserFamily.equals("Safari")) {
                    return osFamily.equals("macOS");
                }
                if (osFamily.equals("Safari")) {
                    return browserFamily.equals("macOS");
                }
                return true;
            }
        );

        // Browser families with specific versions
        browser = new TestParameter("browser", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Chrome", "116.0.5845.96"),   // Chrome family with specific version
            SimpleValue.of("Firefox", "118.0.2"),        // Firefox family with specific version
            SimpleValue.of("Safari", "17.0")             // Safari family with specific version
        ), browserOsRules);
        
        // OS families with specific builds/versions
        os = new TestParameter("os", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Windows", "10.0.19045"),     // Windows family with specific build
            SimpleValue.of("macOS", "14.1"),             // macOS family with specific version
            SimpleValue.of("Linux", "6.5.7")             // Linux family with specific version
        ));
        
        // Resolution categories with specific dimensions
        resolution = new TestParameter("resolution", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("HD", "1920x1080"),           // HD class with specific resolution
            SimpleValue.of("QHD", "2560x1440")          // QHD class with specific resolution
        ));
        
        browserInput = new TestInput();
        browserInput.add(browser);
        browserInput.add(os);
        browserInput.add(resolution);
    }

    @Test
    public void testPairwiseBaselineComparison() {
        // Test pairwise generation with simple equivalence classes
        TestGenerator generator = new TestGenerator(simpleInput);
        generator.generate(new PairwiseAlgorithm());
        CombinationTable result = generator.result();

        // Expected pairs of equivalence classes:
        // Primary-Compact, Primary-Extended, Secondary-Compact, Secondary-Extended
        assertEquals(result.size(), 4, 
            "Should generate minimum number of test cases to cover all equivalence class pairs");

        // Validate all equivalence class pairs are present
        Set<String> expectedPairs = new HashSet<>(Arrays.asList(
            "Primary:Compact", "Primary:Extended", "Secondary:Compact", "Secondary:Extended"
        ));
        Set<String> actualPairs = extractEquivalenceClassPairs(result);
        assertEquals(actualPairs, expectedPairs, 
            "Should generate all expected equivalence class pairs");
    }

    @Test
    public void testPairwiseComprehensiveCoverage() {
        TestGenerator generator = new TestGenerator(browserInput);
        generator.generate(new PairwiseAlgorithm());
        CombinationTable result = generator.result();

        // Validate all parameter equivalence class pairs are covered
        assertTrue(validatePairwiseCoverage(result), 
            "All possible pairs of equivalence classes should be covered");

        // Calculate total possible valid pairs considering compatibility rules
        int totalPairs = calculateTotalValidPairs();
        int testCases = result.size();
        
        assertTrue(testCases <= totalPairs, 
            String.format("Number of test cases (%d) should be less than or equal to total valid pairs (%d)", 
                testCases, totalPairs));
    }

    @Test
    public void testPairwiseWithCompatibilityRules() {
        TestGenerator generator = new TestGenerator(browserInput);
        generator.generate(new PairwiseAlgorithm());
        CombinationTable result = generator.result();

        // Verify Safari-macOS compatibility rule is respected
        for (Combination combination : result.combinations()) {
            if (combination.getValue(0).getName().equals("Safari")) {
                assertEquals(combination.getValue(1).getName(), "macOS",
                    "Safari browser family should only be paired with macOS family");
            }
        }

        // Verify all valid equivalence class pairs are covered
        assertTrue(validatePairwiseCoverage(result),
            "All valid equivalence class pairs should be covered");
    }

    @Test
    public void testCombinatorialComplete() {
        TestGenerator generator = new TestGenerator(browserInput);
        generator.generate(new CombinatorialAlgorithm());
        CombinationTable result = generator.result();

        // Calculate expected number of valid combinations of equivalence classes
        int expectedSize = calculateValidCombinationsCount();
        assertEquals(result.size(), expectedSize,
            "Should generate all possible combinations of equivalence classes");

        // Verify each combination is unique
        Set<String> combinations = new HashSet<>();
        for (Combination combination : result.combinations()) {
            String key = combination.getKey();
            assertTrue(combinations.add(key),
                "Each combination of equivalence classes should be unique");
        }
    }

    @Test
    public void testCombinatorialWithCompatibilityRules() {
        TestGenerator generator = new TestGenerator(browserInput);
        generator.generate(new CombinatorialAlgorithm());
        CombinationTable result = generator.result();

        // Calculate expected number of valid combinations
        int expectedSize = calculateValidCombinationsCount();
        assertEquals(result.size(), expectedSize,
            "Should generate all valid combinations of equivalence classes");

        // Verify Safari-macOS compatibility rule is respected
        for (Combination combination : result.combinations()) {
            if (combination.getValue(0).getName().equals("Safari")) {
                assertEquals(combination.getValue(1).getName(), "macOS",
                    "Safari browser family should only be combined with macOS family");
            }
        }

        // Verify all valid combinations are present
        Set<String> expectedCombinations = generateExpectedCombinations();
        Set<String> actualCombinations = new HashSet<>();
        for (Combination combination : result.combinations()) {
            actualCombinations.add(String.format("%s:%s:%s",
                combination.getValue(0).getName(),
                combination.getValue(1).getName(),
                combination.getValue(2).getName()));
        }
        assertEquals(actualCombinations, expectedCombinations,
            "All valid combinations of equivalence classes should be present");
    }

    // Helper methods

    private Set<String> extractEquivalenceClassPairs(CombinationTable table) {
        Set<String> pairs = new HashSet<>();
        for (Combination combination : table.combinations()) {
            pairs.add(combination.getValue(0).getName() + ":" + 
                     combination.getValue(1).getName());
        }
        return pairs;
    }

    private boolean validatePairwiseCoverage(CombinationTable table) {
        Map<String, Set<String>> coveredPairs = new HashMap<>();
        
        // Initialize coverage tracking for each parameter pair
        List<TestParameter> params = browserInput.getTestParameters();
        for (int i = 0; i < params.size(); i++) {
            for (int j = i + 1; j < params.size(); j++) {
                String key = params.get(i).getName() + ":" + params.get(j).getName();
                coveredPairs.put(key, new HashSet<>());
            }
        }

        // Track all equivalence class pairs in generated combinations
        for (Combination combination : table.combinations()) {
            for (int i = 0; i < params.size(); i++) {
                for (int j = i + 1; j < params.size(); j++) {
                    String paramKey = params.get(i).getName() + ":" + params.get(j).getName();
                    String valueKey = combination.getValue(i).getName() + ":" + 
                                    combination.getValue(j).getName();
                    coveredPairs.get(paramKey).add(valueKey);
                }
            }
        }

        // Verify all valid equivalence class pairs are covered
        for (int i = 0; i < params.size(); i++) {
            for (int j = i + 1; j < params.size(); j++) {
                String paramKey = params.get(i).getName() + ":" + params.get(j).getName();
                int expectedPairs = calculateValidPairsForParameters(params.get(i), params.get(j));
                if (coveredPairs.get(paramKey).size() < expectedPairs) {
                    return false;
                }
            }
        }
        return true;
    }

    private int calculateValidPairsForParameters(TestParameter param1, TestParameter param2) {
        int count = 0;
        for (ParameterValue<?> v1 : param1.getValues()) {
            for (ParameterValue<?> v2 : param2.getValues()) {
                if (param1.areCompatible(v1, v2)) {
                    count++;
                }
            }
        }
        return count;
    }

    private int calculateTotalValidPairs() {
        List<TestParameter> params = browserInput.getTestParameters();
        int totalPairs = 0;
        for (int i = 0; i < params.size(); i++) {
            for (int j = i + 1; j < params.size(); j++) {
                totalPairs += calculateValidPairsForParameters(params.get(i), params.get(j));
            }
        }
        return totalPairs;
    }

    private int calculateValidCombinationsCount() {
        // For browser compatibility rules:
        // Chrome works with all OS (3) * all resolutions (2) = 6 combinations
        // Firefox works with all OS (3) * all resolutions (2) = 6 combinations
        // Safari only works with macOS (1) * all resolutions (2) = 2 combinations
        return 14; // Total valid combinations
    }

    private Set<String> generateExpectedCombinations() {
        Set<String> combinations = new HashSet<>();
        
        // Chrome combinations
        combinations.add("Chrome:Windows:HD");
        combinations.add("Chrome:Windows:QHD");
        combinations.add("Chrome:macOS:HD");
        combinations.add("Chrome:macOS:QHD");
        combinations.add("Chrome:Linux:HD");
        combinations.add("Chrome:Linux:QHD");
        
        // Firefox combinations
        combinations.add("Firefox:Windows:HD");
        combinations.add("Firefox:Windows:QHD");
        combinations.add("Firefox:macOS:HD");
        combinations.add("Firefox:macOS:QHD");
        combinations.add("Firefox:Linux:HD");
        combinations.add("Firefox:Linux:QHD");
        
        // Safari combinations (only with macOS)
        combinations.add("Safari:macOS:HD");
        combinations.add("Safari:macOS:QHD");
        
        return combinations;
    }
} 