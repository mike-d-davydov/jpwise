package com.functest.jpwise.algo;

import com.functest.jpwise.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements a full combinatorial test case generation algorithm.
 * This algorithm generates all possible combinations of parameter values
 * while respecting compatibility rules between values.
 * 
 * <p>Unlike the pairwise algorithm that only covers pairs of values,
 * this algorithm generates every possible combination, which provides
 * complete coverage but results in a much larger number of test cases.</p>
 * 
 * <p>The number of test cases grows exponentially with the number of
 * parameters and their values. For example, with 3 parameters having
 * 3 values each, this could generate up to 27 test cases (3^3).</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * TestInput input = new TestInput();
 * input.add(new TestParameter("browser", browserValues));
 * input.add(new TestParameter("os", osValues));
 * 
 * TestGenerator generator = new TestGenerator(input);
 * generator.generate(new CombinatorialAlgorithm(), 99); // 99 indicates full coverage
 * CombinationTable results = generator.result();
 * </pre>
 * 
 * <p>Use this algorithm when:</p>
 * <ul>
 *   <li>Complete test coverage is required</li>
 *   <li>The number of parameters and values is small</li>
 *   <li>You need to verify all possible interactions</li>
 * </ul>
 *
 * @author DavydovMD
 * @see GenerationAlgorithm
 * @see TestGenerator
 */
public class CombinatorialAlgorithm extends GenerationAlgorithm {

    /**
     * Creates a new combinatorial algorithm instance.
     */
    public CombinatorialAlgorithm() {
        super();
    }

    /**
     * Gets the index of a parameter in the test input based on one of its values.
     *
     * @param value A parameter value
     * @return The index of the parameter that contains this value
     */
    private int getParameterIndex(ParameterValue value) {
        return input().getTestParameters().indexOf(value.getParentParameter());
    }

    /**
     * Generates all possible combinations of parameter values.
     * The algorithm builds combinations incrementally, starting with an empty
     * combination and adding one parameter's values at a time.
     *
     * @param testGenerator The test generator containing input parameters
     * @param nwise The degree of combinations (ignored, always generates all combinations)
     */
    @Override
    public void generate(TestGenerator testGenerator, int nwise) {
        pwGenerator = testGenerator;

        List<Combination> possiblyIncompleteResult, moreCompleteResult = new ArrayList<>();
        moreCompleteResult.add(new Combination(input().size()));

        // For each parameter
        for (TestParameter parameter : input().getTestParameters()) {
            possiblyIncompleteResult = new ArrayList<>(moreCompleteResult);
            // For each existing partial combination
            for (Combination possiblyIncompleteCombination : possiblyIncompleteResult)
                // For each value of the current parameter
                for (ParameterValue paramValue : parameter.getValues()) {
                    moreCompleteResult.remove(possiblyIncompleteCombination);
                    final Combination moreCompleteCombination = new Combination(input().size()).merge(possiblyIncompleteCombination);
                    moreCompleteCombination.setValue(getParameterIndex(paramValue), paramValue);
                    moreCompleteResult.add(moreCompleteCombination);
                }
        }

        // Remove combinations that violate compatibility rules
        moreCompleteResult.removeIf(c -> !c.checkNoConflicts(this));

        // Add valid combinations to the result
        for (Combination testCase : moreCompleteResult) {
            addToResult(testCase);
        }
    }
}
