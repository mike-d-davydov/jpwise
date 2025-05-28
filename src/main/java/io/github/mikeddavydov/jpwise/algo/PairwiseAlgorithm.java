package io.github.mikeddavydov.jpwise.algo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mikeddavydov.jpwise.core.Combination;
import io.github.mikeddavydov.jpwise.core.CombinationTable;
import io.github.mikeddavydov.jpwise.core.EquivalencePartition;
import io.github.mikeddavydov.jpwise.core.GenerationAlgorithm;
import io.github.mikeddavydov.jpwise.core.TestInput;
import io.github.mikeddavydov.jpwise.core.TestParameter;

/**
 * Algorithm that generates pairwise test combinations.
 *
 * <p>This algorithm generates test combinations that cover all pairs of parameter values while
 * respecting compatibility rules. It uses a greedy approach to build combinations that maximize
 * coverage of value pairs.
 */
public class PairwiseAlgorithm extends GenerationAlgorithm {
  private static final Logger logger = LoggerFactory.getLogger(PairwiseAlgorithm.class);

  public PairwiseAlgorithm() {
    super();
  }

  /**
   * Creates a new pairwise algorithm with the specified size.
   *
   * @param size The number of parameters to generate combinations for
   */
  public PairwiseAlgorithm(int size) {
    super();
  }

  @Override
  public CombinationTable generate(TestInput input) {
    logger.debug(
        "PairwiseAlgorithm.generate() called. Input parameters: {}",
        input.getTestParameters().size());
    List<TestParameter> parameters = input.getTestParameters();
    if (parameters.size() < 2) {
      logger.info("Need at least 2 parameters for pairwise generation. Returning empty table.");
      return new CombinationTable(new ArrayList<>());
    }

    List<ValuePair> pairs = generateValuePairs(parameters); // All unique pairs to be covered
    logger.info(
        "Generating pairwise combinations for {} parameters. Total pairs to cover: {}",
        parameters.size(),
        pairs.size());
    if (pairs.isEmpty() && parameters.size() >= 2) {
      // This can happen if parameters have no partitions, or only one partition each,
      // etc.
      // Or if all potential pairs are ruled out by global compatibility rules (not
      // yet implemented here)
      logger.warn(
          "Initial pair generation resulted in zero pairs to cover. Returning empty table.");
      return new CombinationTable(new ArrayList<>());
    }

    List<Combination> combinations = new ArrayList<>();
    int lastLoopPairsCount = pairs.size() + 1; // Ensure first loop runs to track stalling

    while (!pairs.isEmpty()) {
      if (Thread.currentThread().isInterrupted()) {
        logger.warn("Pairwise generation thread interrupted. Returning current results.");
        break;
      }

      if (pairs.size() == lastLoopPairsCount) {
        logger.warn(
            "No pairs were covered in the previous iteration. Breaking to avoid infinite loop with {} pairs remaining.",
            pairs.size());
        logRemainingPairs(pairs);
        break;
      }
      lastLoopPairsCount = pairs.size();

      logger.info(
          "Starting new combination generation cycle. Pairs to cover: {} (Last cycle had: {})",
          pairs.size(),
          lastLoopPairsCount);
      // Pass a mutable copy of current pairs for buildCombination to potentially
      // reorder or prioritize
      Combination combination = buildCombination(parameters, new ArrayList<>(pairs));

      if (combination != null && combination.getSetCount() > 0 && isValidCombination(combination)) {
        logger.info("Successfully built a combination: {}", combination.toString());
        combinations.add(combination);
        int pairsRemovedCount = 0;
        int initialPairCountForCycle = pairs.size();

        // Remove newly covered pairs from the main list
        Iterator<ValuePair> pairIterator = pairs.iterator();
        while (pairIterator.hasNext()) {
          ValuePair pair = pairIterator.next();
          if (isPairCovered(pair, combination)) {
            logger.trace(
                "Pair covered and removed: {} ({}) + {} ({}) by {}",
                pair.param1.getName(),
                pair.value1.getName(),
                pair.param2.getName(),
                pair.value2.getName(),
                combination.toString());
            pairIterator.remove();
            pairsRemovedCount++;
          }
        }
        logger.info(
            "Removed {} pairs covered by this combination (out of {}). Remaining pairs: {}",
            pairsRemovedCount,
            initialPairCountForCycle,
            pairs.size());

        if (pairsRemovedCount == 0 && !pairs.isEmpty()) {
          // This is a critical warning. The combination was valid, but covered no *new*
          // pairs.
          logger.warn(
              "CRITICAL: The generated combination {} did not cover any new pairs from the remaining {} pairs. This will cause an infinite loop.",
              combination.toString(),
              pairs.size());
          // To prevent infinite loop, we must break if this happens.
          // The check `pairs.size() == lastLoopPairsCount` at the loop start will handle
          // this.
        }
      } else {
        logger.warn(
            "buildCombination returned null or an invalid/empty combination. No combination added in this cycle. Pairs remaining: {}",
            pairs.size());
        // If buildCombination returns null, pairs list hasn't changed.
        // The check `pairs.size() == lastLoopPairsCount` at the start of the next
        // iteration will catch this and break.
      }
    } // End while !pairs.isEmpty()

    if (!pairs.isEmpty()) {
      logger.warn("Exited generation loop with {} pairs still uncovered:", pairs.size());
      logRemainingPairs(pairs);
    }

    logger.info(
        "Pairwise generation finished. Generated {} combinations in total.", combinations.size());
    return new CombinationTable(combinations);
  }

