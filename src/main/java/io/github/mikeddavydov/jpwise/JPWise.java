package io.github.mikeddavydov.jpwise;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mikeddavydov.jpwise.algo.CombinatorialAlgorithm;
import io.github.mikeddavydov.jpwise.algo.LegacyPairwiseAlgorithm;
import io.github.mikeddavydov.jpwise.algo.PairwiseAlgorithm;
import io.github.mikeddavydov.jpwise.core.CombinationTable;
import io.github.mikeddavydov.jpwise.core.CompatibilityPredicate;
import io.github.mikeddavydov.jpwise.core.EquivalencePartition;
import io.github.mikeddavydov.jpwise.core.GenerationAlgorithm;
import io.github.mikeddavydov.jpwise.core.TestGenerator;
import io.github.mikeddavydov.jpwise.core.TestInput;
import io.github.mikeddavydov.jpwise.core.TestParameter;

/**
 * A facade class providing a simplified API for the JPWise test generation
 * framework. This class
 * offers static methods for building test inputs and generating test
 * combinations using either
 * pairwise or combinatorial algorithms.
 *
 * <p>
 * Example usage:
 *
 * <pre>
 * // Direct generation
 * CombinationTable results = JPWise.generatePairwise(browser, os);
 *
 * // Using the fluent builder API
 * CombinationTable results = JPWise.builder()
 *     .parameter(browser)
 *     .parameter(os)
 *     .generatePairwise();
 *
 * // Or building input separately
 * TestInput input = JPWise.builder()
 *     .parameter(browser)
 *     .parameter(os)
 *     .build();
 * CombinationTable results = JPWise.generatePairwise(input);
 * </pre>
 */
public final class JPWise {
  private static final Logger logger = LoggerFactory.getLogger(JPWise.class);

  private JPWise() {
    // Prevent instantiation
  }

  /**
   * Creates a new InputBuilder for fluently constructing test inputs. This is an
   * alias for {@link
   * #builder()}.
   *
   * @return A new InputBuilder instance
   */
  public static InputBuilder input() {
    return builder();
  }

  /**
   * Creates a new InputBuilder for fluently constructing test inputs.
   *
   * @return A new InputBuilder instance
   */
  public static InputBuilder builder() {
    return new InputBuilder();
  }

  /**
   * Creates a new InputBuilder pre-populated with the given parameters.
   *
   * @param parameters The parameters to add
   * @return A new InputBuilder instance with parameters added
   * @throws NullPointerException if parameters array or any element is null
   */
  public static InputBuilder withParameters(TestParameter... parameters) {
    return builder().parameters(parameters);
  }

  /**
   * Creates a new InputBuilder pre-populated with the given parameters.
   *
   * @param parameters The parameters to add
   * @return A new InputBuilder instance with parameters added
   * @throws NullPointerException if parameters collection or any element is null
   */
  public static InputBuilder withParameters(Collection<TestParameter> parameters) {
    return builder().parameters(parameters);
  }

  /**
   * Generates test combinations using the pairwise algorithm with default
   * settings.
   *
   * @param parameters The test parameters
   * @return A table of generated test combinations
   * @throws NullPointerException if parameters array or any element is null
   */
  public static CombinationTable generatePairwise(TestParameter... parameters) {
    return withParameters(parameters).generatePairwise();
  }

  /**
   * Generates test combinations using the pairwise algorithm with default
   * settings.
   *
   * @param parameters The test parameters
   * @return A table of generated test combinations
   * @throws NullPointerException if parameters collection or any element is null
   */
  public static CombinationTable generatePairwise(Collection<TestParameter> parameters) {
    return withParameters(parameters).generatePairwise();
  }

  /**
   * Generates test combinations using a configured pairwise algorithm.
   *
   * @param algorithm  The configured pairwise algorithm instance
   * @param parameters The test parameters
   * @return A table of generated test combinations
   * @throws NullPointerException if algorithm, parameters array, or any parameter
   *                              is null
   */
  public static CombinationTable generatePairwise(
      PairwiseAlgorithm algorithm, TestParameter... parameters) {
    Objects.requireNonNull(algorithm, "algorithm must not be null");
    return generatePairwise(withParameters(parameters).build(), algorithm);
  }

  /**
   * Generates test combinations using a configured pairwise algorithm.
   *
   * @param algorithm  The configured pairwise algorithm instance
   * @param parameters The test parameters
   * @return A table of generated test combinations
   * @throws NullPointerException if algorithm, parameters collection, or any
   *                              parameter is null
   */
  public static CombinationTable generatePairwise(
      PairwiseAlgorithm algorithm, Collection<TestParameter> parameters) {
    Objects.requireNonNull(algorithm, "algorithm must not be null");
    return generatePairwise(withParameters(parameters).build(), algorithm);
  }

