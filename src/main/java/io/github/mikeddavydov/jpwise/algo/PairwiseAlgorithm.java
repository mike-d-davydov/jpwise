package io.github.mikeddavydov.jpwise.algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mikeddavydov.jpwise.core.Combination;
import io.github.mikeddavydov.jpwise.core.EquivalencePartition;
import io.github.mikeddavydov.jpwise.core.GenerationAlgorithm;
import io.github.mikeddavydov.jpwise.core.TestGenerator;
import io.github.mikeddavydov.jpwise.core.TestParameter;

/**
 * Implements the pairwise (2-wise) test case generation algorithm. This
 * algorithm generates a
 * minimal set of test cases that covers all possible pairs of parameter values
 * while respecting
 * compatibility rules.
 *
 * <p>
 * The algorithm works in two main phases:
 *
 * <ol>
 * <li>Generate all possible pairs of input values (equivalence partitions)
 * <li>Build complete test cases by combining compatible partitions and getting
 * their values
 * </ol>
 *
 * <p>
 * The algorithm uses a tunable "jump" parameter that affects how pairs are
 * selected from the
 * queue. A larger jump value may result in fewer test cases but takes longer to
 * compute.
 *
 * <p>
 * Example usage:
 *
 * <pre>
 * TestInput input = new TestInput();
 * input.add(new TestParameter("browser", browserPartitions));
 * input.add(new TestParameter("os", osPartitions));
 *
 * TestGenerator generator = new TestGenerator(input);
 * generator.generate(new PairwiseAlgorithm(3)); // jump = 3
 * CombinationTable results = generator.result();
 * </pre>
 *
 * @author davydovmd
 * @see GenerationAlgorithm
 * @see TestGenerator
 */
public class PairwiseAlgorithm extends GenerationAlgorithm {
  private static final Logger logger = LoggerFactory.getLogger(PairwiseAlgorithm.class);
  private final Random random = new Random();

  /** Flag value to indicate that a combination pair has been generated. */
  private static final int COMPLETED = 1;

  /** Flag value to indicate that a combination pair has NOT been generated. */
  private static final int PENDING = 0;

  /** Map tracking which parameter value pairs have been covered. */
  private final Map<String, Integer> combinationMap = new HashMap<>();

  /** Queue of parameter value pairs that still need to be covered. */
  private final List<Combination> combinationQueue = new ArrayList<>();

  /** The jump parameter controls how pairs are selected from the queue. */
  private final int jump;

  /**
   * Creates a new pairwise algorithm instance with the specified jump value.
   *
   * @param jump The jump value for pair selection (recommended values: 2-5)
   */
  public PairwiseAlgorithm(int jump) {
    super();
    this.jump = jump;
    logger.debug("Created new PairwiseAlgorithm with jump value {}", jump);
  }

  /** Creates a new pairwise algorithm instance with a default jump value of 3. */
  public PairwiseAlgorithm() {
    this(3);
  }

  /**
   * Generates a set of test cases that cover all possible pairs of parameter
   * values. The algorithm
   * first generates all possible pairs, then builds complete test cases by
   * combining compatible
   * pairs.
   *
   * @param testGenerator The test generator containing input parameters
   * @param nwise         The degree of combinations (ignored for pairwise, always
   *                      uses 2)
   */
  @Override
  public void generate(TestGenerator testGenerator, int nwise) {
    logger.info("Starting pairwise test generation with jump value {}", jump);
    pwGenerator = testGenerator;

    // Generate all possible pairs
    generatePartialCombinations();
    logger.info("Generated {} pairs to cover", combinationQueue.size());

    // Build complete test cases
    int initialQueueSize = combinationQueue.size();
    while (!combinationQueue.isEmpty()) {
      Combination entry = buildCombination();
      if (entry != null) {
        pwGenerator.result().add(new Combination(entry));
        logger.debug("Progress: {}/{} pairs covered, queue size: {}",
            initialQueueSize - combinationQueue.size(),
            initialQueueSize,
            combinationQueue.size());
      }
    }

    logger.info("Completed pairwise test generation with {} test cases",
        pwGenerator.result().size());
  }

