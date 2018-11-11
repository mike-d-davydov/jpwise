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
 * Class to hold the possible values of an element.
 *
 * @author panwei , davydovmd
 */
public class TestParameter {
    private String name;
    private ImmutableList<ParameterValue> values;
    private Collection<CompatibilityPredicate> dependencies = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param theName         parameter name (for reporting only)
     * @param parameterValues list of parameter value objects
     */
    public TestParameter(String theName, Collection<ParameterValue> parameterValues) {
        super();
        name = theName;
        values = ImmutableList.copyOf(parameterValues);

        for (ParameterValue value : parameterValues) {
            value.setParentParameter(this);
        }
    }


    /**
     * Constructor.
     *
     * @param theName         parameter name (for reporting only)
     * @param parameterValues list of parameter value objects
     */
    public TestParameter(String theName, Collection<ParameterValue> parameterValues, List<CompatibilityPredicate> dependencies) {
        super();
        name = theName;
        this.dependencies = ImmutableList.copyOf(dependencies);
        values = ImmutableList.copyOf(parameterValues);
        for (ParameterValue value : parameterValues) {
            value.setParentParameter(this);
        }
    }

    public ParameterValue getValueByName(String name) {
        for (ParameterValue v : values) {
            if (v.getName().equals(name)) return v;
        }
        return null;
    }

    public ParameterValue getValueByIndex(int i) {
        return values.get(i);
    }

    /**
     * ValueName of the dimension
     */
    public String getName() {
        return name;
    }


    /**
     * Possible values of the dimension
     */

    public List<ParameterValue> getValues() {
        return values;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestParameter{");
        sb.append("name='").append(name).append('\'');
        sb.append(", values=").append(values);
        sb.append('}');
        return sb.toString();
    }

    public boolean areCompatible(ParameterValue thisValue, ParameterValue thatValue) {
        if (dependencies.isEmpty()) return true;
        for (CompatibilityPredicate predicate : dependencies) {
            if (predicate.isCompatible(thisValue, thatValue)) return true;
        }
        return false;
    }
}
