package io.github.mikeddavydov.jpwise.algo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mikeddavydov.jpwise.core.Combination;
import io.github.mikeddavydov.jpwise.core.CombinationTable;
import io.github.mikeddavydov.jpwise.core.EquivalencePartition;
import io.github.mikeddavydov.jpwise.core.GenerationAlgorithm;
import io.github.mikeddavydov.jpwise.core.TestInput;
import io.github.mikeddavydov.jpwise.core.TestParameter;

/**
 * Algorithm that generates pairwise test combinations. This is a new implementation focusing on
 * correctness and clear logic.
 */
public class PairwiseAlgorithm extends GenerationAlgorithm {

  private static final Logger LOGGER = LoggerFactory.getLogger(PairwiseAlgorithm.class);

  public PairwiseAlgorithm() {
    super();
    // LOGGER.debug("PairwiseAlgorithm instance created.");
  }

  @Override
  public CombinationTable generate(TestInput input) {
    LOGGER.debug("Starting new PairwiseAlgorithm generation for TestInput: {}", input);
    List<TestParameter> parameters = input.getTestParameters();

    if (parameters == null) {
      LOGGER.error(
          "Input getTestParameters() returned null. This should not happen. Returning empty table.");
      return new CombinationTable(new ArrayList<>());
    }
    LOGGER.trace("Received {} parameters for generation.", parameters.size());

    if (parameters.size() < 2) {
      if (parameters.size() == 1) {
        LOGGER.debug(
            "Only one parameter present. Generating one combination for each of its partitions.");
        List<Combination> singleParamCombinations = new ArrayList<>();
        TestParameter singleParam = parameters.get(0);
        for (EquivalencePartition partition : singleParam.getPartitions()) {
          Combination combo = new Combination(parameters);
          combo.setValue(0, partition);
          singleParamCombinations.add(combo);
        }
        return new CombinationTable(singleParamCombinations);
      }
      LOGGER.debug(
          "Less than 2 parameters for pairwise generation ({}). Returning empty table.",
          parameters.size());
      return new CombinationTable(new ArrayList<>());
    }

    Set<ValuePair> allValidPairs = generateAllValidValuePairs(parameters);
    if (allValidPairs.isEmpty()) {
      LOGGER.warn(
          "No valid pairs to cover were found after generateAllValidValuePairs. Returning empty table.");
      return new CombinationTable(new ArrayList<>());
    }
    LOGGER.debug("Total valid pairs to cover determined: {}", allValidPairs.size());
    if (LOGGER.isTraceEnabled()) {
      allValidPairs.forEach(p -> LOGGER.trace("  Pair to cover: {}", p));
    }

    List<Combination> resultingCombinations = new ArrayList<>();
    Set<ValuePair> coveredPairs = new HashSet<>();
    int previousCoveredPairsCount = -1; // To detect stalls
    int iteration = 0;

    // --- Primary Pass (Greedy Heuristic) ---
    LOGGER.debug("Starting Primary Pass (Greedy Heuristic)");
    while (coveredPairs.size() < allValidPairs.size()) {
      iteration++;
      LOGGER.trace(
          "Primary Pass - Iteration: {}. Covered pairs: {}/{}",
          iteration,
          coveredPairs.size(),
          allValidPairs.size());
      if (Thread.currentThread().isInterrupted()) {
        LOGGER.warn(
            "Pairwise generation thread interrupted in primary pass. Proceeding to gap-filling if needed.");
        break;
      }

      if (coveredPairs.size() == previousCoveredPairsCount) {
        LOGGER.warn(
            "Primary Pass - No new pairs covered in the last iteration ({}). Breaking. Covered: {}, Total: {}",
            iteration,
            coveredPairs.size(),
            allValidPairs.size());
        logUncoveredPairs("Primary Pass", allValidPairs, coveredPairs);
        break;
      }
      previousCoveredPairsCount = coveredPairs.size();

      LOGGER.trace("Primary Pass - Calling buildNextCombination for iteration {}.", iteration);
      Combination nextCombination = buildNextCombination(parameters, allValidPairs, coveredPairs);

      if (nextCombination != null) {
        LOGGER.trace("Primary Pass - buildNextCombination returned: {}", nextCombination);
        if (!isValidCombination(nextCombination)) {
          LOGGER.error(
              "CRITICAL: Primary Pass - buildNextCombination produced an invalid combination: {}. This should not happen. Stopping primary pass.",
              nextCombination);
          break;
        }
        resultingCombinations.add(nextCombination);
        int newlyCoveredCount =
            updateCoveredPairs(nextCombination, allValidPairs, coveredPairs, parameters);
        LOGGER.debug(
            "Primary Pass - Iteration {}: Added combination: {}. Newly covered: {}. Total covered: {}/{}. Combinations so far: {}",
            iteration,
            nextCombination.getKey(), // Using getKey for concise logging
            newlyCoveredCount,
            coveredPairs.size(),
            allValidPairs.size(),
            resultingCombinations.size());
      } else {
        LOGGER.warn(
            "Primary Pass - buildNextCombination returned null in iteration {}. No new combination could be formed. Stopping primary pass.",
            iteration);
        if (coveredPairs.size() < allValidPairs.size()) {
          logUncoveredPairs("Primary Pass", allValidPairs, coveredPairs);
        }
        break;
      }
    }
    LOGGER.debug(
        "Primary Pass finished. Covered {}/{} pairs. Generated {} combinations.",
        coveredPairs.size(),
        allValidPairs.size(),
        resultingCombinations.size());

    // --- Secondary Pass (Gap Filling) ---
    if (coveredPairs.size() < allValidPairs.size()) {
      LOGGER.debug(
          "Starting Secondary Pass (Gap Filling) for {} remaining uncovered pairs.",
          allValidPairs.size() - coveredPairs.size());
      Set<ValuePair> remainingUncoveredPairs = new HashSet<>(allValidPairs);
      remainingUncoveredPairs.removeAll(coveredPairs);
      int gapFillIteration = 0;
      for (ValuePair targetPair : remainingUncoveredPairs) {
        gapFillIteration++;
        if (coveredPairs.contains(targetPair)) {
          LOGGER.trace(
              "Secondary Pass - Target pair {} already covered (likely by a previous gap-fill). Skipping.",
              targetPair);
          continue;
        }
        LOGGER.trace(
            "Secondary Pass - Iteration {}: Attempting to fill gap for pair: {}",
            gapFillIteration,
            targetPair);
        Combination gapCombination =
            buildCombinationForSpecificPair(targetPair, parameters, allValidPairs, coveredPairs);

        if (gapCombination != null) {
          boolean alreadyExists = false;
          for (Combination existingCombination : resultingCombinations) {
            if (existingCombination.equals(gapCombination)) {
              alreadyExists = true;
              break;
            }
          }

          if (alreadyExists) {
            LOGGER.trace(
                "Secondary Pass - Generated gap combination {} already exists. Skipping.",
                gapCombination.getKey());
          } else {
            resultingCombinations.add(gapCombination);
            LOGGER.debug(
                "Secondary Pass - Iteration {}: Added gap-fill combination for {}: {}. Combinations so far: {}",
                gapFillIteration,
                targetPair,
                gapCombination.getKey(), // Using getKey for concise logging
                resultingCombinations.size());
            updateCoveredPairs(gapCombination, allValidPairs, coveredPairs, parameters);
          }
        } else {
          LOGGER.warn(
              "Secondary Pass - Iteration {}: Failed to build gap-fill combination for pair: {}.",
              gapFillIteration,
              targetPair);
        }
        if (Thread.currentThread().isInterrupted()) {
          LOGGER.warn(
              "Pairwise generation thread interrupted in secondary pass. Returning current results.");
          break;
        }
      }
      LOGGER.debug(
          "Secondary Pass (Gap Filling) finished. Attempted to fill {} gaps.",
          remainingUncoveredPairs.size());
    }

    if (coveredPairs.size() < allValidPairs.size()) {
      LOGGER.warn(
          "Overall generation: {} pairs still uncovered out of {} after {} primary iterations and gap-filling attempts.",
          allValidPairs.size() - coveredPairs.size(),
          allValidPairs.size(),
          iteration);
      logUncoveredPairs("Overall", allValidPairs, coveredPairs);
    } else {
      LOGGER.debug(
          "Overall generation: All {} pairs covered successfully. Primary iterations: {}. Combinations: {}.",
          allValidPairs.size(),
          iteration,
          resultingCombinations.size());
    }

    // LOGGER.info(  // This is a duplicate of the one above essentially
    //     "PairwiseAlgorithm generation finished. Generated {} combinations in total.",
    //     resultingCombinations.size());
    return new CombinationTable(resultingCombinations);
  }

