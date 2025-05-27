package io.github.mikeddavydov.jpwise.algo;

import static com.google.common.base.Preconditions.checkArgument;

import io.github.mikeddavydov.jpwise.core.Combination;
import io.github.mikeddavydov.jpwise.core.EquivalencePartition;
import io.github.mikeddavydov.jpwise.core.GenerationAlgorithm;
import io.github.mikeddavydov.jpwise.core.TestGenerator;
import io.github.mikeddavydov.jpwise.core.TestInput;
import io.github.mikeddavydov.jpwise.core.TestParameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Original implementation of the pairwise algorithm that builds combinations by incrementally
 * merging compatible pairs of equivalence partitions. The concrete values for test cases are
 * obtained from the partitions when building the final combinations.
 *
 * <p>Kept for comparison testing and validation of the new implementation.
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
  public void generate(TestGenerator testGenerator, int nwise) {
    pwGenerator = testGenerator;
    generatePartialCombinations();
    while (!combinationQueue.isEmpty()) {
      Combination entry = buildCombination();
      logger.trace(
          "Progress result:"
              + pwGenerator.result().size()
              + " queue:"
              + combinationQueue.size()
              + " -- "
              + entry.getKey());
    }
  }

  /** Generate candidate partial combinations. */
  private void generatePartialCombinations() {
    int size = pwGenerator.input().size();
    for (int i = 0; i < size; i++) {
      for (int j = i + 1; j < size; j++) {
        generatePairs(i, j);
      }
    }
  }

  /**
   * Generate partial combination for parameters.
   *
   * @param i First parameter index
   * @param j Second parameter index
   */
  private void generatePairs(int i, int j) {
    TestInput testInput = pwGenerator.input();
    TestParameter param1 = testInput.get(i);
    TestParameter param2 = testInput.get(j);

    // Get all possible partitions for both parameters
    List<EquivalencePartition> param1Partitions = new ArrayList<>(param1.getPartitions());
    Collections.shuffle(param1Partitions, random);

    for (EquivalencePartition v1 : param1Partitions) {
      List<EquivalencePartition> param2Partitions = new ArrayList<>(param2.getPartitions());
      Collections.shuffle(param2Partitions, random);

      for (EquivalencePartition v2 : param2Partitions) {
        // Skip incompatible pairs
        if (!isCompatible(v1, v2)) {
          continue;
        }
        Combination entry = new Combination(testInput.size());
        entry.setValue(i, v1);
        entry.setValue(j, v2);

        String key = entry.getKey();
        combinationMap.put(key, PENDING);
        combinationQueue.add(entry);
      }
    }
  }

  /**
   * Build a combination from what is available in the queue. Uses incremental merging of compatible
   * pairs.
   *
   * @return A complete combination
   */
  private Combination buildCombination() {
    int offset = -jump;
    Combination curCombination = new Combination(input().size());

    List<Combination> toPutBack = new ArrayList<>();

    while (!curCombination.isFilled() && !combinationQueue.isEmpty()) {
      offset = (offset + jump) % combinationQueue.size();
      Combination fromQueue = combinationQueue.remove(offset);

      String key = fromQueue.getKey();
      logger.trace(" - trying: " + key);
      Integer status = combinationMap.get(key);
      if (status == COMPLETED) {
        logger.trace(" - skipping: " + key);
        continue;
      }

      // First check if the combination itself is valid
      if (!fromQueue.checkNoConflicts(this)) {
        logger.trace(" - skipping conflicting combination: " + key);
        continue;
      }

      boolean isConflicted = false;

      Combination mergedCombination = curCombination.merge(fromQueue);
      if (mergedCombination != null) {
        isConflicted = !mergedCombination.checkNoConflicts(this);
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
      markCombinations(curCombination);
    }

    combinationQueue.addAll(toPutBack);
    completeCombination(curCombination);

    addToResult(curCombination);

    return curCombination;
  }

  private void completeCombination(Combination combination) {
    TestInput input = pwGenerator.input();

    checkArgument(
        combination.checkNoConflicts(this),
        "Combination should be initially consistent, with no conflicting values. It is not:"
            + combination);
    EquivalencePartition[] initial = combination.getValues();

    for (int i = 0; i < input.size(); i++) {
      if (combination.getValue(i) == null) {
        boolean completed = false;
        // Shuffle partitions for each parameter
        List<EquivalencePartition> shuffledPartitions =
            new ArrayList<>(input().get(i).getPartitions());
        Collections.shuffle(shuffledPartitions, random);
        for (EquivalencePartition partition : shuffledPartitions) {
          combination.setValue(i, partition);
          if (combination.checkNoConflicts(this)) {
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
    int size = input().size();
    for (int i = 0; i < size; i++) {
      for (int j = i + 1; j < size; j++) {
        if ((combination.getValue(i) != null) && (combination.getValue(j) != null)) {
          Combination pair = new Combination(size);
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
