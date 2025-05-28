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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
 * CombinationTable results = generator.generate(new PairwiseAlgorithm());
 * </pre>
 *
 * @author panwei
 * @see TestInput
 * @see GenerationAlgorithm
 * @see CombinationTable
 */
public class TestGenerator {
  private static final Logger logger = LoggerFactory.getLogger(TestGenerator.class);

  private final TestInput input; // The effective TestInput after preprocessing
  private final List<TestParameter> parameters; // Derived from the effective input
  private CombinationTable result; // Holds the generated combinations
  private final RulePreprocessor rulePreprocessor;

  /**
   * Initializes a new TestGenerator with the provided test input.
   * A new preprocessed TestInput will be used internally.
   *
   * @param initialInput The initial TestInput.
   */
  public TestGenerator(TestInput initialInput) {
    Objects.requireNonNull(initialInput, "Initial TestInput cannot be null");

    logger.info("Rule preprocessing is enabled by default. Preprocessing initial input.");
    this.rulePreprocessor = new RulePreprocessor();
    this.input = this.rulePreprocessor.preprocess(initialInput);

    this.parameters = this.input.getTestParameters();
    if (this.parameters.isEmpty()) {
      throw new IllegalArgumentException("Test input must contain at least one parameter.");
    }
    this.result = new CombinationTable(new ArrayList<>()); // Initialize result table
  }

  public TestInput getInput() {
    return input;
  }

  /**
   * Generates test combinations using the specified algorithm and N-wise value
   * (or limit).
   *
   * @param algorithm    The generation algorithm to use
   * @param nWiseOrLimit For N-wise algorithms, this is N. For combinatorial, this
   *                     is the limit.
   * @return A table of generated combinations
   */
  public CombinationTable generate(GenerationAlgorithm algorithm, int nWiseOrLimit) {
    logger.info(
        "Generating combinations with algorithm: {}, N-wise/Limit: {}",
        algorithm.getClass().getSimpleName(),
        nWiseOrLimit);
    this.result = algorithm.generate(this.input, nWiseOrLimit);
    logger.info("Generated {} combinations", this.result.size());
    return this.result;
  }

  /**
   * Calculates the total number of possible combinations without considering any
   * rules.
   * This is the product of the number of partitions for each parameter.
   *
   * @return The total number of possible combinations (span of the input space).
   */
  public int span() {
    if (parameters.isEmpty()) {
      return 0;
    }
    int totalCombinations = 1;
    for (TestParameter parameter : parameters) {
      totalCombinations *= parameter.getPartitions().size();
    }
    return totalCombinations;
  }
}