  private Combination buildCombinationForSpecificPair(
      ValuePair targetPair,
      List<TestParameter> parameters,
      Set<ValuePair> allValidPairs,
      Set<ValuePair> alreadyCoveredPairs) {
    LOGGER.trace(
        "Attempting to build a specific combination for target pair: {}", targetPair.toString());

    Combination newCombination = new Combination(parameters);

    TestParameter P1 = targetPair.param1;
    EquivalencePartition V1 = targetPair.value1;
    TestParameter P2 = targetPair.param2;
    EquivalencePartition V2 = targetPair.value2;

    int p1Index = parameters.indexOf(P1);
    int p2Index = parameters.indexOf(P2);

    if (p1Index == -1 || p2Index == -1) {
      LOGGER.error(
          "Error in buildCombinationForSpecificPair: Target pair parameters not found in main list. P1: {}, P2: {}",
          P1.getName(),
          P2.getName());
      return null;
    }

    // LOGGER.debug("Setting P1 ({}) to {} at index {}", P1.getName(), V1.getName(), p1Index);
    newCombination.setValue(p1Index, V1);
    // LOGGER.debug("Setting P2 ({}) to {} at index {}", P2.getName(), V2.getName(), p2Index);
    newCombination.setValue(p2Index, V2);

    // Fill remaining parameters
    for (int i = 0; i < parameters.size(); i++) {
      if (i == p1Index || i == p2Index) {
        continue; // Skip parameters already set by the target pair
      }
      TestParameter currentParam = parameters.get(i);
      boolean valueSet = false;
      for (EquivalencePartition potentialValue : currentParam.getPartitions()) {
        // LOGGER.trace(
        //     "  Trying to set param {} with value {}", currentParam.getName(),
        // potentialValue.getName());
        newCombination.setValue(i, potentialValue);
        if (isValidCombination(newCombination)) {
          // LOGGER.trace(
          //     "    Value {} for param {} is valid with current combination.",
          //     potentialValue.getName(),
          //     currentParam.getName());
          valueSet = true;
          break;
        } else {
          // LOGGER.trace(
          //     "    Value {} for param {} is NOT valid with current combination. Clearing and
          // trying next.",
          //     potentialValue.getName(),
          //     currentParam.getName());
          newCombination.setValue(i, null); // Backtrack/clear
        }
      }
      if (!valueSet) {
        LOGGER.warn(
            "buildCombinationForSpecificPair: Could not find a valid value for parameter '{}' to complete combination for {}. Returning null.",
            currentParam.getName(),
            targetPair);
        // logCurrentCombinationState(newCombination);
        return null; // Cannot complete combination
      }
    }

    LOGGER.trace("Successfully built specific combination: {}", newCombination.getKey());
    return newCombination;
  }

