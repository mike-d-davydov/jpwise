/**
 * Copyright (c) 2010  Ng Pan Wei, 2013 Mikhail Davydov,  Copyright (c) 2013 Mikhail Davydov
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.functest.jpwise.algo;

import com.functest.jpwise.core.*;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Basic Algorithm to generate combinations.
 *
 * @author panwei
 */
public class PairwiseAlgorithm extends GenerationAlgorithm {
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
     * Map of candidate combination pairs.
     */
    private Map<String, Integer> _combinationMap = new HashMap<>();
    /**
     * List of unprocessed combination pairs.
     */
    private List<Combination> _combinationQueue = new ArrayList<Combination>() {
    };

    /**
     * parameter to tune the selection of unprocessed combination pairs.
     */
    private int _jump;

    /**
     * Constructor.
     */
    public PairwiseAlgorithm(int jump) {
        super();
        _jump = jump;
    }

    public PairwiseAlgorithm() {
        this(3);
    }


    /**
     * Main generation algorithm.
     */
    @Override
    public void generate(TestGenerator testGenerator, int nwise) {
        _Pairwise_generator = testGenerator;
        generatePartialCombinations();
        while (!_combinationQueue.isEmpty()) {
            Combination entry = buildCombination();
            logger.trace("Progress result:" + _Pairwise_generator.result().size() + " queue:"
                    + _combinationQueue.size() + " -- " + entry.getKey());
        }


    }

    /**
     * Generate candidate partial combinations.
     */
    private void generatePartialCombinations() {
        int size = _Pairwise_generator.input().size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                generatePairs(i, j);
            }
        }
    }


    /**
     * Generate partial combination for parameters.
     *
     * @param i
     * @param j
     */
    private void generatePairs(int i, int j) {
        TestInput testInput = _Pairwise_generator.input();
        TestParameter param1 = testInput.get(i);
        TestParameter param2 = testInput.get(j);

        List<ParameterValue> v1values = new ArrayList<>(param1.getValues());
        Collections.shuffle(v1values);

        for (ParameterValue v1 : v1values) {
            List<ParameterValue> v2values = new ArrayList<>(param2.getValues());
            Collections.shuffle(v2values);

            for (ParameterValue v2 : v2values) {
                Combination entry = new Combination(testInput.size());
                if (isCompatible(v1, v2)) {
                    entry.setValue(i, v1);
                    entry.setValue(j, v2);

                    String key = entry.getKey();
                    _combinationMap.put(key, PENDING);
                    _combinationQueue.add(entry);
                }
            }
        }
    }

    /**
     * Build a combination from what is available in the queue.
     *
     * @return
     */
    private Combination buildCombination() {
        int offset = -_jump;
        Combination curCombination = new Combination(input().size());

        List<Combination> toPutBack = new ArrayList<>();

        while (!curCombination.isFilled() && !_combinationQueue.isEmpty()) {
            offset = (offset + _jump) % _combinationQueue.size();
            Combination fromQueue = _combinationQueue.remove(offset);
            Preconditions.checkArgument(fromQueue.checkNoConflicts(this), "Combination in queue is already conflicting!" + fromQueue);

            String key = fromQueue.getKey();
            logger.trace(" - trying: " + key);
            Integer status = _combinationMap.get(key);
            if (status == COMPLETED) {
                logger.trace(" - skipping: " + key);
                continue;
            }

            boolean isConflicted = false;

            Combination mergedCombination = curCombination.merge(fromQueue);
            if (mergedCombination != null) {
                isConflicted = !mergedCombination.checkNoConflicts(this);
            }


            if ((mergedCombination == null) || (isConflicted)) {
                toPutBack.add(fromQueue);
                logger.trace(" - postponing: " + key + ". Merge conflict?" + (mergedCombination == null) + "; Incompatible values?" + isConflicted);
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
        TestInput input = _Pairwise_generator.input();

        Preconditions.checkArgument(combination.checkNoConflicts(this), "Combination should be initially consistent, with no conflicting values. It is not:" + combination);
        ParameterValue[] initial = combination.getValues();

        for (int i = 0; i < input.size(); i++) {
            if (combination.getValue(i) == null) {
                boolean completed = false;
                List<ParameterValue> shuffledValues = new ArrayList<>(input().get(i).getValues());
                Collections.shuffle(shuffledValues);
                for (ParameterValue value : shuffledValues) {
                    combination.setValue(i, value);
                    if (combination.checkNoConflicts(this)) {
                        completed = true;
                        break;
                    } else {
                        logger.trace(combination + " contains incompatible values (changed value was: " + value + ")?");
                    }
                }

                if (!completed)
                    logger.warn("Failed to find value of parameter " + combination.getValue(i).getParentParameter() + " compatible with other parameter values in combination " + Arrays.toString(initial));
                //throw new RuntimeException("Failed to find value of parameter " + combination.getValue(i) + " compatible with other parameter values in combination " + Arrays.toString(initial));

            }
        }

    }


    /**
     * Mark partial combinations that have been used.
     *
     * @param combination
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
