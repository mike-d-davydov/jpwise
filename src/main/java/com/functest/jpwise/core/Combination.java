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
import java.util.Objects;

/**
 * Represents a combination of parameter values that forms a test case.
 * A combination can be either partial (containing only some parameter values)
 * or complete (containing values for all parameters).
 * 
 * <p>Combinations are used both during test case generation (as partial combinations)
 * and in the final results (as complete test cases). The class provides methods
 * for manipulating combinations, checking their completeness, and merging them.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Create a combination for 3 parameters
 * Combination combination = new Combination(3);
 * 
 * // Set values for parameters
 * combination.setValue(0, browserValue);
 * combination.setValue(1, osValue);
 * combination.setValue(2, resolutionValue);
 * 
 * // Check if all parameters have values
 * boolean isComplete = combination.isFilled();
 * 
 * // Get a unique key for the combination
 * String key = combination.getKey();
 * </pre>
 */
public class Combination {
    private static final String SEPARATOR = "|";
    private static final String EMPTY = "_";
    private ParameterValue[] values;

    /**
     * Creates a new combination with space for the specified number of parameter values.
     * All values are initially set to null.
     *
     * @param size The number of parameters in the combination
     */
    @SuppressWarnings("AssignmentToNull") // Here we simply initialize array
    public Combination(int size) {
        super();
        values = new ParameterValue[size];
        for (int i = 0; i < size; i++)
            values[i] = null;
    }

    /**
     * Converts this combination into a row for TestNG's data provider.
     * The first element is the combination's string representation,
     * followed by the actual values.
     *
     * @return An array suitable for use with TestNG's data provider
     */
    Object[] asDataProviderRow() {
        List<Object> res = new ArrayList<>();
        res.add(this.toString());

        for (ParameterValue value : values) {
            res.add(value.getValue());
        }

        return res.toArray();
    }

    /**
     * Gets the parameter value at the specified index.
     *
     * @param i The index of the parameter value to get
     * @return The parameter value, or null if not set
     */
    public ParameterValue getValue(int i) {
        return values[i];
    }

    /**
     * Sets the parameter value at the specified index.
     *
     * @param i The index at which to set the value
     * @param value The parameter value to set
     */
    public void setValue(int i, ParameterValue value) {
        values[i] = value;
    }

    /**
     * Generates a unique key for this combination.
     * The key is a string representation of all values, with empty positions
     * marked by underscores and values separated by vertical bars.
     * For example: "Chrome|_|1024x768"
     *
     * @return A string key uniquely identifying this combination
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
     * Checks if this combination is complete (has values for all parameters).
     *
     * @return true if all parameters have values, false otherwise
     */
    public boolean isFilled() {
        for (ParameterValue value : values) {
            if (value == null)
                return false;
        }
        return true;
    }

    /**
     * Attempts to merge this combination with another combination.
     * The merge succeeds if there are no conflicts (same parameter having different values).
     * Values from the other combination are added to positions that are null in this combination.
     *
     * @param other The combination to merge with this one
     * @return A new merged combination, or null if there are conflicts
     */
    public Combination merge(Combination other) {
        Combination result = new Combination(values.length);

        for (int i = 0; i < values.length; i++) {
            result.setValue(i, getValue(i));
            if (other.getValue(i) != null) {
                if (getValue(i) == null) {
                    result.setValue(i, other.getValue(i));
                } else {
                    if (!result.getValue(i).equals(other.getValue(i)))
                        return null;
                }
            }
        }
        return result;
    }

    /**
     * Finds the difference between this combination and another combination.
     * The result contains values that are different between the combinations,
     * with null values where they are the same.
     *
     * @param other The combination to compare with
     * @return A new combination containing the differences, or null if invalid
     */
    public Combination diff(Combination other) {
        Combination result = new Combination(values.length);
        for (int i = 0; i < values.length; i++) {
            result.setValue(i, values[i]);
            if (other.getValue(i) != null) {
                if (result.getValue(i) == null) {
                    result.setValue(i, other.getValue(i));
                } else {
                    if (result.getValue(i).equals(other.getValue(i)))
                        result.setValue(i, null);
                    else
                        return null;
                }
            }
        }
        return result;
    }

    /**
     * Gets a copy of all parameter values in this combination.
     *
     * @return An array containing all parameter values
     */
    public ParameterValue[] getValues() {
        return Arrays.copyOf(values, values.length);
    }

    /**
     * Gets the number of parameters in this combination.
     *
     * @return The size of the combination
     */
    public int size() {
        return values.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Combination that = (Combination) o;
        return Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Combination{[");
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(values[i].getParentParameter().getName())
                  .append(":")
                  .append(values[i].getName());
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * Checks if all parameter values in this combination are compatible with each other.
     * This uses the compatibility rules defined in the parameters.
     *
     * @param algorithm The generation algorithm providing compatibility checking
     * @return true if all values are compatible, false if any are incompatible
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