  private Combination buildNextCombination(
      List<TestParameter> parameters,
      Set<ValuePair> allValidPairs,
      Set<ValuePair> alreadyCoveredPairs) {
    LOGGER.trace(
        "Building next combination. Total valid pairs: {}, Already covered: {}",
        allValidPairs.size(),
        alreadyCoveredPairs.size());

    Combination bestOverallCandidateCombination = null;
    int maxOverallNewPairs = -1;

    // Iterate through each parameter to choose it as the 'starting point'
    for (int firstParamIndex = 0; firstParamIndex < parameters.size(); firstParamIndex++) {
      TestParameter paramToSetAsFirst = parameters.get(firstParamIndex);
      LOGGER.trace("  Trying parameter '{}' as the first to set.", paramToSetAsFirst.getName());

      for (EquivalencePartition currentSeedValue : paramToSetAsFirst.getPartitions()) {
        LOGGER.trace(
            "    Trying seed value '{}' for parameter '{}'",
            currentSeedValue.getName(),
            paramToSetAsFirst.getName());

        Combination currentCandidateCombination = new Combination(parameters);
        currentCandidateCombination.setValue(firstParamIndex, currentSeedValue);

        // Now, fill the remaining parameters for this seed
        Combination filledCombination =
            fillRemainingParametersGreedily(
                currentCandidateCombination,
                parameters,
                allValidPairs,
                alreadyCoveredPairs,
                firstParamIndex);

        if (filledCombination != null) {
          if (!isValidCombination(filledCombination)) {
            LOGGER.warn(
                "buildNextCombination: filledCombination {} from seed {}:{} was invalid. Skipping.",
                filledCombination.getKey(),
                paramToSetAsFirst.getName(),
                currentSeedValue.getName());
            continue;
          }

          int candidateScore =
              countNewlyCoveredPairs(filledCombination, allValidPairs, alreadyCoveredPairs);
          LOGGER.trace(
              "    Seed {}:{} -> Filled: {} -> Score (new pairs): {}",
              paramToSetAsFirst.getName(),
              currentSeedValue.getName(),
              filledCombination.getKey(),
              candidateScore);

          if (candidateScore > maxOverallNewPairs) {
            maxOverallNewPairs = candidateScore;
            bestOverallCandidateCombination = filledCombination;
            LOGGER.trace(
                "      New best overall candidate from seed {}:{} -> {}, score {}",
                paramToSetAsFirst.getName(),
                currentSeedValue.getName(),
                bestOverallCandidateCombination.getKey(),
                maxOverallNewPairs);
          } else if (candidateScore == maxOverallNewPairs
              && bestOverallCandidateCombination != null) {
            // Deterministic tie-breaking
            if (filledCombination.getKey().compareTo(bestOverallCandidateCombination.getKey())
                < 0) {
              bestOverallCandidateCombination = filledCombination;
              LOGGER.trace(
                  "      Tie-break (alphabetical) from seed {}:{} -> {}, score {}",
                  paramToSetAsFirst.getName(),
                  currentSeedValue.getName(),
                  bestOverallCandidateCombination.getKey(),
                  maxOverallNewPairs);
            }
          }
        } else {
          LOGGER.trace(
              "    Seed {}:{} -> fillRemainingParametersGreedily returned null.",
              paramToSetAsFirst.getName(),
              currentSeedValue.getName());
        }
      }
    } // End of loop for paramToSetAsFirst

    LOGGER.trace(
        "Finished evaluating all seeds and their filled versions. Best overall candidate: {}, covers {} new pairs.",
        bestOverallCandidateCombination != null ? bestOverallCandidateCombination.getKey() : "null",
        maxOverallNewPairs);

    if (maxOverallNewPairs > 0) {
      LOGGER.debug(
          "Best overall candidate combination {} covers {} new pairs. Returning it.",
          bestOverallCandidateCombination.getKey(),
          maxOverallNewPairs);
      return bestOverallCandidateCombination;
    } else {
      LOGGER.debug(
          "No candidate combination found that covers any new pairs (maxOverallNewPairs = {}). Returning null.",
          maxOverallNewPairs);
      return null;
    }
  }

