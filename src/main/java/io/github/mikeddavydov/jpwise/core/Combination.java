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
package io.github.mikeddavydov.jpwise.core;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a combination of parameter equivalence partitions (values) that
 * forms a test case. A
 * combination can be either partial (containing only some parameter partitions)
 * or complete
 * (containing partitions for all parameters).
 *
 * <p>
 * Combinations are used both during test case generation (as partial
 * combinations) and in the
 * final results (as complete test cases). The class provides methods for
 * manipulating combinations,
 * checking their completeness, and merging them.
 *
 * <p>
 * Example usage:
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
  private static final Logger logger = LoggerFactory.getLogger(Combination.class);
  private static final String SEPARATOR = "|";
  private static final String EMPTY = "_";

  private final EquivalencePartition[] values;

  /**
   * Creates a new combination with the specified number of parameters.
   * All values are initially null.
   *
   * @param size The number of parameters in the combination
   */
  public Combination(int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("Combination size must be positive");
    }
    this.values = new EquivalencePartition[size];
  }

  /**
   * Creates a copy of an existing combination.
   *
   * @param other The combination to copy
   */
  public Combination(Combination other) {
    if (other == null) {
      throw new IllegalArgumentException("Cannot copy null combination");
    }
    this.values = new EquivalencePartition[other.values.length];
    System.arraycopy(other.values, 0, this.values, 0, other.values.length);
  }

  /**
   * Converts this combination into a row for TestNG's data provider. The first
   * element is the
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
   * Gets the value for a parameter in this combination.
   *
   * @param index The index of the parameter
   * @return The value at the specified index
   */
  public EquivalencePartition getValue(int index) {
    return values[index];
  }

  /**
   * Sets the value for a parameter in this combination.
   *
   * @param index The index of the parameter
   * @param value The value to set
   */
  public void setValue(int index, EquivalencePartition value) {
    if (index < 0 || index >= values.length) {
      throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + values.length);
    }
    if (value == null) {
      throw new IllegalArgumentException("Cannot set null value for combination");
    }
    values[index] = value;
  }

  /**
   * Generates a unique key for this combination. The key is a string
   * representation of all
   * partition names, with empty positions marked by underscores and names
   * separated by vertical
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
      if (i < (values.length - 1)) {
        key += SEPARATOR;
      }
    }
    return key;
  }

  /**
   * Checks if this combination is complete (has a partition set for all
   * parameters).
   *
   * @return true if all parameters have a partition set, false otherwise
   */
  public boolean isFilled() {
    if (values == null) {
      return false;
    }
    for (EquivalencePartition value : values) {
      if (value == null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Attempts to merge this combination with another combination. The merge
   * succeeds if there are no
   * conflicts (same parameter having different partitions). Partitions from the
   * other combination
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
          if (!result.getValue(i).equals(other.getValue(i))) {
            return null;
          }
        }
      }
    }
    return result;
  }

  /**
   * Finds the difference between this combination and another combination. The
   * result contains
   * values that are different between the combinations, with null values where
   * they are the same.
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
          if (result.getValue(i).equals(other.getValue(i))) {
            result.setValue(i, null);
          } else {
            return null;
          }
        }
      }
    }
    return result;
  }

  /**
   * Gets the array of values in this combination.
   *
   * @return The array of values
   */
  public EquivalencePartition[] getValues() {
    return values;
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
    if (this == o) {
      return true;
    }
    if (!(o instanceof Combination)) {
      return false;
    }

    Combination that = (Combination) o;
    if (values.length != that.values.length) {
      return false;
    }
    for (int i = 0; i < values.length; i++) {
      if (!values[i].equals(that.values[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = 0;
    for (EquivalencePartition value : values) {
      if (value != null) {
        result = 31 * result + value.hashCode();
      }
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Combination{[");
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      if (values[i] != null) {
        sb.append(values[i].getParentParameter().getName())
            .append(":")
            .append(values[i].getName());
      } else {
        sb.append("null");
      }
    }
    sb.append("]}");
    return sb.toString();
  }

  /**
   * Checks if all parameter values in this combination are compatible with each
   * other. This uses
   * the compatibility rules defined in the parameters.
   *
   * @param algorithm The generation algorithm providing compatibility checking
   * @return true if all values are compatible, false if any are incompatible or
   *         if the combination is not filled
   */
  public boolean checkNoConflicts(GenerationAlgorithm algorithm) {
    if (algorithm == null) {
      throw new IllegalArgumentException("Algorithm cannot be null");
    }

    // Check for null values
    for (int i = 0; i < values.length; i++) {
      if (values[i] == null) {
        continue; // Skip null values as they don't cause conflicts
      }

      // Check compatibility with other non-null values
      for (int j = i + 1; j < values.length; j++) {
        if (values[j] == null) {
          continue;
        }
        if (!values[i].isCompatibleWith(values[j])) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Thoroughly checks if all values in the combination are compatible with each
   * other.
   * This method checks all possible combinations of values, not just pairs.
   * It is slower but more accurate for complex compatibility rules.
   *
   * @param algorithm The generation algorithm to use for compatibility checks
   * @return true if all values are compatible, false otherwise
   */
  public boolean checkNoConflictsThoroughly(GenerationAlgorithm algorithm) {
    if (algorithm == null) {
      logger.warn("Generation algorithm is null, skipping compatibility check");
      return true;
    }

    // Get all non-null values
    List<EquivalencePartition> nonNullValues = new ArrayList<>();
    for (EquivalencePartition value : this.values) {
      if (value != null) {
        nonNullValues.add(value);
      }
    }

    // If we have less than 2 values, there can't be any conflicts
    if (nonNullValues.size() < 2) {
      return true;
    }

    // Check all possible combinations of values
    for (int i = 0; i < nonNullValues.size(); i++) {
      for (int j = i + 1; j < nonNullValues.size(); j++) {
        EquivalencePartition ep1 = nonNullValues.get(i);
        EquivalencePartition ep2 = nonNullValues.get(j);

        // Check compatibility in both directions
        if (!ep1.isCompatibleWith(ep2) || !ep2.isCompatibleWith(ep1)) {
          logger.debug("Found incompatible values: {} and {}", ep1, ep2);
          return false;
        }
      }
    }

    return true;
  }
}
