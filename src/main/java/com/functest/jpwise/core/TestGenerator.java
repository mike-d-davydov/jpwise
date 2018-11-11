/**
 * Copyright (c) 2010  Ng Pan Wei, 2013 Mikhail Davydov
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

package com.functest.jpwise.core;


/**
 * Combination generation algorithm to generate N-wise.
 * Currently support only pair-wise.
 *
 * @author panwei
 */
public class TestGenerator {
    /**
     * Combinations generated.
     */
    protected CombinationTable _result;
    /**
     * TestInput from which combinations will be generated.
     */
    private TestInput _testInput;

    /**
     * Constructor ;
     *
     * @param theTestInput
     */
    public TestGenerator(TestInput theTestInput) {
        _testInput = theTestInput;
        _result = new CombinationTable();
    }

    /**
     * Delegate generation to algorithm.
     *
     * @param nwise
     */
    public void generate(GenerationAlgorithm algorithm, int nwise) {
        algorithm.generate(this, nwise);
    }

    /**
     * Delegate generation to algorithm.
     */
    public void generate(GenerationAlgorithm algorithm) {
        algorithm.generate(this, 2);
    }


    /**
     * Get the domain.
     *
     * @return
     */
    public TestInput input() {
        return _testInput;
    }

    /**
     * Get the result.
     *
     * @return
     */
    public CombinationTable result() {
        return _result;
    }

    /**
     * Compute the span of the domain.
     *
     * @return
     */
    public int span() {
        int size = 0;
        for (int i = 0; i < _testInput.size(); i++) {
            for (int j = i + 1; j < _testInput.size(); j++) {
                size += _testInput.get(i).getValues().size()
                        * _testInput.get(j).getValues().size();
            }
        }
        return size;
    }


}
