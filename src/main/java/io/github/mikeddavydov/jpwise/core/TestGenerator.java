/**
 * Copyright (c) 2010 Ng Pan Wei, 2013 Mikhail Davydov
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mikeddavydov.jpwise.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main test case generator class that implements test combination
 * generation algorithms. This
 * class serves as the primary interface for generating test combinations using
 * either pairwise or
 * full combinatorial approaches.
 *
 * <p>
 * The generator works with a {@link TestInput} object that defines the test
 * parameters and their
 * possible values. It uses a specified {@link GenerationAlgorithm} to generate
 * the test
 * combinations.
 *
 * <p>
 * Example usage:
 *
 * <pre>
 * TestInput input = new TestInput();
 * input.add(new TestParameter("browser", Arrays.asList(
 *     SimpleValue.of("Chrome"),
 *     SimpleValue.of("Firefox"))));
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
  private static final Logger logger = LoggerFactory.getLogger(TestGenerator.class);

  /** The table containing all generated test combinations. */
  private CombinationTable result;

  /**
   * The test input configuration containing parameters and their possible values.
   */
  private TestInput testInput;

  /**
   * Creates a new test generator for the specified test input configuration.
   *
   * @param input The test input configuration containing parameters and their
   *              values
   */
  public TestGenerator(TestInput input) {
    this.testInput = input;
    this.result = new CombinationTable();
    logger.debug("Created new TestGenerator with {} parameters", input.getTestParameters().size());
  }

  /**
   * Generates test combinations using the specified algorithm and N-wise
   * coverage. For pairwise
   * testing, use N=2. For higher-order combinations, use larger values. For full
   * combinatorial
   * testing, use a large value like 99.
   *
   * @param algorithm The generation algorithm to use (e.g., PairwiseAlgorithm or
   *                  CombinatorialAlgorithm)
   * @param limit     The degree of combinations (2 for pairwise, higher for more
   *                  combinations)
   */
  public void generate(GenerationAlgorithm algorithm, int limit) {
    logger.info("Starting test generation with {} algorithm and limit {}",
        algorithm.getClass().getSimpleName(), limit);
    algorithm.generate(this, limit);
    logger.info("Generated {} test combinations", result.size());
  }

  /**
   * Generates test combinations using the specified algorithm with pairwise
   * (2-wise) coverage. This
   * is a convenience method equivalent to calling generate(algorithm, 2).
   *
   * @param algorithm The generation algorithm to use
   */
  public void generate(GenerationAlgorithm algorithm) {
    logger.debug("Starting pairwise test generation with {} algorithm",
        algorithm.getClass().getSimpleName());
    algorithm.generate(this, 2);
  }

  /**
   * Gets the test input configuration.
   *
   * @return The test input configuration
   */
  public TestInput input() {
    return testInput;
  }

  /**
   * Gets the table containing all generated test combinations.
   *
   * @return The combination table with generated test cases
   */
  public CombinationTable result() {
    return result;
  }

  /**
   * Computes the total number of possible parameter value pairs in the test
   * input. This represents
   * the theoretical maximum number of combinations that could be generated.
   *
   * @return The total number of possible parameter value pairs
   */
  public int span() {
    int size = 0;
    for (int i = 0; i < testInput.size(); i++) {
      for (int j = i + 1; j < testInput.size(); j++) {
        size += testInput.get(i).getPartitions().size() * testInput.get(j).getPartitions().size();
      }
    }
    return size;
  }
}