  // Helper method to fill remaining parameters for a given partial combination
  private Combination fillRemainingParametersGreedily(
      Combination partialCombination,
      List<TestParameter> parameters,
      Set<ValuePair> allValidPairs,
      Set<ValuePair> alreadyCoveredPairs,
      int firstParamIndexToSkip) { // Index of the parameter already set in partialCombination

    Combination currentWorkingCombination =
        new Combination(partialCombination); // Start with a copy

    for (int i = 0; i < parameters.size(); i++) {
      if (i == firstParamIndexToSkip || currentWorkingCombination.getValue(i) != null) {
        // Skip the initially set parameter or any parameter that might have been set by a previous
        // iteration (though logic implies only firstParamIndexToSkip is initially set)
        continue;
      }

      TestParameter currentParamToFill = parameters.get(i);
      EquivalencePartition bestValueForThisParam = null;
      int bestScoreForThisParam = -1;
      List<EquivalencePartition> candidateValues = new ArrayList<>();

      LOGGER.trace("    fillRemaining: Trying to fill param '{}'", currentParamToFill.getName());
      for (EquivalencePartition val : currentParamToFill.getPartitions()) {
        currentWorkingCombination.setValue(i, val); // Tentatively set value
        if (isValidCombination(currentWorkingCombination)) {
          int score =
              countNewlyCoveredPairs(currentWorkingCombination, allValidPairs, alreadyCoveredPairs);
          LOGGER.trace(
              "      fillRemaining: Param '{}', Value '{}', Score: {}",
              currentParamToFill.getName(),
              val.getName(),
              score);
          if (score > bestScoreForThisParam) {
            bestScoreForThisParam = score;
            candidateValues.clear();
            candidateValues.add(val);
          } else if (score == bestScoreForThisParam) {
            candidateValues.add(val);
          }
        } else {
          LOGGER.trace(
              "      fillRemaining: Param '{}', Value '{}' makes combination invalid. Current: {}",
              currentParamToFill.getName(),
              val.getName(),
              currentWorkingCombination.getKey());
        }
        currentWorkingCombination.setValue(i, null); // Backtrack
      }

      if (!candidateValues.isEmpty()) {
        if (candidateValues.size() == 1) {
          bestValueForThisParam = candidateValues.get(0);
        } else {
          candidateValues.sort(
              (v1, v2) -> v1.getName().compareTo(v2.getName())); // Deterministic tie-break
          bestValueForThisParam = candidateValues.get(0);
        }
        currentWorkingCombination.setValue(i, bestValueForThisParam);
        LOGGER.trace(
            "    fillRemaining: Set param '{}' to '{}'",
            currentParamToFill.getName(),
            bestValueForThisParam.getName());
      } else {
        LOGGER.warn(
            "    fillRemaining: Could not find a suitable value for param '{}' that is valid and covers new pairs. Combination might be incomplete or sub-optimal.",
            currentParamToFill.getName());
        // If we can't find a value that covers new pairs, we might still need to pick a valid one
        // to complete the combination. For now, this might lead to returning null if not all params
        // are filled.
        // Or, try to pick any valid value even if score is 0.
        // Let's try to pick the first valid value if no scoring candidates were found.
        for (EquivalencePartition val : currentParamToFill.getPartitions()) {
          currentWorkingCombination.setValue(i, val);
          if (isValidCombination(currentWorkingCombination)) {
            bestValueForThisParam = val;
            currentWorkingCombination.setValue(i, bestValueForThisParam);
            LOGGER.trace(
                "    fillRemaining: (Fallback) Set param '{}' to first valid value '{}'",
                currentParamToFill.getName(),
                bestValueForThisParam.getName());
            break;
          }
          currentWorkingCombination.setValue(i, null); // Backtrack
        }
        if (bestValueForThisParam == null) {
          LOGGER.error(
              "    fillRemaining: CRITICAL - Could not find ANY valid value for param '{}'. Returning null for filledCombination.",
              currentParamToFill.getName());
          return null; // Cannot complete the combination
        }
      }
    }
    // Ensure all parameters are filled
    for (int i = 0; i < parameters.size(); i++) {
      if (currentWorkingCombination.getValue(i) == null) {
        LOGGER.error(
            "fillRemainingParametersGreedily: Failed to fill all parameters. Param '{}' is null. Returning null.",
            parameters.get(i).getName());
        return null;
      }
    }
    return currentWorkingCombination;
  }

