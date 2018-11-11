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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Combination Table.
 *
 * @author panwei
 */
public class CombinationTable {
    /**
     * List of combinations.
     */
    private List<Combination> _combinations;

    /**
     * Constructor.
     */
    public CombinationTable() {
        super();
        _combinations = new ArrayList<>();
    }

    /**
     * Add a combination.
     *
     * @param combination a Combination to add
     */
    public void add(Combination combination) {
        _combinations.add(combination);
    }

    /**
     * The size of the table.
     *
     * @return
     */
    public int size() {
        return _combinations.size();
    }

    /**
     * Get the combinations.
     *
     * @return
     */
    public List<Combination> combinations() {
        return _combinations;
    }

    public List<Map<String, Object>> asRowMapList() {
        List<Map<String, Object>> rows = new ArrayList<>();

        for (Combination combination : _combinations) {
            Map<String, Object> row = new HashMap<>();

            for (ParameterValue value : combination.getValues()) {
                row.put(value.getParentParameter().getName(), value.get());
            }
            row.put("combination_description", combination.toString());
            rows.add(row);
        }

        return rows;
    }


    public Object[][] asDataProvider() {
        List<Object[]> testCases = new ArrayList<>();
        for (Combination combination : _combinations) {
            testCases.add(combination.asDataProviderRow());
        }

        Object[][] res = new Object[testCases.size()][];
        return testCases.toArray(res);
    }

    /**
     * Get the depth of the combinations.
     *
     * @return
     */
    public int breadth() {
        Combination combination = _combinations.get(0);
        if (combination == null)
            return -1;
        return combination.size();
    }

    /**
     * Get the number of N-Pairs spanned by this set of combinations.
     *
     * @return
     */
    public int span() {
        int depth = breadth();
        if (depth < 0)
            return -1;
        HashMap<String, Integer> tempMap = new HashMap<>();
        for (int i = 0; i < depth; i++) {
            for (int j = i + 1; j < depth; j++) {
                for (Combination generatedEntry : _combinations) {
                    if ((generatedEntry.getValue(i) != null)
                            && (generatedEntry.getValue(j) != null)) {
                        Combination entry = new Combination(depth);
                        entry.setValue(i, generatedEntry.getValue(i));
                        entry.setValue(j, generatedEntry.getValue(j));
                        String key = entry.getKey();
                        tempMap.put(key, 1);
                    }
                }
            }
        }
        return tempMap.size();
    }

    @Override
    public String toString() {
        return String.format(
                "CombinationTable{%d combinations.%s}",
                _combinations.size(),
                _combinations.isEmpty() ? "" : (" First is: " + _combinations.get(0))
        );
    }
}
