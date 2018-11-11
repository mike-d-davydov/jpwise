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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for the algorithm.
 *
 * @author panwei
 */
public abstract class GenerationAlgorithm {
    private static Logger logger = LoggerFactory.getLogger(GenerationAlgorithm.class);
    /**
     * Reference to the generator.
     */
    protected TestGenerator _Pairwise_generator;

    /**
     * Main method to generate combinations to be overwritten by child classes.
     *
     * @param testGenerator
     * @param nwise
     */
    public abstract void generate(TestGenerator testGenerator, int nwise);

    /**
     * Get the domain from the generator.
     *
     * @return
     */
    protected TestInput input() {
        return _Pairwise_generator.input();
    }

    protected void addToResult(Combination combination) {
        logger.debug("Adding combination:" + combination);
        _Pairwise_generator.result().add(combination);
    }

    protected boolean isCompatible(ParameterValue v1, ParameterValue v2) {
        return v1.isCompatibleWith(v2) && (v2.isCompatibleWith(v1));
    }
}
