/**
 * Copyright (c) 2010 Ng Pan Wei, 2013 Mikhail Davydov
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
 * Represents a combination of parameter values for testing.
 *
 * <p>A combination is a set of values, one from each parameter's partition, that together form a
 * single test case.
 */
public class Combination {
  private static final Logger logger = LoggerFactory.getLogger(Combination.class);
  private static final String SEPARATOR = "|";
  private static final String EMPTY = "_";

  private final EquivalencePartition[] values;
  private final List<TestParameter> parameters;

  /**
   * Creates a new combination with the specified parameters. All values are initially null.
   *
   * @param parameters The list of parameters for this combination
   */
  public Combination(List<TestParameter> parameters) {
    if (parameters == null || parameters.isEmpty()) {
      throw new IllegalArgumentException("Parameters list must not be null or empty");
    }
    this.parameters = new ArrayList<>(parameters);
    this.values = new EquivalencePartition[parameters.size()];
  }

  /**
   * Creates a new combination with the specified size. All values are initially null.
   *
   * @param size The number of parameters in this combination
   */
  public Combination(int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("Size must be positive");
    }
    this.parameters = new ArrayList<>();
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
    this.parameters = new ArrayList<>(other.parameters);
    this.values = new EquivalencePartition[other.values.length];
    System.arraycopy(other.values, 0, this.values, 0, other.values.length);
  }

  /**
   * Gets the list of parameters in this combination.
   *
   * @return The list of parameters
   */
  public List<TestParameter> getParameters() {
    return parameters;
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
      throw new IndexOutOfBoundsException(
          "Index " + index + " out of bounds for length " + values.length);
    }
    values[index] = value;
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
      if (i < (values.length - 1)) {
        key += SEPARATOR;
      }
    }
    return key;
  }

  /**
   * Checks if this combination is complete (has a partition set for all parameters).
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
   * Counts the number of parameters that have a value set in this combination.
   *
   * @return The count of non-null values
   */
  public int getSetCount() {
    if (values == null) {
      return 0;
    }
    int count = 0;
    for (EquivalencePartition value : values) {
      if (value != null) {
        count++;
      }
    }
    return count;
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
    logger.debug(
        "Combination.merge called. this.getKey(): {}, other.getKey(): {}",
        this.getKey(),
        other.getKey());

    if (other == null) {
      logger.error("Combination.merge: 'other' combination is null.");
      throw new IllegalArgumentException("Cannot merge with a null combination");
    }
    if (this.parameters == null) { // Should not happen if constructor is correct
      logger.error("Combination.merge: 'this.parameters' is null.");
      // This would be a critical internal error.
      throw new IllegalStateException("'this.parameters' cannot be null in merge operation.");
    }
    // The parameters lists should be compatible (ideally same instance or .equals()
    // true)
    // For LegacyPairwiseAlgorithm, 'this.parameters' (from curCombination) and
    // 'other.parameters' (from fromQueue)
    // are both derived from 'input.getTestParameters()', so they should be
    // compatible.
    // The original merge logic didn't explicitly check parameters list equality
    // here,
    // relying on them having the same size and corresponding TestParameter objects.

    Combination result = null;
    try {
      // 'this.parameters' is the list of TestParameter objects for the current
      // combination.
      // This list is used to initialize the 'result' combination.
      logger.debug(
          "Combination.merge: About to create result = new Combination(this.parameters). this.parameters size: {}",
          this.parameters.size());
      result = new Combination(this.parameters);
      logger.debug(
          "Combination.merge: result combination created. result.values.length: {}",
          result.values.length);
    } catch (Exception e) {
      logger.error("Combination.merge: Exception during new Combination(this.parameters): ", e);
      // If an exception occurs here, rethrow it to make it visible.
      throw e;
    }

    logger.debug("Combination.merge: Starting loop. this.values.length: {}", this.values.length);
    // Assuming this.values.length == other.values.length because they should share
    // compatible parameter lists.
    for (int i = 0; i < this.values.length; i++) {
      EquivalencePartition thisValue = this.values[i];
      // Defensive check for other.values, though it should be initialized by
      // Combination's constructor
      EquivalencePartition otherValue =
          (other.values != null && i < other.values.length) ? other.values[i] : null;

      logger.debug(
          "  Merge loop i={}: this.value={}, other.value={}",
          i,
          (thisValue == null ? "null" : thisValue.getName()),
          (otherValue == null ? "null" : otherValue.getName()));

      if (thisValue != null && otherValue != null) {
        // Both have a value for this parameter
        logger.debug(
            "    Both non-null. Comparing: '{}' with '{}'",
            thisValue.getName(),
            otherValue.getName());
        if (!thisValue.equals(otherValue)) {
          logger.debug("    Conflict! Values are not equal. Returning null.");
          return null; // Conflict
        }
        // Since they are equal, assign one of them to the result.
        // Direct assignment to result.values[i] is fine if setValue has side effects we
        // want to avoid here.
        result.values[i] = thisValue;
        logger.debug("    Values equal. result.values[{}] set to {}", i, thisValue.getName());
      } else if (thisValue != null) {
        // Only this combination has a value
        result.values[i] = thisValue;
        logger.debug(
            "    Only thisValue non-null. result.values[{}] set to {}", i, thisValue.getName());
      } else if (otherValue != null) {
        // Only other combination has a value
        result.values[i] = otherValue;
        logger.debug(
            "    Only otherValue non-null. result.values[{}] set to {}", i, otherValue.getName());
      } else {
        // Both are null, result.values[i] remains null (as initialized)
        logger.debug("    Both values null. result.values[{}] remains null.", i);
      }
    }
    logger.debug("Combination.merge loop finished. Returning result.getKey(): {}", result.getKey());
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
    Combination result = new Combination(parameters);
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
      // Different number of parameters, so cannot be equal in terms of structure.
      // This check might be too strict if we allow comparisons of combinations
      // that might be from different contexts but coincidentally have same values.
      // However, for typical use (e.g., in a Set or List.contains()), they should
      // have the same structure.
      return false;
    }

    // Also compare the parameter lists for equality. Two combinations are not truly
    // equal if they don't refer to the same parameters, even if their values array is
    // coincidentally the same.
    // This assumes TestParameter has a proper equals/hashCode implementation.
    if (!this.parameters.equals(that.parameters)) {
      // If the parameter lists are not considered structurally equal by List.equals
      // (which uses element-wise equals), then the combinations are different.
      // This is important if combinations come from different TestInputs.
      // However, if parameters list is empty (e.g. constructed with size), this check might be
      // problematic.
      // For now, let's rely on parameters being part of the identity.
      // If this.parameters is empty for both (e.g. constructed with int size), they would pass
      // this.
      return false;
    }

    for (int i = 0; i < values.length; i++) {
      EquivalencePartition thisValue = this.values[i];
      EquivalencePartition thatValue = that.values[i];
      if (thisValue == null) {
        if (thatValue != null) {
          return false; // One is null, the other is not
        }
        // Both are null, continue to next value
      } else {
        // thisValue is not null
        if (!thisValue.equals(thatValue)) {
          return false; // thisValue is not null, but not equal to thatValue (which could be null or
          // non-null)
        }
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
        sb.append(values[i].getParentParameter().getName()).append(":").append(values[i].getName());
      } else {
        sb.append("null");
      }
    }
    sb.append("]}");
    return sb.toString();
  }
}
