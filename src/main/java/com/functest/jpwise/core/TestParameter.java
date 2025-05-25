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

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a test parameter with its possible values and compatibility rules.
 * A test parameter is a variable in the test space that can take on different values.
 * For example, a "browser" parameter might have values like "Chrome", "Firefox", and "Safari".
 * 
 * <p>Parameters can also have compatibility rules that define which values are compatible
 * with values of other parameters. For example, "Safari" might only be compatible with
 * "MacOS" in an operating system parameter.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Simple parameter without compatibility rules
 * TestParameter browser = new TestParameter("browser", Arrays.asList(
 *     SimpleValue.of("Chrome"),
 *     SimpleValue.of("Firefox")
 * ));
 * 
 * // Parameter with compatibility rules
 * List<CompatibilityPredicate> rules = Arrays.asList(
 *     ValueCompatibility.valuesAre(
 *         new ParameterValueMatcher(Field.NAME, ConditionOperator.EQ, "Safari"),
 *         new ParameterValueMatcher(Field.NAME, ConditionOperator.EQ, "MacOS")
 *     )
 * );
 * TestParameter browser = new TestParameter("browser", values, rules);
 * </pre>
 *
 * @author panwei, davydovmd
 * @see ParameterValue
 * @see CompatibilityPredicate
 */
public class TestParameter {
    private String name;
    private ImmutableList<ParameterValue<?>> values;
    private Collection<CompatibilityPredicate> dependencies = new ArrayList<>();

    /**
     * Creates a new test parameter with the specified name and possible values.
     * This constructor creates a parameter without any compatibility rules.
     *
     * @param theName The name of the parameter (used for reporting and identification)
     * @param parameterValues Collection of possible values for this parameter
     */
    public TestParameter(String theName, Collection<? extends ParameterValue<?>> parameterValues) {
        super();
        name = theName;
        values = ImmutableList.copyOf(parameterValues);

        for (ParameterValue<?> value : parameterValues) {
            value.setParentParameter(this);
        }
    }

    /**
     * Creates a new test parameter with name, values, and compatibility rules.
     * The compatibility rules define which values of this parameter are compatible
     * with values of other parameters.
     *
     * @param theName The name of the parameter (used for reporting and identification)
     * @param parameterValues Collection of possible values for this parameter
     * @param dependencies List of compatibility rules for this parameter's values
     */
    public TestParameter(String theName, Collection<? extends ParameterValue<?>> parameterValues, List<CompatibilityPredicate> dependencies) {
        super();
        name = theName;
        this.dependencies = ImmutableList.copyOf(dependencies);
        values = ImmutableList.copyOf(parameterValues);
        for (ParameterValue<?> value : parameterValues) {
            value.setParentParameter(this);
        }
    }

    /**
     * Finds a parameter value by its name.
     *
     * @param name The name of the value to find
     * @return The matching parameter value, or null if not found
     */
    public ParameterValue<?> getValueByName(String name) {
        for (ParameterValue<?> v : values) {
            if (v.getName().equals(name)) return v;
        }
        return null;
    }

    /**
     * Gets a parameter value by its index in the values list.
     *
     * @param i The index of the value to get
     * @return The parameter value at the specified index
     */
    public ParameterValue<?> getValueByIndex(int i) {
        return values.get(i);
    }

    /**
     * Gets the name of this parameter.
     *
     * @return The parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets all possible values for this parameter.
     *
     * @return An immutable list of all parameter values
     */
    public List<ParameterValue<?>> getValues() {
        return values;
    }

    /**
     * Checks if two parameter values are compatible according to this parameter's rules.
     * This method applies all compatibility predicates to determine if the values can
     * be used together in a test combination.
     *
     * @param value1 The first parameter value to check
     * @param value2 The second parameter value to check
     * @return true if the values are compatible, false otherwise
     */
    public boolean areCompatible(ParameterValue<?> value1, ParameterValue<?> value2) {
        if (dependencies.isEmpty()) return true;
        for (CompatibilityPredicate predicate : dependencies) {
            if (predicate.isCompatible(value1, value2)) return true;
        }
        return false;
    }
}