  private void logRemainingPairs(List<ValuePair> pairs) {
    if (pairs.isEmpty()) return;
    logger.warn("Listing up to 10 remaining uncovered pairs:");
    for (int i = 0; i < Math.min(pairs.size(), 10); i++) {
      ValuePair p = pairs.get(i);
      logger.warn(
          "  - Pair: Param1={}, Value1={}, Param2={}, Value2={}",
          p.param1.getName(),
          p.value1.getName(),
          p.param2.getName(),
          p.value2.getName());
    }
    if (pairs.size() > 10) {
      logger.warn("  ... and {} more.", pairs.size() - 10);
    }
  }

  private List<ValuePair> generateValuePairs(List<TestParameter> parameters) {
    List<ValuePair> pairs = new ArrayList<>();
    for (int i = 0; i < parameters.size(); i++) {
      TestParameter param1 = parameters.get(i);
      for (int j = i + 1; j < parameters.size(); j++) {
        TestParameter param2 = parameters.get(j);
        for (EquivalencePartition value1 : param1.getPartitions()) {
          for (EquivalencePartition value2 : param2.getPartitions()) {
            if (value1.isCompatibleWith(value2)) {
              pairs.add(new ValuePair(param1, value1, param2, value2));
            }
          }
        }
      }
    }
    return pairs;
  }

