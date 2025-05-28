package io.github.mikeddavydov.jpwise.algo;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mikeddavydov.jpwise.core.Combination;
import io.github.mikeddavydov.jpwise.core.CombinationTable;
import io.github.mikeddavydov.jpwise.core.EquivalencePartition;
import io.github.mikeddavydov.jpwise.core.GenerationAlgorithm;
import io.github.mikeddavydov.jpwise.core.TestInput;
import io.github.mikeddavydov.jpwise.core.TestParameter;

/**
 * Original implementation of the pairwise algorithm that builds combinations by
 * incrementally
 * merging compatible pairs of equivalence partitions. The concrete values for
 * test cases are
 * obtained from the partitions when building the final combinations.
 *
 * <p>
 * Kept for comparison testing and validation of the new implementation.
 *
 * @author panwei
 */
public class LegacyPairwiseAlgorithm extends GenerationAlgorithm {
  /** Flag value to indicate that a combination pair has been generated. */
  private static final int COMPLETED = 1;

  /** Flag value to indicate that a combination pair has NOT been generated. */
  private static final int PENDING = 0;

  private static Logger logger = LoggerFactory.getLogger(LegacyPairwiseAlgorithm.class);

  /** Map of candidate combination pairs. */
  private Map<String, Integer> combinationMap = new HashMap<>();

  /** List of unprocessed combination pairs. */
  private List<Combination> combinationQueue = new ArrayList<Combination>();

  /** parameter to tune the selection of unprocessed combination pairs. */
  private int jump;

  private final Random random = new Random();

  /** Constructor. */
  public LegacyPairwiseAlgorithm(int jump) {
    super();
    this.jump = jump;
  }

  public LegacyPairwiseAlgorithm() {
    this(3);
  }

  /** Main generation algorithm. */
  @Override
  public CombinationTable generate(TestInput input, int nWiseOrLimit) {
    // nWiseOrLimit is not used by LegacyPairwiseAlgorithm, but is part of the
    // interface.
    logger.info("Generating pairwise combinations for {} parameters (nWiseOrLimit={} ignored)",
        input.getTestParameters().size(), nWiseOrLimit);

    List<Combination> combinations = new ArrayList<>();
    generatePartialCombinations(input);

    while (!combinationQueue.isEmpty()) {
      Combination entry = buildCombination(input);
      if (entry != null) {
        combinations.add(entry);
        logger.trace("Progress result: {} queue: {} -- {}",
            combinations.size(), combinationQueue.size(), entry.getKey());
      }
    }

    logger.info("Generated {} combinations", combinations.size());
    return new CombinationTable(combinations);
  }

  /** Generate candidate partial combinations. */
  private void generatePartialCombinations(TestInput input) {
    int size = input.size();
    for (int i = 0; i < size; i++) {
      for (int j = i + 1; j < size; j++) {
        generatePairs(input, i, j);
      }
    }
  }

  /**
   * Generate partial combination for parameters.
   *
   * @param input The test input
   * @param i     First parameter index
   * @param j     Second parameter index
   */
  private void generatePairs(TestInput input, int i, int j) {
    TestParameter param1 = input.get(i);
    TestParameter param2 = input.get(j);
    logger.debug("generatePairs called for param index {} ({}) and param index {} ({})", i, param1.getName(), j,
        param2.getName());

    // Get all possible partitions for both parameters
    List<EquivalencePartition> param1Partitions = new ArrayList<>(param1.getPartitions());
    Collections.shuffle(param1Partitions, random);

    for (EquivalencePartition v1 : param1Partitions) {
      List<EquivalencePartition> param2Partitions = new ArrayList<>(param2.getPartitions());
      Collections.shuffle(param2Partitions, random);
      logger.debug("  Processing v1: {} from {}", v1.getName(), param1.getName());

      for (EquivalencePartition v2 : param2Partitions) {
        logger.debug("    Processing v2: {} from {}", v2.getName(), param2.getName());
        // Skip incompatible pairs
        if (!v1.isCompatibleWith(v2)) {
          logger.debug("      v1 {} and v2 {} are NOT compatible (isCompatibleWith returned false)", v1.getName(),
              v2.getName());
          continue;
        }
        // Added explicit check for TestParameter.areCompatible, which is what
        // isValidCombination uses
        if (!param1.areCompatible(v1, v2)) {
          logger.debug("      param1 {} and v1 {} are NOT compatible with v2 {} (param1.areCompatible returned false)",
              param1.getName(), v1.getName(), v2.getName());
          continue;
        }
        if (!param2.areCompatible(v2, v1)) {
          logger.debug("      param2 {} and v2 {} are NOT compatible with v1 {} (param2.areCompatible returned false)",
              param2.getName(), v2.getName(), v1.getName());
          continue;
        }

        Combination entry = new Combination(input.getTestParameters());
        entry.setValue(i, v1);
        entry.setValue(j, v2);

        String key = entry.getKey();
        combinationMap.put(key, PENDING);
        combinationQueue.add(entry);
      }
    }
  }

