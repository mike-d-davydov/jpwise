package io.github.mikeddavydov.jpwise.algo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mikeddavydov.jpwise.core.Combination;
import io.github.mikeddavydov.jpwise.core.EquivalencePartition;
import io.github.mikeddavydov.jpwise.core.GenerationAlgorithm;
import io.github.mikeddavydov.jpwise.core.TestGenerator;
import io.github.mikeddavydov.jpwise.core.TestParameter;

/**
 * Implements a full combinatorial test case generation algorithm. This
 * algorithm generates all
 * possible combinations of parameter values while respecting compatibility
 * rules between values.
 *
 * <p>
 * Unlike the pairwise algorithm that only covers pairs of values, this
 * algorithm generates every
 * possible combination, which provides complete coverage but results in a much
 * larger number of
 * test cases.
 *
 * <p>
 * The number of test cases grows exponentially with the number of parameters
 * and their values.
 * For example, with 3 parameters having 3 values each, this could generate up
 * to 27 test cases
 * (3^3).
 *
 * <p>
 * Example usage:
 *
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
 * <p>
 * Use this algorithm when:
 *
 * <ul>
 * <li>Complete test coverage is required
 * <li>The number of parameters and values is small
 * <li>You need to verify all possible interactions
 * </ul>
 *
 * @author DavydovMD
 * @see GenerationAlgorithm
 * @see TestGenerator
 */
public class CombinatorialAlgorithm extends GenerationAlgorithm {
  private static final Logger logger = LoggerFactory.getLogger(CombinatorialAlgorithm.class);

  /** Creates a new combinatorial algorithm instance. */
  public CombinatorialAlgorithm() {
    super();
    logger.debug("Created new CombinatorialAlgorithm instance");
  }

  /**
   * Generates all possible combinations of parameter values. The algorithm builds
   * combinations
   * recursively, checking compatibility rules at each step to avoid generating
   * invalid
   * combinations.
   *
   * @param testGenerator The test generator containing input parameters
   * @param limit         The maximum number of combinations to generate
   */
  @Override
  public void generate(TestGenerator testGenerator, int limit) {
    logger.info("Starting combinatorial test generation with limit {}", limit);

    List<TestParameter> parameters = testGenerator.input().getTestParameters();
    if (parameters.isEmpty()) {
      logger.warn("No parameters provided for test generation");
      return;
    }

    List<Combination> results = new ArrayList<>();
    Combination current = new Combination(parameters.size());

    generateCombinationsRecursive(parameters, 0, current, results, limit);

    // Add all valid combinations to the result table
    for (Combination combination : results) {
      if (combination.isFilled()) {
        testGenerator.result().add(combination);
      }
    }

    logger.info(
        "Generated {} valid combinations out of {} possible combinations",
        results.size(),
        calculateTotalCombinations(parameters));
  }

  private void generateCombinationsRecursive(
      List<TestParameter> parameters,
      int currentIndex,
      Combination current,
      List<Combination> results,
      int limit) {
    if (currentIndex >= parameters.size()) {
      if (current.isFilled() && current.checkNoConflicts(this)) {
        results.add(new Combination(current));
        if (results.size() >= limit) {
          return;
        }
      }
      return;
    }

    TestParameter parameter = parameters.get(currentIndex);
    if (parameter == null || parameter.getPartitions().isEmpty()) {
      logger.warn("Parameter at index {} is null or has no partitions", currentIndex);
      return;
    }

    for (EquivalencePartition partition : parameter.getPartitions()) {
      if (partition == null) {
        logger.warn("Null partition found in parameter {}", parameter.getName());
        continue;
      }

      current.setValue(currentIndex, partition);
      if (current.checkNoConflicts(this)) {
        generateCombinationsRecursive(parameters, currentIndex + 1, current, results, limit);
        if (results.size() >= limit) {
          return;
        }
      }
    }
  }

  private long calculateTotalCombinations(List<TestParameter> parameters) {
    long total = 1;
    for (TestParameter parameter : parameters) {
      total *= parameter.getPartitions().size();
    }
    return total;
  }
}