  private Combination buildCombination(List<TestParameter> parameters, List<ValuePair> pairs) {
    logger.debug("--- buildCombination START ---");
    logger.debug(
        "Attempting to build combination for {} parameters with {} remaining pairs.",
        parameters.size(),
        pairs.size());
    Combination combination = new Combination(parameters);
    Queue<ValuePair> pairQueue = new LinkedList<>(pairs); // Use a copy for iteration

    int iteration = 0;
    while (!pairQueue.isEmpty()) {
      iteration++;
      ValuePair currentPair = pairQueue.poll();
      logger.debug(
          "[Iter {}] Polled pair: {} ({}) + {} ({})",
          iteration,
          currentPair.param1.getName(),
          currentPair.value1.getName(),
          currentPair.param2.getName(),
          currentPair.value2.getName());
      logger.debug("[Iter {}] Current combination state: {}", iteration, combination.toString());

      if (tryAddPairToCombination(combination, currentPair)) {
        logger.debug(
            "[Iter {}] Successfully ADDED pair directly. Combination now: {}",
            iteration,
            combination.toString());
        // If combination is full and valid, it might be a candidate, but the loop
        // continues to cover more pairs.
        if (combination.isFilled() && isValidCombination(combination)) {
          logger.debug("[Iter {}] Combination is FILLED and VALID after adding pair.", iteration);
        }
        continue; // Try next pair
      }
      logger.debug("[Iter {}] Could NOT add pair directly. Trying to ADJUST.", iteration);

      // If direct add fails, try to adjust the current combination to accommodate the
      // pair
      // This part is tricky; the original algorithm might not have a robust
      // adjustment here
      // For now, the original tryAdjustCombination is simple. If it fails, this path
      // means the pair couldn't be added.
      if (!tryAdjustCombination(combination, currentPair)) {
        // This implies that with the current state of 'combination', 'currentPair'
        // cannot be satisfied
        // even with simple adjustments. The original algorithm might have returned null
        // here or continued.
        // For debugging, let's log this and continue to see if other pairs can be
        // added.
        // A more sophisticated algorithm might try more complex backtracking or
        // re-ordering.
        logger.debug(
            "[Iter {}] Could NOT ADJUST combination for pair. Pair remains uncovered for now.",
            iteration);
        // It's crucial to decide what to do here. If we simply continue, we might never
        // cover this pair.
        // If the pair can't be added or adjusted for, the original logic implied it
        // would be re-queued
        // or effectively dropped for this specific combination build attempt.
        // The 'return null' from the original tryAdjustCombination if it failed meant
        // that
        // buildCombination would return null, and the main loop would break.
        // Let's keep the original behavior: if adjustment fails, this build attempt
        // fails.
        logger.debug("--- buildCombination END (returning NULL due to failed adjustment) ---");
        return null; // Adjustment failed, cannot satisfy this pair in current path
      }
      logger.debug(
          "[Iter {}] Successfully ADJUSTED combination for pair. Combination now: {}",
          iteration,
          combination.toString());
      if (combination.isFilled() && isValidCombination(combination)) {
        logger.debug(
            "[Iter {}] Combination is FILLED and VALID after adjusting for pair.", iteration);
      }
    }

    // MARKER START OF NEW RETURN LOGIC
    if (combination == null) {
      logger.error("IMPOSSIBLE: Combination object itself is null before final return decision.");
      return null;
    }

    boolean isValid = isValidCombination(combination);
    int setCount = combination.getSetCount();
    boolean isFilled = combination.isFilled();

    logger.debug("FINAL CHECK: isValid={}, setCount={}, isFilled={}", isValid, setCount, isFilled);
    logger.debug("FINAL COMBINATION STATE: {}", combination.toString());

    if (isFilled && isValid) {
      logger.info("RETURN_LOGIC_BRANCH_1_FILLED_VALID");
      return combination;
    } else if (setCount > 0 && isValid) {
      logger.info("RETURN_LOGIC_BRANCH_2_PARTIAL_VALID");
      return combination;
    } else {
      logger.warn(
          "RETURN_LOGIC_BRANCH_3_NULL_CASE. State: {}, Filled: {}, Valid: {}, SetCount: {}",
          combination.toString(),
          isFilled,
          isValid,
          setCount);
      return null;
    }
    // MARKER END OF NEW RETURN LOGIC
  }