  /**
   * Build a combination from what is available in the queue. Uses incremental
   * merging of compatible
   * pairs.
   *
   * @param input The test input
   * @return A complete combination
   */
  private Combination buildCombination(TestInput input) {
    logger.debug("buildCombination called. Initial combinationQueue size: {}", combinationQueue.size());
    int offset = -jump;
    Combination curCombination = new Combination(input.getTestParameters());

    List<Combination> toPutBack = new ArrayList<>();

    while (!curCombination.isFilled() && !combinationQueue.isEmpty()) {
      offset = (offset + jump) % combinationQueue.size();
      logger.debug("  buildCombination loop: offset = {}, queue size = {}", offset, combinationQueue.size());
      Combination fromQueue = combinationQueue.remove(offset);
      logger.debug("    Removed fromQueue: {}", fromQueue.getKey());

      String key = fromQueue.getKey();
      logger.trace(" - trying: " + key);
      Integer status = combinationMap.get(key);
      if (status == COMPLETED) {
        logger.trace(" - skipping: " + key);
        continue;
      }

      // First check if the combination itself is valid
      logger.debug("    Checking validity of fromQueue itself: {}", fromQueue.getKey());
      if (!isValidCombination(fromQueue)) {
        logger.trace(" - skipping conflicting combination: " + key);
        toPutBack.add(fromQueue);
        continue;
      }
      logger.debug("    fromQueue is valid. Current curCombination: {}", curCombination.getKey());

      boolean isConflicted = false;

      logger.debug("    Attempting to merge curCombination with fromQueue");
      Combination mergedCombination = curCombination.merge(fromQueue);

      if (mergedCombination != null) {
        logger.debug("    Merge successful. Merged combination: {}. Checking its validity.",
            mergedCombination.getKey());
        isConflicted = !isValidCombination(mergedCombination);
        if (isConflicted) {
          logger.debug("    Merged combination {} is conflicted.", mergedCombination.getKey());
        }
      } else {
        logger.debug("    Merge returned null.");
      }

      if ((mergedCombination == null) || (isConflicted)) {
        toPutBack.add(fromQueue);
        logger.trace(
            " - postponing: "
                + key
                + ". Merge conflict?"
                + (mergedCombination == null)
                + "; Incompatible values?"
                + isConflicted);
        continue;
      }

      curCombination = mergedCombination;
      logger.debug("    curCombination updated to: {}. Marking its pairs.", curCombination.getKey());
      markCombinations(curCombination);
    }
    logger.debug("  buildCombination loop finished. curCombination: {}, isFilled: {}, queueEmpty: {}",
        curCombination.getKey(), curCombination.isFilled(), combinationQueue.isEmpty());

    combinationQueue.addAll(toPutBack);
    logger.debug("    Calling completeCombination for: {}", curCombination.getKey());
    completeCombination(input, curCombination);
    logger.debug("    completeCombination returned. Final curCombination for this build: {}", curCombination.getKey());

    return curCombination;
  }

  private void completeCombination(TestInput input, Combination combination) {
    checkArgument(
        isValidCombination(combination),
        "Combination should be initially consistent, with no conflicting values. It is not:"
            + combination);
    EquivalencePartition[] initial = combination.getValues();

    for (int i = 0; i < input.size(); i++) {
      if (combination.getValue(i) == null) {
        boolean completed = false;
        // Shuffle partitions for each parameter
        List<EquivalencePartition> shuffledPartitions = new ArrayList<>(input.get(i).getPartitions());
        Collections.shuffle(shuffledPartitions, random);
        for (EquivalencePartition partition : shuffledPartitions) {
          combination.setValue(i, partition);
          if (isValidCombination(combination)) {
            completed = true;
            break;
          } else {
            logger.trace(
                combination
                    + " contains incompatible values (changed value was: "
                    + partition
                    + ")?");
          }
        }

        if (!completed) {
          logger.warn(
              "Failed to find value of parameter "
                  + combination.getValue(i).getParentParameter()
                  + " compatible with other parameter values in combination "
                  + Arrays.toString(initial));
        }
      }
    }
  }

  /**
   * Mark partial combinations that have been used.
   *
   * @param combination The combination containing pairs to mark
   */
  private void markCombinations(Combination combination) {
    int size = combination.getParameters().size();
    for (int i = 0; i < size; i++) {
      for (int j = i + 1; j < size; j++) {
        if ((combination.getValue(i) != null) && (combination.getValue(j) != null)) {
          Combination pair = new Combination(combination.getParameters());
          pair.setValue(i, combination.getValue(i));
          pair.setValue(j, combination.getValue(j));
          String key = pair.getKey();
          Integer status = combinationMap.get(key);
          if (status != COMPLETED) {
            logger.trace(" - clearing: " + key);
            combinationMap.put(key, COMPLETED);
          }
        }
      }
    }
  }
}
