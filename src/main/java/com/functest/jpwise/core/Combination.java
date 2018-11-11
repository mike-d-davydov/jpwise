/**
 * Copyright (c) 2010  Ng Pan Wei, 2013 Mikhail Davydov, 2013 Mikhail Davydov
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A combination of values.
 *
 * @author panwei
 */
public class Combination {
    private static final String SEPARATOR = "|";
    private static final String EMPTY = "_";
    private ParameterValue[] values;


    /**
     * Constructor.
     *
     * @param size
     */
    @SuppressWarnings("AssignmentToNull") // Here we simply initialize array
    public Combination(int size) {
        super();
        values = new ParameterValue[size];
        for (int i = 0; i < size; i++)
            values[i] = null;
    }

    Object[] asDataProviderRow() {
        List<Object> res = new ArrayList<>();
        res.add(this.toString());

        for (ParameterValue value : values) {
            res.add(value.get());
        }

        return res.toArray();
    }

    public ParameterValue getValue(int i) {
        return values[i];
    }

    public void setValue(int i, ParameterValue value) {
        values[i] = value;
    }

    /**
     * Get a key for the combination.
     * Key is a concatenation of values.
     *
     * @return Key for a combination
     */
    public String getKey() {
        String key = "";
        for (int i = 0; i < values.length; i++) {
            if (getValue(i) == null) {
                key += EMPTY;
            } else {
                key += values[i].getName();
            }
            if (i < (values.length - 1))
                key += SEPARATOR;
        }
        return key;
    }

    /**
     * Determine if this combination is filled/completed.
     * A filled combination has all non-null values.
     *
     * @return
     */
    public boolean isFilled() {
        for (ParameterValue value : values) {
            if (value == null)
                return false;
        }
        return true;
    }

    /**
     * Merge a combination into this combination to produce
     * another combination
     *
     * @param operand the combination to be merged with this combination.
     * @return null if merge has conflicts.
     */
    public Combination merge(Combination operand) {
        Combination result = new Combination(values.length);

        for (int i = 0; i < values.length; i++) {
            result.setValue(i, getValue(i));
            if (operand.getValue(i) != null) {
                if (getValue(i) == null) {
                    result.setValue(i, operand.getValue(i));
                } else {
                    if (!result.getValue(i).equals(operand.getValue(i)))
                        return null;
                }
            }
        }
        return result;
    }

    /**
     * Find the difference between given combination with this combination.
     *
     * @param operand
     * @return null if difference is not valid.
     */
    public Combination diff(Combination operand) {
        Combination result = new Combination(values.length);
        for (int i = 0; i < values.length; i++) {
            result.setValue(i, values[i]);
            if (operand.getValue(i) != null) {
                if (result.getValue(i) == null) {
                    result.setValue(i, operand.getValue(i));
                } else {
                    if (result.getValue(i).equals(operand.getValue(i)))
                        result.setValue(i, null);
                    else
                        return null;
                }
            }
        }
        return result;
    }

    /**
     * The values.
     * If the value[i] == null, it is not filled.
     */
    public ParameterValue[] getValues() {
        return Arrays.copyOf(values, values.length);
    }


    public int size() {
        return values.length;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Combination{");
        sb.append(Arrays.toString(values));
        sb.append('}');
        return sb.toString();
    }

    /**
     * True means no conflicts; False
     *
     * @param algorithm@return
     */
    public boolean checkNoConflicts(GenerationAlgorithm algorithm) {

        for (ParameterValue v1 : getValues()) {
            for (ParameterValue v2 : getValues()) {
                if ((v1 != null) && (v2 != null)) {
                    if (!algorithm.isCompatible(v1, v2))
                        return false;
                }
            }
        }
        return true;
    }
}