  // Helper to count newly covered pairs for a given combination
  private int countNewlyCoveredPairs(
      Combination combination, Set<ValuePair> allValidPairs, Set<ValuePair> alreadyCoveredPairs) {
    if (combination == null) return 0;
    int count = 0;
    // Temporarily get parameters from the combination itself if it's complete
    // This assumes combination.getParameters() gives the original list order/content
    List<TestParameter> paramsInCombo = combination.getParameters();
    if (paramsInCombo == null || paramsInCombo.isEmpty()) {
      // Fallback or error - this should ideally not happen if combination is well-formed
      // For safety, could pass 'parameters' (the main list) if combo.getParameters is unreliable
      LOGGER.warn("countNewlyCoveredPairs: Combination has no parameters. Cannot count pairs.");
      return 0;
    }

    for (int i = 0; i < paramsInCombo.size(); i++) {
      for (int j = i + 1; j < paramsInCombo.size(); j++) {
        EquivalencePartition v1 = combination.getValue(i);
        EquivalencePartition v2 = combination.getValue(j);
        TestParameter p1 = paramsInCombo.get(i);
        TestParameter p2 = paramsInCombo.get(j);

        if (v1 != null && v2 != null) {
          // Check if this pair (p1,v1)-(p2,v2) is a valid pair to be covered AND not already
          // covered
          ValuePair currentPair =
              new ValuePair(p1, v1, p2, v2); // ValuePair constructor handles canonical order
          if (allValidPairs.contains(currentPair) && !alreadyCoveredPairs.contains(currentPair)) {
            count++;
          }
        }
      }
    }
    return count;
  }

