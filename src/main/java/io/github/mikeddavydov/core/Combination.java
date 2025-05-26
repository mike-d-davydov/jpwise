/**
 * Copyright (c) 2010 Ng Pan Wei, 2013 Mikhail Davydov, 2013 Mikhail Davydov
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mikeddavydov.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a combination of parameter equivalence partitions (values) that forms a test case. A
 * combination can be either partial (containing only some parameter partitions) or complete
 * (containing partitions for all parameters).
 *
 * <p>Combinations are used both during test case generation (as partial combinations) and in the
 * final results (as complete test cases). The class provides methods for manipulating combinations,
 * checking their completeness, and merging them.
 *
 * <p>Example usage:
 *
 * <pre>
 * // Create a combination for 3 parameters
 * Combination combination = new Combination(3);
 *
 * // Set partitions for parameters
 * combination.setValue(0, browserPartition);
 * combination.setValue(1, osPartition);
 * combination.setValue(2, resolutionPartition);
 *
 * // Check if all parameters have partitions
 * boolean isComplete = combination.isFilled();
 *
 * // Get a unique key for the combination
 * String key = combination.getKey();
 * </pre>
 */
public class Combination {
  private static final String SEPARATOR = "|";
  private static final String EMPTY = "_";

  private EquivalencePartition[] values;

  /**
   * Creates a new combination with space for the specified number of parameter values. All values
   * are initially set to null.
   *
   * @param size The number of parameters in the combination
   */
  @SuppressWarnings("AssignmentToNull") // Here we simply initialize array
  public Combination(int size) {
    super();
    values = new EquivalencePartition[size];
    for (int i = 0; i < size; i++) values[i] = null;
  }

  /**
   * Converts this combination into a row for TestNG's data provider. The first element is the
   * combination's string representation, followed by the actual values.
   *
   * @return An array suitable for use with TestNG's data provider
   */
  Object[] asDataProviderRow() {
    List<Object> res = new ArrayList<>();
    res.add(this.toString());

    for (EquivalencePartition partition : values) {
      res.add(partition.getValue());
    }

    return res.toArray();
  }

  /**
   * Gets a value from this combination at the specified index.
   *
   * @param i The index to get the value from
   * @return The equivalence partition at the specified index, or null if not set
   */
  public EquivalencePartition getValue(int i) {
    return values[i];
  }

  /**
   * Sets a value in this combination at the specified index.
   *
   * @param i The index to set the value at
   * @param value The equivalence partition to set
   */
  public void setValue(int i, EquivalencePartition value) {
    values[i] = value;
  }

  /**
   * Generates a unique key for this combination. The key is a string representation of all
   * partition names, with empty positions marked by underscores and names separated by vertical
   * bars. For example: "Chrome|_|1024x768"
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
      if (i < (values.length - 1)) key += SEPARATOR;
    }
    return key;
  }

  /**
   * Checks if this combination is complete (has a partition set for all parameters).
   *
   * @return true if all parameters have a partition set, false otherwise
   */
  public boolean isFilled() {
    for (EquivalencePartition partition : values) {
      if (partition == null) return false;
    }
    return true;
  }

  /**
   * Attempts to merge this combination with another combination. The merge succeeds if there are no
   * conflicts (same parameter having different partitions). Partitions from the other combination
   * are added to positions that are null in this combination.
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
          if (!result.getValue(i).equals(other.getValue(i))) return null;
        }
      }
    }
    return result;
  }

  /**
   * Finds the difference between this combination and another combination. The result contains
   * values that are different between the combinations, with null values where they are the same.
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
          if (result.getValue(i).equals(other.getValue(i))) result.setValue(i, null);
          else return null;
        }
      }
    }
    return result;
  }

  /**
   * Gets a copy of all parameter partitions in this combination.
   *
   * @return An array containing all parameter partitions
   */
  public EquivalencePartition[] getValues() {
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
        sb.append(values[i].getParentParameter().getName()).append(":").append(values[i].getName());
      }
    }
    sb.append("]}");
    return sb.toString();
  }

  /**
   * Checks if all parameter values in this combination are compatible with each other. This uses
   * the compatibility rules defined in the parameters.
   *
   * @param algorithm The generation algorithm providing compatibility checking
   * @return true if all values are compatible, false if any are incompatible
   */
  public boolean checkNoConflicts(GenerationAlgorithm algorithm) {
    // Only check compatibility between parameters that have rules
    for (int i = 0; i < values.length; i++) {
      EquivalencePartition v1 = values[i];
      if (v1 == null) continue;

      // Only check parameters that have compatibility rules
      TestParameter param1 = v1.getParentParameter();
      if (param1.getDependencies().isEmpty()) continue;

      for (int j = i + 1; j < values.length; j++) {
        EquivalencePartition v2 = values[j];
        if (v2 == null) continue;

        // Only check if either parameter has rules
        TestParameter param2 = v2.getParentParameter();
        if (param1.getDependencies().isEmpty() && param2.getDependencies().isEmpty()) continue;

        if (!algorithm.isCompatible(v1, v2)) {
          return false;
        }
      }
    }
    return true;
  }
}
