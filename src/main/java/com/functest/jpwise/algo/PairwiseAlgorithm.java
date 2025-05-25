package com.functest.jpwise.algo;

import com.functest.jpwise.core.*;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implements the pairwise (2-wise) test case generation algorithm.
 * This algorithm generates a minimal set of test cases that covers all possible
 * pairs of parameter values while respecting compatibility rules.
 * 
 * <p>The algorithm works in two main phases:</p>
 * <ol>
 *   <li>Generate all possible pairs of input values (equivalence partitions)</li>
 *   <li>Build complete test cases by combining compatible partitions and getting their values</li>
 * </ol>
 * 
 * <p>The algorithm uses a tunable "jump" parameter that affects how pairs are
 * selected from the queue. A larger jump value may result in fewer test cases
 * but takes longer to compute.</p>
 * 
 * <p>Example usage:</p>
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
 * @author panwei
 * @see GenerationAlgorithm
 * @see TestGenerator
 */
public class PairwiseAlgorithm extends GenerationAlgorithm {
    private final Random random = new Random();
    /**
     * Flag value to indicate that a combination pair has been generated.
     */
    private static final int COMPLETED = 1;
    /**
     * Flag value to indicate that a combination pair has NOT been generated.
     */
    private static final int PENDING = 0;
    private static Logger logger = LoggerFactory.getLogger(PairwiseAlgorithm.class);
    /**
     * Map tracking which parameter value pairs have been covered.
     * The key is a string representation of the pair, and the value is
     * either COMPLETED or PENDING.
     */
    private Map<String, Integer> _combinationMap = new HashMap<>();
    /**
     * Queue of parameter value pairs that still need to be covered.
     * Each combination in the queue represents a pair of values that
     * needs to be included in a test case.
     */
    private List<Combination> _combinationQueue = new ArrayList<Combination>() {
    };

    /**
     * The jump parameter controls how pairs are selected from the queue.
     * A larger value may result in fewer test cases but increases computation time.
     */
    private int _jump;

    /**
     * Creates a new pairwise algorithm instance with the specified jump value.
     *
     * @param jump The jump value for pair selection (recommended values: 2-5)
     */
    public PairwiseAlgorithm(int jump) {
        super();
        _jump = jump;
    }

    /**
     * Creates a new pairwise algorithm instance with a default jump value of 3.
     */
    public PairwiseAlgorithm() {
        this(3);
    }

    /**
     * Generates a set of test cases that cover all possible pairs of parameter values.
     * The algorithm first generates all possible pairs, then builds complete test cases
     * by combining compatible pairs.
     *
     * @param testGenerator The test generator containing input parameters
     * @param nwise The degree of combinations (ignored for pairwise, always uses 2)
     */
    @Override
    public void generate(TestGenerator testGenerator, int nwise) {
        pwGenerator = testGenerator;
        generatePartialCombinations();
        while (!_combinationQueue.isEmpty()) {
            Combination entry = buildCombination();
            logger.trace("Progress result:" + pwGenerator.result().size() + " queue:"
                    + _combinationQueue.size() + " -- " + entry.getKey());
        }
    }