  private int updateCoveredPairs(
      Combination combination,
      Set<ValuePair> allValidPairs, // For efficient lookup
      Set<ValuePair> coveredPairs, // To be updated
      List<TestParameter> parameters) {
    int newlyCovered = 0;
    LOGGER.trace("Updating covered pairs for combination: {}", combination.getKey());
    Set<ValuePair> pairsInCombination =
        getPairsInCombination(combination, parameters, allValidPairs);
    for (ValuePair pair : pairsInCombination) {
      if (coveredPairs.add(pair)) {
        newlyCovered++;
        LOGGER.trace("  Newly covered pair: {}", pair);
      }
    }
    LOGGER.trace("  Total newly covered by this combination: {}", newlyCovered);
    return newlyCovered;
  }

  private void logUncoveredPairs(
      String context, Set<ValuePair> allValidPairs, Set<ValuePair> coveredPairs) {
    if (LOGGER.isWarnEnabled()) {
      Set<ValuePair> uncovered = new HashSet<>(allValidPairs);
      uncovered.removeAll(coveredPairs);
      if (!uncovered.isEmpty()) {
        LOGGER.warn("Context [{}]: {} Uncovered Pairs:", context, uncovered.size());
        // uncovered.forEach(p -> LOGGER.warn("  - {}", p));  // Can be too verbose
        if (uncovered.size() < 20) { // Log details only if not too many
          uncovered.forEach(p -> LOGGER.warn("  - {}", p));
        } else {
          LOGGER.warn(
              "  (Too many uncovered pairs to list individually - {} pairs)", uncovered.size());
        }
      }
    }
  }

  /** Represents a pair of (parameter, value) from a combination. */
  private static class ValuePair {
    final TestParameter param1;
    final EquivalencePartition value1;
    final TestParameter param2;
    final EquivalencePartition value2;

    public ValuePair(
        TestParameter p1, EquivalencePartition v1, TestParameter p2, EquivalencePartition v2) {
      if (p1.getName().compareTo(p2.getName()) > 0
          || (p1.getName().equals(p2.getName()) && v1.getName().compareTo(v2.getName()) > 0)) {
        this.param1 = p2;
        this.value1 = v2;
        this.param2 = p1;
        this.value2 = v1;
      } else {
        this.param1 = p1;
        this.value1 = v1;
        this.param2 = p2;
        this.value2 = v2;
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ValuePair valuePair = (ValuePair) o;
      // Relies on canonical ordering in constructor
      return Objects.equals(param1.getName(), valuePair.param1.getName())
          && Objects.equals(value1.getName(), valuePair.value1.getName())
          && Objects.equals(param2.getName(), valuePair.param2.getName())
          && Objects.equals(value2.getName(), valuePair.value2.getName());
    }

    @Override
    public int hashCode() {
      // Relies on canonical ordering in constructor
      return Objects.hash(param1.getName(), value1.getName(), param2.getName(), value2.getName());
    }

    @Override
    public String toString() {
      return String.format(
          "Pair[(%s:%s) - (%s:%s)]",
          param1.getName(), value1.getName(), param2.getName(), value2.getName());
    }
  }