  /**
   * Generates all possible pairs of parameter values that need to be covered.
   * This is the first
   * phase of the algorithm where we identify all pairs that must appear in at
   * least one test case.
   */
  private void generatePartialCombinations() {
    int size = pwGenerator.input().size();
    logger.debug("Generating partial combinations for {} parameters", size);

    for (int i = 0; i < size; i++) {
      for (int j = i + 1; j < size; j++) {
        generatePairs(i, j);
      }
    }
  }

  /**
   * Generates all possible pairs between values of two parameters. Only
   * compatible pairs are added
   * to the queue.
   *
   * @param i Index of the first parameter
   * @param j Index of the second parameter
   */
  private void generatePairs(int i, int j) {
    TestParameter param1 = pwGenerator.input().get(i);
    TestParameter param2 = pwGenerator.input().get(j);
    logger.debug("Generating pairs between parameters {} and {}",
        param1.getName(), param2.getName());

    for (EquivalencePartition ep1 : param1.getPartitions()) {
      for (EquivalencePartition ep2 : param2.getPartitions()) {
        if (ep1.isCompatibleWith(ep2)) {
          Combination pair = new Combination(pwGenerator.input().size());
          pair.setValue(i, ep1);
          pair.setValue(j, ep2);
          String key = pair.getKey();
          if (combinationMap.get(key) == null) {
            combinationMap.put(key, PENDING);
            combinationQueue.add(new Combination(pair));
            logger.trace("Added pair: {} - {}", ep1.getName(), ep2.getName());
          }
        } else {
          logger.trace("Skipping incompatible pair: {} - {}", ep1.getName(), ep2.getName());
        }
      }
    }
  }

  /**
   * Builds a complete test case by starting with a pair from the queue and adding
   * compatible values
   * for other parameters.
   *
   * @return A complete test case combination, or null if no valid combination
   *         could be built
   */
  private Combination buildCombination() {
    if (combinationQueue.isEmpty()) {
      return null;
    }

    int index = 0;
    Combination result = null;
    int queueSize = combinationQueue.size();

    // Try different pairs from the queue to find one that can be completed
    while ((index < queueSize) && (result == null)) {
      Combination entry = combinationQueue.get(index);
      if (entry == null) {
        index = (index + jump) % queueSize;
        continue;
      }

      // Skip incompatible combinations
      if (!entry.checkNoConflicts(this)) {
        logger.trace("Skipping incompatible combination at index {}", index);
        index = (index + jump) % queueSize;
        continue;
      }

      // Try to complete this combination
      result = new Combination(entry);
      completeCombination(result);

      // If we couldn't complete it, try the next one
      if (!result.isFilled()) {
        result = null;
        index = (index + jump) % queueSize;
      }
    }

    return result;
  }

  /**
   * Completes a partial combination by adding compatible values (equivalence
   * partitions) for all
   * parameters. This method tries to find values that are compatible with all
   * values (partitions)
   * already in the combination.
   *
   * @param combination The partial combination to complete
   */
  private void completeCombination(Combination combination) {
    logger.trace("Completing combination: {}", combination);
    for (int i = 0; i < pwGenerator.input().size(); i++) {
      if (combination.getValue(i) == null) {
        TestParameter parameter = pwGenerator.input().get(i);
        List<EquivalencePartition> partitions = parameter.getPartitions();
        Collections.shuffle(partitions, random);

        for (EquivalencePartition partition : partitions) {
          combination.setValue(i, partition);
          if (combination.checkNoConflicts(this)) {
            logger.trace("Added value {} for parameter {}", partition.getName(), parameter.getName());
            break;
          }
        }
      }
    }
  }

  /**
   * Marks all pairs in a combination as used. This prevents the same pairs from
   * being used again in
   * future test cases.
   *
   * @param combination The combination containing pairs to mark
   */
  private void markUsedCombinations(Combination combination) {
    logger.trace("Marking used combinations for: {}", combination);
    for (int i = 0; i < combination.size(); i++) {
      for (int j = i + 1; j < combination.size(); j++) {
        if (combination.getValue(i) != null && combination.getValue(j) != null) {
          Combination pair = new Combination(combination.size());
          pair.setValue(i, combination.getValue(i));
          pair.setValue(j, combination.getValue(j));
          combinationMap.put(pair.getKey(), COMPLETED);
        }
      }
    }
  }
}