  /**
   * Generates test combinations using the pairwise algorithm with default
   * settings.
   *
   * @param input The test input configuration
   * @return A table of generated test combinations
   * @throws NullPointerException if input is null
   */
  public static CombinationTable generatePairwise(TestInput input) {
    return executeGeneration(input, new LegacyPairwiseAlgorithm(), 2);
  }

  /**
   * Generates test combinations using a configured pairwise algorithm.
   *
   * @param input     The test input configuration
   * @param algorithm The configured pairwise algorithm instance
   * @return A table of generated test combinations
   * @throws NullPointerException if input or algorithm is null
   */
  public static CombinationTable generatePairwise(TestInput input, PairwiseAlgorithm algorithm) {
    Objects.requireNonNull(input, "input must not be null");
    Objects.requireNonNull(algorithm, "algorithm must not be null");
    return executeGeneration(input, algorithm, 2);
  }

  /**
   * Generates test combinations using the combinatorial algorithm with default
   * settings.
   *
   * @param parameters The test parameters
   * @return A table of generated test combinations
   * @throws NullPointerException if parameters array or any element is null
   */
  public static CombinationTable generateCombinatorial(TestParameter... parameters) {
    return withParameters(parameters).generateCombinatorial();
  }

  /**
   * Generates test combinations using the combinatorial algorithm with default
   * settings.
   *
   * @param parameters The test parameters
   * @param limit      The maximum number of combinations to generate
   * @return A table of generated test combinations
   * @throws NullPointerException     if parameters collection or any element is
   *                                  null
   * @throws IllegalArgumentException if limit is less than 1
   */
  public static CombinationTable generateCombinatorial(
      Collection<TestParameter> parameters, int limit) {
    return withParameters(parameters).generateCombinatorial(limit);
  }

  /**
   * Generates test combinations using a configured combinatorial algorithm.
   *
   * @param algorithm  The configured combinatorial algorithm instance
   * @param limit      The maximum number of combinations to generate
   * @param parameters The test parameters
   * @return A table of generated test combinations
   * @throws NullPointerException     if algorithm, parameters array, or any
   *                                  parameter is null
   * @throws IllegalArgumentException if limit is less than 1
   */
  public static CombinationTable generateCombinatorial(
      CombinatorialAlgorithm algorithm, int limit, TestParameter... parameters) {
    Objects.requireNonNull(algorithm, "algorithm must not be null");
    return generateCombinatorial(withParameters(parameters).build(), algorithm, limit);
  }

  /**
   * Generates test combinations using the combinatorial algorithm with default
   * settings.
   *
   * @param input The test input configuration
   * @return A table of generated test combinations
   * @throws NullPointerException if input is null
   */
  public static CombinationTable generateCombinatorial(TestInput input) {
    return generateCombinatorial(input, new CombinatorialAlgorithm());
  }

  /**
   * Generates test combinations using a configured combinatorial algorithm.
   *
   * @param input     The test input configuration
   * @param algorithm The configured combinatorial algorithm instance
   * @return A table of generated test combinations
   * @throws NullPointerException if input or algorithm is null
   */
  public static CombinationTable generateCombinatorial(
      TestInput input, CombinatorialAlgorithm algorithm) {
    return generateCombinatorial(input, algorithm, 99);
  }

  /**
   * Generates test combinations using a configured combinatorial algorithm with a
   * limit.
   *
   * @param input     The test input configuration
   * @param algorithm The configured combinatorial algorithm instance
   * @param limit     The maximum number of combinations to generate
   * @return A table of generated test combinations
   * @throws NullPointerException     if input or algorithm is null
   * @throws IllegalArgumentException if limit is less than 1
   */
  public static CombinationTable generateCombinatorial(
      TestInput input, CombinatorialAlgorithm algorithm, Integer limit) {
    Objects.requireNonNull(input, "input must not be null");
    Objects.requireNonNull(algorithm, "algorithm must not be null");
    if (limit < 1) {
      throw new IllegalArgumentException("limit must be positive");
    }
    CombinatorialAlgorithm effectiveAlgorithm = new CombinatorialAlgorithm(limit);
    return executeGeneration(input, effectiveAlgorithm, limit);
  }

  private static CombinationTable executeGeneration(
      TestInput input, GenerationAlgorithm algorithm, int nWiseOrLimit) {
    logger.debug("JPWise.executeGeneration called with algorithm: {}, nWiseOrLimit: {}",
        algorithm.getClass().getSimpleName(), nWiseOrLimit);
    Objects.requireNonNull(input, "input must not be null");
    Objects.requireNonNull(algorithm, "algorithm must not be null");
    if (nWiseOrLimit < 1) {
      throw new IllegalArgumentException("nWiseOrLimit must generally be positive.");
    }

    TestGenerator generator = new TestGenerator(input);
    logger.debug("JPWise.executeGeneration: TestGenerator created. Calling TestGenerator.generate()...");
    CombinationTable result = generator.generate(algorithm, nWiseOrLimit);
    logger.info("Generated {} test combinations", result.size());
    return result;
  }