  private Set<ValuePair> generateAllValidValuePairs(List<TestParameter> parameters) {
    Set<ValuePair> allPairs = new HashSet<>();
    LOGGER.debug("Generating all valid value pairs for {} parameters.", parameters.size());
    for (int i = 0; i < parameters.size(); i++) {
      for (int j = i + 1; j < parameters.size(); j++) {
        TestParameter p1 = parameters.get(i);
        TestParameter p2 = parameters.get(j);
        LOGGER.trace("  Considering parameter pair: ({}, {})", p1.getName(), p2.getName());
        for (EquivalencePartition v1 : p1.getPartitions()) {
          for (EquivalencePartition v2 : p2.getPartitions()) {
            if (areValuesCompatible(p1, v1, p2, v2)) {
              ValuePair pair = new ValuePair(p1, v1, p2, v2);
              allPairs.add(pair);
              LOGGER.trace("    Added valid pair: {}", pair);
            } else {
              LOGGER.trace(
                  "    Skipped invalid pair ({}:{}, {}:{}} due to areValuesCompatible=false",
                  p1.getName(),
                  v1.getName(),
                  p2.getName(),
                  v2.getName());
            }
          }
        }
      }
    }
    LOGGER.debug("Generated {} unique valid value pairs.", allPairs.size());
    return allPairs;
  }

  private boolean areValuesCompatible(
      TestParameter p1, EquivalencePartition v1, TestParameter p2, EquivalencePartition v2) {
    // Check compatibility from p1's perspective and p2's perspective
    // This is crucial for handling rules that might only be defined on one parameter
    // but should apply symmetrically or when GenerationAlgorithm.isValidCombination checks it.

    LOGGER.trace(
        "areValuesCompatible check between P1='{}' V1='{}' and P2='{}' V2='{}'",
        p1.getName(),
        v1.getName(),
        p2.getName(),
        v2.getName());

    boolean p1Compatible = p1.areCompatible(v1, v2); // v1 is from p1, v2 is from p2
    LOGGER.trace("  p1.areCompatible(v1, v2) = {}", p1Compatible);
    if (!p1Compatible) {
      LOGGER.trace("  Result: Incompatible from p1's perspective.");
      return false;
    }

    // For p2.areCompatible, the arguments must be (value_from_p2, value_from_p1)
    boolean p2Compatible = p2.areCompatible(v2, v1);
    LOGGER.trace("  p2.areCompatible(v2, v1) = {}", p2Compatible);
    if (!p2Compatible) {
      LOGGER.trace("  Result: Incompatible from p2's perspective.");
      return false;
    }

    LOGGER.trace("  Result: Compatible from both perspectives.");
    return true;
  }

  // Get all unique pairs present in a given combination.
  // Ensures pairs are valid by checking against allValidPairsSet.
  private Set<ValuePair> getPairsInCombination(
      Combination combination, List<TestParameter> allParams, Set<ValuePair> allValidPairsSet) {
    Set<ValuePair> pairs = new HashSet<>();
    for (int i = 0; i < allParams.size(); i++) {
      for (int j = i + 1; j < allParams.size(); j++) {
        TestParameter p1 = allParams.get(i);
        TestParameter p2 = allParams.get(j);
        EquivalencePartition v1 = combination.getValue(i);
        EquivalencePartition v2 = combination.getValue(j);

        if (v1 != null && v2 != null) {
          // Create a temporary ValuePair to check against the canonical ones in allValidPairsSet
          ValuePair tempPair = new ValuePair(p1, v1, p2, v2);
          // It's important that allValidPairsSet contains pairs in canonical form.
          if (allValidPairsSet.contains(tempPair)) {
            pairs.add(tempPair);
          } else {
            // This case should ideally not happen if the combination itself is deemed valid
            // by isValidCombination, which relies on areValuesCompatible, which in turn
            // should align with generateAllValidValuePairs.
            // However, if it does, it means a pair was formed that wasn't in the initial
            // allValidPairsSet, which could indicate an issue or a very sparse rule set.
            // LOGGER.warn(
            //     "  Pair ({}:{}, {}:{}) from combination not found in allValidPairsSet. This is
            // unexpected if combination is valid.",
            //     p1.getName(),
            //     v1.getName(),
            //     p2.getName(),
            //     v2.getName());
          }
        }
      }
    }
    return pairs;
  }
}