  private boolean tryAddPairToCombination(Combination combination, ValuePair pair) {
    logger.debug(
        "  -- tryAddPairToCombination START for pair: {} ({}) + {} ({}) --",
        pair.param1.getName(),
        pair.value1.getName(),
        pair.param2.getName(),
        pair.value2.getName());
    logger.debug("     Combination state BEFORE: {}", combination.toString());

    int index1 = combination.getParameters().indexOf(pair.param1);
    int index2 = combination.getParameters().indexOf(pair.param2);

    // Can we set both?
    if (combination.getValue(index1) == null && combination.getValue(index2) == null) {
      logger.debug(
          "     Slots for both params are empty. Trying to set: {} -> {}, {} -> {}",
          pair.param1.getName(),
          pair.value1.getName(),
          pair.param2.getName(),
          pair.value2.getName());
      combination.setValue(index1, pair.value1);
      combination.setValue(index2, pair.value2);
      if (isValidCombination(combination)) {
        logger.debug(
            "     SUCCESS (both empty). Combination state AFTER: {}", combination.toString());
        logger.debug("  -- tryAddPairToCombination END (true) --");
        return true;
      }
      logger.debug(
          "     FAILED (both empty, invalid). Reverting. Combination state AFTER: {}",
          combination.toString());
      combination.setValue(index1, null); // Revert
      combination.setValue(index2, null); // Revert
      logger.debug("  -- tryAddPairToCombination END (false) --");
      return false;
    }

    // Can we set param1?
    if (combination.getValue(index1) == null
        && combination.getValue(index2) != null
        && combination.getValue(index2).equals(pair.value2)) {
      logger.debug(
          "     Slot for param1 ({}) empty, param2 ({}) matches pair ({}). Trying to set: {} -> {}",
          pair.param1.getName(),
          pair.param2.getName(),
          pair.value2.getName(),
          pair.param1.getName(),
          pair.value1.getName());
      EquivalencePartition originalP1 = combination.getValue(index1); // null
      combination.setValue(index1, pair.value1);
      if (isValidCombination(combination)) {
        logger.debug(
            "     SUCCESS (param1 empty, param2 matched). Combination state AFTER: {}",
            combination.toString());
        logger.debug("  -- tryAddPairToCombination END (true) --");
        return true;
      }
      logger.debug(
          "     FAILED (param1 empty, param2 matched, invalid). Reverting. Combination state AFTER: {}",
          combination.toString());
      combination.setValue(index1, originalP1); // Revert (back to null)
      logger.debug("  -- tryAddPairToCombination END (false) --");
      return false;
    }

    // Can we set param2?
    if (combination.getValue(index2) == null
        && combination.getValue(index1) != null
        && combination.getValue(index1).equals(pair.value1)) {
      logger.debug(
          "     Slot for param2 ({}) empty, param1 ({}) matches pair ({}). Trying to set: {} -> {}",
          pair.param2.getName(),
          pair.param1.getName(),
          pair.value1.getName(),
          pair.param2.getName(),
          pair.value2.getName());
      EquivalencePartition originalP2 = combination.getValue(index2); // null
      combination.setValue(index2, pair.value2);
      if (isValidCombination(combination)) {
        logger.debug(
            "     SUCCESS (param2 empty, param1 matched). Combination state AFTER: {}",
            combination.toString());
        logger.debug("  -- tryAddPairToCombination END (true) --");
        return true;
      }
      logger.debug(
          "     FAILED (param2 empty, param1 matched, invalid). Reverting. Combination state AFTER: {}",
          combination.toString());
      combination.setValue(index2, originalP2); // Revert (back to null)
      logger.debug("  -- tryAddPairToCombination END (false) --");
      return false;
    }

    // If both slots are filled, check if they already satisfy the pair
    if (combination.getValue(index1) != null
        && combination.getValue(index1).equals(pair.value1)
        && combination.getValue(index2) != null
        && combination.getValue(index2).equals(pair.value2)) {
      logger.debug("     Pair already satisfied by current combination values.");
      logger.debug("  -- tryAddPairToCombination END (true, pair already covered) --");
      return true;
    }

    logger.debug(
        "     COULD NOT ADD pair under current conditions (slots filled incompatibly or one slot free but other mismatches).");
    logger.debug("  -- tryAddPairToCombination END (false) --");
    return false;
  }