    /**
     * Generates all possible pairs of parameter values that need to be covered.
     * This is the first phase of the algorithm where we identify all pairs
     * that must appear in at least one test case.
     */
    private void generatePartialCombinations() {
        int size = pwGenerator.input().size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                generatePairs(i, j);
            }
        }
    }

    /**
     * Generates all possible pairs between values of two parameters.
     * Only compatible pairs are added to the queue.
     *
     * @param i Index of the first parameter
     * @param j Index of the second parameter
     */
    private void generatePairs(int i, int j) {
        TestInput testInput = pwGenerator.input();
        TestParameter param1 = testInput.get(i);
        TestParameter param2 = testInput.get(j);

        // Get all possible values (equivalence partitions) for both parameters
        List<EquivalencePartition<?>> param1Partitions = new ArrayList<>(param1.getPartitions());
        Collections.shuffle(param1Partitions, random);

        List<EquivalencePartition<?>> param2Partitions = new ArrayList<>(param2.getPartitions());
        Collections.shuffle(param2Partitions, random);

        for (EquivalencePartition<?> v1 : param1Partitions) {
            for (EquivalencePartition<?> v2 : param2Partitions) {
                // Skip incompatible pairs
                if (!isCompatible(v1, v2)) {
                    continue;
                }

                // Create a combination with just this pair
                Combination entry = new Combination(testInput.size());
                entry.setValue(i, v1);
                entry.setValue(j, v2);

                // Verify the combination is valid
                if (entry.checkNoConflicts(this)) {
                    String key = entry.getKey();
                    _combinationMap.put(key, PENDING);
                    _combinationQueue.add(entry);
                }
            }
        }
    }

    /**
     * Builds a complete test case by starting with a pair from the queue
     * and adding compatible values for other parameters.
     *
     * @return A complete test case combination
     */
    private Combination buildCombination() {
        int index = 0;
        Combination result = null;
        int queueSize = _combinationQueue.size();

        // Try different pairs from the queue to find one that can be completed
        while ((index < queueSize) && (result == null)) {
            Combination entry = _combinationQueue.get(index);
            
            // Skip incompatible combinations
            if (!entry.checkNoConflicts(this)) {
                index = index + _jump;
                continue;
            }
            
            result = entry;
            completeCombination(result);
            if (!result.isFilled()) {
                result = null;
            }
            index = index + _jump;
        }

        if (result == null) {
            // If no pair could be completed, try each combination in order
            for (int i = 0; i < _combinationQueue.size(); i++) {
                Combination entry = _combinationQueue.get(i);
                if (entry.checkNoConflicts(this)) {
                    result = entry;
                    completeCombination(result);
                    if (result.isFilled()) {
                        break;
                    }
                }
            }
            
            // If still no valid combination found, something is wrong with the input
            if (result == null || !result.isFilled()) {
                throw new IllegalStateException("Could not find any valid combinations to complete");
            }
        }

        // Remove the used pair and mark it as completed
        _combinationQueue.remove(result);
        markUsedCombinations(result);
        addToResult(result);

        return result;
    }

    /**
     * Completes a partial combination by adding compatible values (equivalence partitions) for all parameters.
     * This method tries to find values that are compatible with all values (partitions) already
     * in the combination.
     *
     * @param combination The partial combination to complete
     */
    @SuppressWarnings("rawtypes")
    private void completeCombination(Combination combination) {
        TestInput input = pwGenerator.input();

        Preconditions.checkArgument(combination.checkNoConflicts(this), 
            "Combination should be initially consistent, with no conflicting values. It is not:" + combination);
        EquivalencePartition[] initial = combination.getValues();

        for (int i = 0; i < input.size(); i++) {
            if (combination.getValue(i) == null) {
                boolean completed = false;
                // Shuffle values for each parameter
                List<EquivalencePartition<?>> shuffledPartitions = new ArrayList<>(input().get(i).getPartitions());
                Collections.shuffle(shuffledPartitions, random);
                for (EquivalencePartition<?> value : shuffledPartitions) {
                    combination.setValue(i, value);
                    if (combination.checkNoConflicts(this)) {
                        completed = true;
                        break;
                    } else {
                        logger.trace(combination + " contains incompatible values (changed value was: " + value + ")?");
                    }
                }

                if (!completed)
                    logger.warn("Failed to find value of parameter " + combination.getValue(i).getParentParameter() + 
                        " compatible with other parameter values in combination " + Arrays.toString(initial));
            }
        }
    }

    /**
     * Marks all pairs in a combination as completed in the combination map.
     * This helps track which pairs have been covered by test cases.
     *
     * @param combination The combination containing pairs to mark as completed
     */
    private void markUsedCombinations(Combination combination) {
        int size = combination.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if ((combination.getValue(i) != null)
                        && (combination.getValue(j) != null)) {
                    Combination entry = new Combination(size);
                    entry.setValue(i, combination.getValue(i));
                    entry.setValue(j, combination.getValue(j));
                    String key = entry.getKey();
                    _combinationMap.put(key, COMPLETED);
                }
            }
        }
    }
}
