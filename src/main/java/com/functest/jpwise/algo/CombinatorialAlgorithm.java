package com.functest.jpwise.algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.functest.jpwise.core.*;

/**
 * Implements a full combinatorial test case generation algorithm. This algorithm generates all
 * possible combinations of parameter values while respecting compatibility rules between values.
 *
 * <p>Unlike the pairwise algorithm that only covers pairs of values, this algorithm generates every
 * possible combination, which provides complete coverage but results in a much larger number of
 * test cases.
 *
 * <p>The number of test cases grows exponentially with the number of parameters and their values.
 * For example, with 3 parameters having 3 values each, this could generate up to 27 test cases
 * (3^3).
 *
 * <p>Example usage:
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
 * <p>Use this algorithm when:
 *
 * <ul>
 *   <li>Complete test coverage is required
 *   <li>The number of parameters and values is small
 *   <li>You need to verify all possible interactions
 * </ul>
 *
 * @author DavydovMD
 * @see GenerationAlgorithm
 * @see TestGenerator
 */
public class CombinatorialAlgorithm extends GenerationAlgorithm {

  /** Creates a new combinatorial algorithm instance. */
  public CombinatorialAlgorithm() {
    super();
  }

  /**
   * Recursively generates all possible combinations starting from a given parameter index. Only
   * valid combinations that satisfy compatibility rules are included.
   *
   * @param currentCombination The combination being built
   * @param paramIndex The current parameter index
   * @param validCombinations List to store valid combinations
   */
  private void generateCombinationsRecursive(
      Combination currentCombination, int paramIndex, List<Combination> validCombinations) {
    // If we've assigned values to all parameters, check if combination is valid
    if (paramIndex >= input().size()) {
      if (currentCombination.checkNoConflicts(this)) {
        validCombinations.add(new Combination(currentCombination.size()));
        // Copy values to new combination
        for (int i = 0; i < currentCombination.size(); i++) {
          validCombinations
              .get(validCombinations.size() - 1)
              .setValue(i, currentCombination.getValue(i));
        }
      }
      return;
    }

    // Get current parameter and its values
    TestParameter parameter = input().get(paramIndex);
    List<EquivalencePartition> partitions = parameter.getPartitions();

    // Try each value for current parameter
    for (EquivalencePartition partition : partitions) {
      // Set value in combination
      currentCombination.setValue(paramIndex, partition);

      // Check if current partial combination is valid before continuing
      boolean isValid = true;
      for (int i = 0; i <= paramIndex; i++) {
        EquivalencePartition v1 = currentCombination.getValue(i);
        EquivalencePartition v2 = currentCombination.getValue(paramIndex);

        // Check compatibility between values if both are set
        if (v1 != null && v2 != null && !isCompatible(v1, v2)) {
          isValid = false;
          break;
        }
      }

      // Only recurse if current partial combination is valid
      if (isValid) {
        generateCombinationsRecursive(currentCombination, paramIndex + 1, validCombinations);
      }
    }
  }

  /**
   * Generates all possible combinations of parameter values. The algorithm builds combinations
   * recursively, checking compatibility rules at each step to avoid generating invalid
   * combinations.
   *
   * @param testGenerator The test generator containing input parameters
   * @param limit The maximum number of combinations to generate
   */
  @Override
  public void generate(TestGenerator testGenerator, int limit) {
    pwGenerator = testGenerator;

    // Initialize list to store valid combinations
    List<Combination> validCombinations = new ArrayList<>();

    // Start recursive generation with empty combination
    Combination initialCombination = new Combination(input().size());
    generateCombinationsRecursive(initialCombination, 0, validCombinations);

    // Shuffle combinations if we need to limit them
    if (limit < validCombinations.size()) {
      Collections.shuffle(validCombinations);
      validCombinations = validCombinations.subList(0, limit);
    }

    // Add valid combinations to result
    for (Combination combination : validCombinations) {
      pwGenerator.result().add(combination);
    }
  }
}
