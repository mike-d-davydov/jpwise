package com.functest.jpwise.algo;

import com.functest.jpwise.core.*;
import com.google.common.base.Preconditions;
import java.util.*;
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
@SuppressWarnings("rawtypes")
public class LegacyPairwiseAlgorithm extends GenerationAlgorithm {
  /** Flag value to indicate that a combination pair has been generated. */
  private static final int COMPLETED = 1;

  /** Flag value to indicate that a combination pair has NOT been generated. */
  private static final int PENDING = 0;

  private static Logger logger = LoggerFactory.getLogger(LegacyPairwiseAlgorithm.class);

  /** Map of candidate combination pairs. */
  private Map<String, Integer> _combinationMap = new HashMap<>();

  /** List of unprocessed combination pairs. */
  private List<Combination> _combinationQueue = new ArrayList<Combination>();

  /** parameter to tune the selection of unprocessed combination pairs. */
  private int _jump;

  private final Random random = new Random();

  /** Constructor. */
  public LegacyPairwiseAlgorithm(int jump) {
    super();
    _jump = jump;
  }

  public LegacyPairwiseAlgorithm() {
    this(3);
  }

  /** Main generation algorithm. */
  @Override
  public void generate(TestGenerator testGenerator, int nwise) {
    pwGenerator = testGenerator;
    generatePartialCombinations();
    while (!_combinationQueue.isEmpty()) {
      Combination entry = buildCombination();
      logger.trace(
          "Progress result:"
              + pwGenerator.result().size()
              + " queue:"
              + _combinationQueue.size()
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
        _combinationMap.put(key, PENDING);
        _combinationQueue.add(entry);
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
    int offset = -_jump;
    Combination curCombination = new Combination(input().size());

    List<Combination> toPutBack = new ArrayList<>();

    while (!curCombination.isFilled() && !_combinationQueue.isEmpty()) {
      offset = (offset + _jump) % _combinationQueue.size();
      Combination fromQueue = _combinationQueue.remove(offset);

      String key = fromQueue.getKey();
      logger.trace(" - trying: " + key);
      Integer status = _combinationMap.get(key);
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

    _combinationQueue.addAll(toPutBack);
    completeCombination(curCombination);

    addToResult(curCombination);

    return curCombination;
  }

  private void completeCombination(Combination combination) {
    TestInput input = pwGenerator.input();

    Preconditions.checkArgument(
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

        if (!completed)
          logger.warn(
              "Failed to find value of parameter "
                  + combination.getValue(i).getParentParameter()
                  + " compatible with other parameter values in combination "
                  + Arrays.toString(initial));
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
          Integer status = _combinationMap.get(key);
          if (status != COMPLETED) {
            logger.trace(" - clearing: " + key);
            _combinationMap.put(key, COMPLETED);
          }
        }
      }
    }
  }
}