  private boolean tryAdjustCombination(Combination combination, ValuePair pair) {
    logger.debug(
        "    -- tryAdjustCombination START for pair: {} ({}) + {} ({}) --",
        pair.param1.getName(),
        pair.value1.getName(),
        pair.param2.getName(),
        pair.value2.getName());
    logger.debug("       Combination state BEFORE adjustment: {}", combination.toString());

    int index1 = combination.getParameters().indexOf(pair.param1);
    int index2 = combination.getParameters().indexOf(pair.param2);

    // Try to adjust by changing param1 value first
    EquivalencePartition originalValueP1 = combination.getValue(index1);
    if (originalValueP1 != null && !originalValueP1.equals(pair.value1)) {
      logger.debug(
          "       Attempting to adjust P1: Current P1 ({}) is {}, pair needs {}.",
          pair.param1.getName(),
          originalValueP1.getName(),
          pair.value1.getName());
      combination.setValue(index1, pair.value1); // Tentatively set P1
      EquivalencePartition currentValueP2 = combination.getValue(index2);
      if (currentValueP2 == null) { // If P2 is not set, set it to pair's P2
        logger.debug("       P2 is null. Setting P2 to {}.", pair.value2.getName());
        combination.setValue(index2, pair.value2);
        if (isValidCombination(combination)) {
          logger.debug(
              "       SUCCESS: Changed P1 to {}, set P2 to {}. Valid. Combo: {}",
              pair.value1.getName(),
              pair.value2.getName(),
              combination.toString());
          logger.debug("    -- tryAdjustCombination END (true) --");
          return true;
        }
        combination.setValue(index2, null); // Revert P2
        logger.debug(
            "       P2 was null, set to {}, but combo invalid. Reverted P2.",
            pair.value2.getName());
      } else if (currentValueP2.equals(pair.value2)) { // If P2 is already set and matches pair's P2
        logger.debug("       P2 is already {} (matches pair).", currentValueP2.getName());
        if (isValidCombination(combination)) {
          logger.debug(
              "       SUCCESS: Changed P1 to {}. P2 matched. Valid. Combo: {}",
              pair.value1.getName(),
              combination.toString());
          logger.debug("    -- tryAdjustCombination END (true) --");
          return true;
        }
        logger.debug("       P2 matched pair, but combo invalid after changing P1.");
      } else { // P2 is set but does NOT match pair's P2. Try changing P2 as well.
        logger.debug(
            "       P2 is {} (mismatched pair value {}). Attempting to change P2 to {}.",
            currentValueP2.getName(),
            pair.value2.getName(),
            pair.value2.getName());
        EquivalencePartition originalConflictingP2 = currentValueP2; // Store it before changing
        combination.setValue(index2, pair.value2);
        if (isValidCombination(combination)) {
          logger.debug(
              "       SUCCESS: Changed P1 to {}, changed P2 from {} to {}. Valid. Combo: {}",
              pair.value1.getName(),
              originalConflictingP2.getName(),
              pair.value2.getName(),
              combination.toString());
          logger.debug("    -- tryAdjustCombination END (true) --");
          return true;
        }
        combination.setValue(
            index2, originalConflictingP2); // Revert P2 to its pre-adjustment state
        logger.debug(
            "       Changed P2 to {}, but combo invalid. Reverted P2 to {}.",
            pair.value2.getName(),
            originalConflictingP2.getName());
      }
      combination.setValue(index1, originalValueP1); // Revert P1 if all attempts above failed
      logger.debug(
          "       All P1 adjustment attempts failed. Reverted P1 to {}. Combo: {}",
          originalValueP1.getName(),
          combination.toString());
    }

    // Try to adjust by changing param2 value (if P1 adjustment didn't work or P1
    // was already ok/null)
    EquivalencePartition originalValueP2 = combination.getValue(index2);
    if (originalValueP2 != null && !originalValueP2.equals(pair.value2)) {
      logger.debug(
          "       Attempting to adjust P2: Current P2 ({}) is {}, pair needs {}.",
          pair.param2.getName(),
          originalValueP2.getName(),
          pair.value2.getName());
      combination.setValue(index2, pair.value2); // Tentatively set P2
      EquivalencePartition currentValueP1 = combination.getValue(index1);
      if (currentValueP1 == null) { // If P1 is not set, set it to pair's P1
        logger.debug("       P1 is null. Setting P1 to {}.", pair.value1.getName());
        combination.setValue(index1, pair.value1);
        if (isValidCombination(combination)) {
          logger.debug(
              "       SUCCESS: Changed P2 to {}, set P1 to {}. Valid. Combo: {}",
              pair.value2.getName(),
              pair.value1.getName(),
              combination.toString());
          logger.debug("    -- tryAdjustCombination END (true) --");
          return true;
        }
        combination.setValue(index1, null); // Revert P1
        logger.debug(
            "       P1 was null, set to {}, but combo invalid. Reverted P1.",
            pair.value1.getName());
      } else if (currentValueP1.equals(pair.value1)) { // If P1 is already set and matches pair's P1
        logger.debug("       P1 is already {} (matches pair).", currentValueP1.getName());
        if (isValidCombination(combination)) {
          logger.debug(
              "       SUCCESS: Changed P2 to {}. P1 matched. Valid. Combo: {}",
              pair.value2.getName(),
              combination.toString());
          logger.debug("    -- tryAdjustCombination END (true) --");
          return true;
        }
        logger.debug("       P1 matched pair, but combo invalid after changing P2.");
      } else { // P1 is set but does NOT match pair's P1. Try changing P1 as well. (This case
        // implies P1 needs to be changed to satisfy the pair, in conjunction with P2)
        logger.debug(
            "       P1 is {} (mismatched pair value {}). Attempting to change P1 to {}.",
            currentValueP1.getName(),
            pair.value1.getName(),
            pair.value1.getName());
        EquivalencePartition originalConflictingP1 = currentValueP1; // Store it before changing
        combination.setValue(index1, pair.value1);
        if (isValidCombination(combination)) {
          logger.debug(
              "       SUCCESS: Changed P2 to {}, changed P1 from {} to {}. Valid. Combo: {}",
              pair.value2.getName(),
              originalConflictingP1.getName(),
              pair.value1.getName(),
              combination.toString());
          logger.debug("    -- tryAdjustCombination END (true) --");
          return true;
        }
        combination.setValue(
            index1, originalConflictingP1); // Revert P1 to its pre-adjustment state
        logger.debug(
            "       Changed P1 to {}, but combo invalid. Reverted P1 to {}.",
            pair.value1.getName(),
            originalConflictingP1.getName());
      }
      combination.setValue(index2, originalValueP2); // Revert P2 if all attempts above failed
      logger.debug(
          "       All P2 adjustment attempts failed. Reverted P2 to {}. Combo: {}",
          originalValueP2.getName(),
          combination.toString());
    }

    logger.debug(
        "       COULD NOT ADJUST combination with current strategy for this pair (P1 and P2 might be null or already match pair, or adjustment attempts failed).");
    logger.debug("    -- tryAdjustCombination END (false) --");
    return false;
  }

