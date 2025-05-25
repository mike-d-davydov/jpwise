/**
 * Copyright (c) 2010  Ng Pan Wei, 2013 Mikhail Davydov
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.functest.jpwise.core;


/**
 * The main test case generator class that implements test combination generation algorithms.
 * This class serves as the primary interface for generating test combinations using either
 * pairwise or full combinatorial approaches.
 * 
 * <p>The generator works with a {@link TestInput} object that defines the test parameters
 * and their possible values. It uses a specified {@link GenerationAlgorithm} to generate
 * the test combinations.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * TestInput input = new TestInput();
 * input.add(new TestParameter("browser", Arrays.asList(
 *     SimpleValue.of("Chrome"),
 *     SimpleValue.of("Firefox")
 * )));
 * 
 * TestGenerator generator = new TestGenerator(input);
 * generator.generate(new PairwiseAlgorithm());
 * CombinationTable results = generator.result();
 * </pre>
 *
 * @author panwei
 * @see TestInput
 * @see GenerationAlgorithm
 * @see CombinationTable
 */
public class TestGenerator {
    /**
     * The table containing all generated test combinations.
     */
    protected CombinationTable _result;
    
    /**
     * The test input configuration containing parameters and their possible values.
     */
    private TestInput _testInput;

    /**
     * Creates a new test generator for the specified test input configuration.
     *
     * @param theTestInput The test input configuration containing parameters and their values
     */
    public TestGenerator(TestInput theTestInput) {
        _testInput = theTestInput;
        _result = new CombinationTable();
    }

    /**
     * Generates test combinations using the specified algorithm and N-wise coverage.
     * For pairwise testing, use N=2. For higher-order combinations, use larger values.
     * For full combinatorial testing, use a large value like 99.
     *
     * @param algorithm The generation algorithm to use (e.g., PairwiseAlgorithm or CombinatorialAlgorithm)
     * @param nwise The degree of combinations (2 for pairwise, higher for more combinations)
     */
    public void generate(GenerationAlgorithm algorithm, int nwise) {
        algorithm.generate(this, nwise);
    }

    /**
     * Generates test combinations using the specified algorithm with pairwise (2-wise) coverage.
     * This is a convenience method equivalent to calling generate(algorithm, 2).
     *
     * @param algorithm The generation algorithm to use
     */
    public void generate(GenerationAlgorithm algorithm) {
        algorithm.generate(this, 2);
    }

    /**
     * Gets the test input configuration.
     *
     * @return The test input configuration
     */
    public TestInput input() {
        return _testInput;
    }

    /**
     * Gets the table containing all generated test combinations.
     *
     * @return The combination table with generated test cases
     */
    public CombinationTable result() {
        return _result;
    }

    /**
     * Computes the total number of possible parameter value pairs in the test input.
     * This represents the theoretical maximum number of combinations that could be generated.
     *
     * @return The total number of possible parameter value pairs
     */
    public int span() {
        int size = 0;
        for (int i = 0; i < _testInput.size(); i++) {
            for (int j = i + 1; j < _testInput.size(); j++) {
                size += _testInput.get(i).getValues().size()
                        * _testInput.get(j).getValues().size();
            }
        }
        return size;
    }
}
