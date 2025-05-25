package com.functest.jpwise.algo;

import com.functest.jpwise.core.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.Assert.*;

/**
 * Tests that compare the results of the old and new pairwise algorithms
 * to ensure they generate equivalent test cases.
 */
public class PairwiseAlgorithmComparisonTest {
    private TestInput browserInput;
    private TestInput simpleInput;
    private TestInput complexInput;

    @BeforeMethod
    public void setUp() {
        // Set up browser testing input with compatibility rules
        List<CompatibilityPredicate> browserOsRules = Arrays.asList(
            (v1, v2) -> {
                if (v1.getName().equals("Safari")) {
                    return v2.getName().equals("macOS");
                }
                if (v2.getName().equals("Safari")) {
                    return v1.getName().equals("macOS");
                }
                return true;
            }
        );

        TestParameter browser = new TestParameter("browser", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Chrome", "116.0.5845.96"),
            SimpleValue.of("Firefox", "118.0.2"),
            SimpleValue.of("Safari", "17.0")
        ), browserOsRules);
        
        TestParameter os = new TestParameter("os", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Windows", "10.0.19045"),
            SimpleValue.of("macOS", "14.1"),
            SimpleValue.of("Linux", "6.5.7")
        ));
        
        TestParameter resolution = new TestParameter("resolution", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("HD", "1920x1080"),
            SimpleValue.of("QHD", "2560x1440")
        ));
        
        browserInput = new TestInput();
        browserInput.add(browser);
        browserInput.add(os);
        browserInput.add(resolution);

        // Set up simple input for basic comparison
        TestParameter color = new TestParameter("color", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Primary", "red"),
            SimpleValue.of("Secondary", "blue")
        ));
        TestParameter size = new TestParameter("size", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Compact", "small"),
            SimpleValue.of("Extended", "large")
        ));
        simpleInput = new TestInput();
        simpleInput.add(color);
        simpleInput.add(size);

        // Set up complex input with more parameters and values
        TestParameter userRole = new TestParameter("userRole", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Admin", "FULL_ACCESS"),
            SimpleValue.of("Manager", "DEPARTMENT_ACCESS"),
            SimpleValue.of("User", "BASIC_ACCESS")
        ));
        
        TestParameter department = new TestParameter("department", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Sales", "REVENUE_CRITICAL"),
            SimpleValue.of("Engineering", "TECHNICAL"),
            SimpleValue.of("Support", "CUSTOMER_FACING")
        ));
        
        TestParameter accessLevel = new TestParameter("accessLevel", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Full", "ALL_OPERATIONS"),
            SimpleValue.of("ReadWrite", "MODIFY_CONTENT"),
            SimpleValue.of("ReadOnly", "VIEW_CONTENT")
        ));

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
        assertCompatibilityRulesRespected(oldResults, "Old algorithm should respect compatibility rules");
        assertCompatibilityRulesRespected(newResults, "New algorithm should respect compatibility rules");
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

    private void assertEquivalentCoverage(CombinationTable oldResults, CombinationTable newResults, TestInput input) {
        // Get all parameter pairs covered by each algorithm
        Set<String> oldPairs = extractAllPairs(oldResults, input);
        Set<String> newPairs = extractAllPairs(newResults, input);

        // Compare coverage
        assertEquals(oldPairs, newPairs, 
            "Both algorithms should cover the same parameter value pairs");
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
                    String pair = String.format("%s:%s:%s:%s",
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
                assertEquals(combination.getValue(1).getName(), "macOS",
                    message + " - Safari should only be paired with macOS");
            }
        }
    }
} 