  private boolean isPairCovered(ValuePair pair, Combination combination) {
    int index1 = combination.getParameters().indexOf(pair.param1);
    int index2 = combination.getParameters().indexOf(pair.param2);
    EquivalencePartition value1 = combination.getValue(index1);
    EquivalencePartition value2 = combination.getValue(index2);
    return value1 != null
        && value2 != null
        && value1.equals(pair.value1)
        && value2.equals(pair.value2);
  }

  private static class ValuePair {
    final TestParameter param1;
    final EquivalencePartition value1;
    final TestParameter param2;
    final EquivalencePartition value2;

    ValuePair(
        TestParameter param1,
        EquivalencePartition value1,
        TestParameter param2,
        EquivalencePartition value2) {
      this.param1 = param1;
      this.value1 = value1;
      this.param2 = param2;
      this.value2 = value2;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ValuePair)) return false;
      ValuePair other = (ValuePair) o;
      return param1.equals(other.param1)
          && value1.equals(other.value1)
          && param2.equals(other.param2)
          && value2.equals(other.value2);
    }

    @Override
    public int hashCode() {
      int result = param1.hashCode();
      result = 31 * result + value1.hashCode();
      result = 31 * result + param2.hashCode();
      result = 31 * result + value2.hashCode();
      return result;
    }
  }
}