  /** A fluent builder for constructing test inputs. */
  public static class InputBuilder {
    private static final Logger logger = LoggerFactory.getLogger(InputBuilder.class);
    private final TestInput testInput;

    private InputBuilder() {
      this.testInput = new TestInput();
      logger.debug("Created new InputBuilder");
    }

    /**
     * Adds a single parameter to the test input.
     *
     * @param parameter The parameter to add
     * @return This builder instance
     * @throws NullPointerException if parameter is null
     */
    public InputBuilder parameter(TestParameter parameter) {
      Objects.requireNonNull(parameter, "parameter must not be null");
      testInput.add(parameter);
      logger.debug("Added parameter: {}", parameter.getName());
      return this;
    }

    /**
     * Creates and adds a parameter with the given name and partitions.
     *
     * @param name       The parameter name
     * @param partitions The parameter's partitions
     * @return This builder instance
     * @throws NullPointerException if name or partitions array is null
     */
    public InputBuilder parameter(String name, EquivalencePartition... partitions) {
      Objects.requireNonNull(name, "name must not be null");
      Objects.requireNonNull(partitions, "partitions must not be null");
      return parameter(new TestParameter(name, Arrays.asList(partitions)));
    }

    /**
     * Creates and adds a parameter with the given name, partitions, and
     * compatibility rules.
     *
     * @param name       The parameter name
     * @param partitions The parameter's partitions
     * @param rules      The parameter's compatibility rules
     * @return This builder instance
     * @throws NullPointerException if any argument is null
     */
    public InputBuilder parameter(
        String name, List<EquivalencePartition> partitions, List<CompatibilityPredicate> rules) {
      Objects.requireNonNull(name, "name must not be null");
      Objects.requireNonNull(partitions, "partitions must not be null");
      Objects.requireNonNull(rules, "rules must not be null");
      return parameter(new TestParameter(name, partitions, rules));
    }

    /**
     * Adds multiple parameters to the test input.
     *
     * @param parameters The parameters to add
     * @return This builder instance
     * @throws NullPointerException if parameters array or any element is null
     */
    public InputBuilder parameters(TestParameter... parameters) {
      Objects.requireNonNull(parameters, "parameters array must not be null");
      logger.debug("Adding {} parameters", parameters.length);
      Arrays.stream(parameters).forEach(this::parameter);
      return this;
    }

    /**
     * Adds a collection of parameters to the test input.
     *
     * @param parameters The parameters to add
     * @return This builder instance
     * @throws NullPointerException if parameters collection or any element is null
     */
    public InputBuilder parameters(Collection<TestParameter> parameters) {
      Objects.requireNonNull(parameters, "parameters collection must not be null");
      logger.debug("Adding {} parameters from collection", parameters.size());
      parameters.forEach(this::parameter);
      return this;
    }

    /**
     * Builds and returns the constructed test input.
     *
     * @return The constructed test input
     */
    public TestInput build() {
      logger.debug("Building TestInput with {} parameters", testInput.getTestParameters().size());
      TestInput copy = new TestInput();
      for (TestParameter param : testInput.getTestParameters()) {
        copy.add(param);
      }
      return copy;
    }

    /**
     * Convenience method to generate test combinations using the pairwise algorithm
     * with default
     * settings.
     *
     * @return A table of generated test combinations
     */
    public CombinationTable generatePairwise() {
      logger.info("InputBuilder.generatePairwise() called. Using PairwiseAlgorithm by default.");
      return JPWise.executeGeneration(testInput, new PairwiseAlgorithm(), 2);
    }

    /**
     * Generates pairwise combinations using the legacy pairwise algorithm with a
     * specific jump value.
     * This method is kept for specific scenarios or comparison and will likely be
     * deprecated.
     *
     * @param jumpValue The jump value for the legacy pairwise algorithm.
     * @return The generated {@link CombinationTable}.
     */
    @Deprecated
    public CombinationTable generateLegacyPairwise(int jumpValue) {
      logger.info(
          "InputBuilder.generateLegacyPairwise(jumpValue={}) called. Using LegacyPairwiseAlgorithm.",
          jumpValue);
      return JPWise.executeGeneration(testInput, new LegacyPairwiseAlgorithm(jumpValue), 2);
    }

    /**
     * Convenience method to generate test combinations using the combinatorial
     * algorithm with
     * default settings.
     *
     * @return A table of generated test combinations
     */
    public CombinationTable generateCombinatorial() {
      return JPWise.generateCombinatorial(testInput);
    }

    /**
     * Convenience method to generate test combinations using the combinatorial
     * algorithm with a
     * limit.
     *
     * @param limit The maximum number of combinations to generate
     * @return A table of generated test combinations
     * @throws IllegalArgumentException if limit is less than 1
     */
    public CombinationTable generateCombinatorial(int limit) {
      return JPWise.generateCombinatorial(
          testInput, new CombinatorialAlgorithm(limit), Integer.valueOf(limit));
    }
  }
}
