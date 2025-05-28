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
 * The main test case generator class that implements test combination generation algorithms. This
 * class serves as the primary interface for generating test combinations using either pairwise or
 * full combinatorial approaches.
 *
 * <p>The generator works with a {@link TestInput} object that defines the test parameters and their
 * possible values. It uses a specified {@link GenerationAlgorithm} to generate the test
 * combinations.
 *
 * <p>Example usage:
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

  private final TestInput effectiveInput; // Renamed from 'input' to 'effectiveInput'
  private final List<TestParameter> parameters; // Derived from the effective input
  private CombinationTable result; // Holds the generated combinations
  private final RulePreprocessor rulePreprocessor;
  private final boolean enableRulePreprocessing;

  /**
   * Initializes a new TestGenerator with the provided test input. Rule preprocessing is disabled by
   * default.
   *
   * @param initialInput The initial TestInput.
   */
  public TestGenerator(TestInput initialInput) {
    this(initialInput, false); // Default to disabling preprocessing
  }

  /**
   * Initializes a new TestGenerator with the provided test input and explicit control over rule
   * preprocessing.
   *
   * @param initialInput The initial TestInput.
   * @param enableRulePreprocessing If true, rules will be preprocessed (e.g., to add symmetric
   *     rules). If false, the input is used as-is by the generation algorithm.
   */
  public TestGenerator(TestInput initialInput, boolean enableRulePreprocessing) {
    Objects.requireNonNull(initialInput, "Initial TestInput cannot be null");
    this.enableRulePreprocessing = enableRulePreprocessing;
    this.rulePreprocessor = new RulePreprocessor(); // Instantiated regardless, used if flag is true

    if (this.enableRulePreprocessing) {
      logger.info("Rule preprocessing is ENABLED. Preprocessing initial input.");
      this.effectiveInput = this.rulePreprocessor.preprocess(initialInput);
    } else {
      logger.info("Rule preprocessing is DISABLED. Using initial input as-is.");
      this.effectiveInput = initialInput;
    }

    this.parameters = this.effectiveInput.getTestParameters();
    if (this.parameters == null || this.parameters.isEmpty()) { // Added null check for parameters
      throw new IllegalArgumentException(
          "Test input (after potential preprocessing) must contain at least one parameter.");
    }
    this.result = new CombinationTable(new ArrayList<>()); // Initialize result table
  }

  public TestInput getInput() {
    return effectiveInput; // Return the (potentially) processed input
  }

  /**
   * Generates test combinations using the specified algorithm. The algorithm itself should be
   * configured with any specific parameters (like N-wise value or limit) via its constructor.
   *
   * @param algorithm The generation algorithm to use
   * @return A table of generated combinations
   */
  public CombinationTable generate(GenerationAlgorithm algorithm) {
    logger.info("Generating combinations with algorithm: {}", algorithm.getClass().getSimpleName());
    // The algorithm now receives the effectiveInput, which may or may not have been preprocessed.
    this.result = algorithm.generate(this.effectiveInput);
    logger.info("Generated {} combinations", this.result.size());
    return this.result;
  }

  /**
   * Calculates the total number of possible combinations without considering any rules. This is the
   * product of the number of partitions for each parameter.
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
