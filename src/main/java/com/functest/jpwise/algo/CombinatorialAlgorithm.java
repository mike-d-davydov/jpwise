package com.functest.jpwise.algo;

import com.functest.jpwise.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DavydovMD
 * Date: 19.06.13
 * Time: 17:57
 */
public class CombinatorialAlgorithm extends GenerationAlgorithm {


    public CombinatorialAlgorithm() {
        super();

    }

    private int getParameterIndex(ParameterValue value) {
        return input().getTestParameters().indexOf(value.getParentParameter());
    }


    @Override
    public void generate(TestGenerator testGenerator, int nwise) {

        _Pairwise_generator = testGenerator;

        List<Combination> possiblyIncompleteResult, moreCompleteResult = new ArrayList<>();
        moreCompleteResult.add(new Combination(input().size()));

        for (TestParameter parameter : input().getTestParameters()) {
            possiblyIncompleteResult = new ArrayList<>(moreCompleteResult);
            for (Combination possiblyIncompleteCombination : possiblyIncompleteResult)
                for (ParameterValue paramValue : parameter.getValues()) {
                    moreCompleteResult.remove(possiblyIncompleteCombination);
                    final Combination moreCompleteCombination = new Combination(input().size()).merge(possiblyIncompleteCombination);
                    moreCompleteCombination.setValue(getParameterIndex(paramValue), paramValue);
                    moreCompleteResult.add(moreCompleteCombination);
                }
        }


        moreCompleteResult.removeIf(c -> !c.checkNoConflicts(this));

        for (Combination testCase : moreCompleteResult) {
            addToResult(testCase);
        }
    }
}
