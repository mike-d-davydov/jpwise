package io.github.mikeddavydov.jpwise.algo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mikeddavydov.jpwise.core.Combination;
import io.github.mikeddavydov.jpwise.core.CombinationTable;
import io.github.mikeddavydov.jpwise.core.EquivalencePartition;
import io.github.mikeddavydov.jpwise.core.GenerationAlgorithm;
import io.github.mikeddavydov.jpwise.core.TestInput;
import io.github.mikeddavydov.jpwise.core.TestParameter;

/**
 * Algorithm that generates all possible combinations of parameter values.
 * <p>
 * This algorithm generates the complete set of possible combinations by
 * taking the cartesian product of all parameter values. It then filters
 * out combinations that violate compatibility rules.
 * <p>
 * Note that this algorithm can generate a very large number of combinations
 * if there are many parameters or values per parameter. Use with caution.
 */
public class CombinatorialAlgorithm extends GenerationAlgorithm {
  private static final Logger logger = LoggerFactory.getLogger(
      CombinatorialAlgorithm.class);
  private int limit = Integer.MAX_VALUE;

  public CombinatorialAlgorithm() {
    super();
  }

  /**
   * Constructs a CombinatorialAlgorithm with a specific limit.
   * This limit can be overridden by the one passed to the generate method.
   * 
   * @param limit The maximum number of combinations to generate.
   */
  public CombinatorialAlgorithm(int limit) {
    super();
    if (limit <= 0) {
      throw new IllegalArgumentException("Limit must be positive.");
    }
    this.limit = limit;
  }

  @Override
  public CombinationTable generate(TestInput input, int nWiseOrLimit) {
    // The nWiseOrLimit parameter for CombinatorialAlgorithm is the actual limit.
    final int effectiveLimit = (nWiseOrLimit > 0) ? nWiseOrLimit : this.limit;
    logger.info("Generating all possible combinations for {} parameters, limit: {}",
        input.getTestParameters().size(), effectiveLimit);

    List<Combination> combinations = new ArrayList<>();
    List<TestParameter> parameters = input.getTestParameters();

    Combination current = new Combination(parameters);
    // Pass effectiveLimit to the recursive helper
    generateCombinationsRecursive(combinations, current, parameters, 0, effectiveLimit);

    logger.info("Generated {} combinations (limit was {})", combinations.size(), effectiveLimit);
    return new CombinationTable(combinations);
  }

  private void generateCombinationsRecursive(List<Combination> combinations, Combination current,
      List<TestParameter> parameters, int index, final int effectiveLimit) {
    if (index == parameters.size()) {
      if (combinations.size() >= effectiveLimit) {
        return;
      }
      if (isValidCombination(current)) {
        combinations.add(new Combination(current));
      }
      return;
    }

    if (combinations.size() >= effectiveLimit) {
      return;
    }

    TestParameter parameter = parameters.get(index);
    for (EquivalencePartition partition : parameter.getPartitions()) {
      current.setValue(index, partition);
      generateCombinationsRecursive(combinations, current, parameters, index + 1, effectiveLimit);
      if (combinations.size() >= effectiveLimit) {
        return;
      }
    }
  }
}
