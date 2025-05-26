package com.functest.jpwise.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An equivalence partition that cycles through a set of equivalent values. For example, when
 * testing browser versions, you might want to cycle through several versions of Chrome that are
 * considered equivalent for testing purposes.
 *
 * <p>The partition automatically cycles through all values, returning to the first value after
 * reaching the end. This is useful for ensuring test coverage across multiple equivalent values
 * while maintaining the pairwise testing efficiency.
 *
 * <p>Example usage:
 *
 * <pre>
 * CyclingPartition<String> chrome = new CyclingPartition<>(
 *     "Chrome",
 *     "116.0",  // Default value
 *     Arrays.asList("116.0", "116.1", "116.2")  // Values to cycle through
 * );
 * </pre>
 *
 * @param <T> The type of values this partition cycles through
 */
public class CyclingPartition<T> extends BaseEquivalencePartition<T> {
  private final List<T> equivalentValues;
  private final AtomicInteger currentIndex = new AtomicInteger(0);
  private final T defaultValue;

  /**
   * Creates a new cycling partition with a name, default value, and a collection of equivalent
   * values.
   *
   * @param name The name of this partition (e.g., "Chrome")
   * @param defaultValue The default value to start with
   * @param equivalentValues Collection of values to cycle through
   */
  public CyclingPartition(String name, T defaultValue, Collection<T> equivalentValues) {
    super(name);
    List<T> values = new ArrayList<>(equivalentValues);
    if (!values.contains(defaultValue)) {
      values.add(0, defaultValue);
    }
    this.equivalentValues = Collections.unmodifiableList(values);
    this.defaultValue = defaultValue;
  }

  /**
   * Creates a new cycling partition with a name and a collection of equivalent values. The first
   * value in the collection will be used as the default value.
   *
   * @param name The name of this partition (e.g., "Chrome")
   * @param equivalentValues Collection of values to cycle through
   */
  public CyclingPartition(String name, Collection<T> equivalentValues) {
    super(name);
    List<T> immutableValues = Collections.unmodifiableList(new ArrayList<>(equivalentValues));
    if (immutableValues.isEmpty()) {
      throw new IllegalArgumentException("Must provide at least one value");
    }
    this.equivalentValues = immutableValues;
    this.defaultValue = immutableValues.get(0);
  }

  /**
   * Gets the next value in the cycle. This method is thread-safe and will automatically wrap around
   * to the first value after reaching the end of the list.
   *
   * @return The next value in the cycle
   */
  @Override
  public T getValue() {
    return equivalentValues.get(currentIndex.getAndUpdate(i -> (i + 1) % equivalentValues.size()));
  }
}
