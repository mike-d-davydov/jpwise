package io.github.mikeddavydov.jpwise.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An equivalence partition that cycles through a set of equivalent values. For
 * example, when
 * testing browser versions, you might want to cycle through several versions of
 * Chrome that are
 * considered equivalent for testing purposes.
 *
 * <p>
 * The partition automatically cycles through all values, returning to the first
 * value after
 * reaching the end. This is useful for ensuring test coverage across multiple
 * equivalent values
 * while maintaining the pairwise testing efficiency.
 *
 * <p>
 * Example usage:
 *
 * <pre>
 * // Using factory method with varargs
 * CyclingPartition chrome = CyclingPartition.of("Chrome", "116.0", "116.1", "116.2");
 *
 * // Using factory method with collection
 * CyclingPartition firefox = CyclingPartition.of("Firefox", Arrays.asList("118.0.2", "118.0.3"));
 * </pre>
 */
public class CyclingPartition extends BaseEquivalencePartition {
  private List<Object> equivalentValues;
  private final AtomicInteger currentIndex = new AtomicInteger(0);

  /**
   * Creates a new cycling partition with a name and a collection of equivalent
   * values.
   *
   * @param name             The name of this partition (e.g., "Chrome")
   * @param equivalentValues Collection of values to cycle through
   * @return A new cycling partition
   */
  public static CyclingPartition of(String name, Collection<Object> equivalentValues) {
    return new CyclingPartition(name, equivalentValues);
  }

  /**
   * Creates a new cycling partition with a name and a set of equivalent values.
   *
   * @param name   The name of this partition (e.g., "Chrome")
   * @param values Values to cycle through
   * @return A new cycling partition
   */
  public static CyclingPartition of(String name, Object... values) {
    return new CyclingPartition(name, values);
  }

  /**
   * Creates a new cycling partition with a name and a collection of equivalent
   * values.
   *
   * @param name             The name of this partition (e.g., "Chrome")
   * @param equivalentValues Collection of values to cycle through
   */
  public CyclingPartition(String name, Collection<Object> equivalentValues) {
    super(name);
    checkNotNull(equivalentValues, "equivalentValues cannot be null");
    checkArgument(!equivalentValues.isEmpty(), "Must provide at least one value");
    this.equivalentValues = new ArrayList<>(equivalentValues);
  }

  /**
   * Creates a new cycling partition with a name and a set of equivalent values.
   *
   * @param name   The name of this partition (e.g., "Chrome")
   * @param values Values to cycle through
   */
  public CyclingPartition(String name, Object... values) {
    super(name);
    checkArgument(values != null, "values cannot be null");
    checkArgument(values.length > 0, "Must provide at least one value");
    this.equivalentValues = new ArrayList<>(Arrays.asList(values));
  }

  /**
   * Gets the next value in the cycle. This method is thread-safe and will
   * automatically wrap around
   * to the first value after reaching the end of the list.
   *
   * @return The next value in the cycle
   */
  @Override
  public Object getValue() {
    return equivalentValues.get(currentIndex.getAndUpdate(i -> (i + 1) % equivalentValues.size()));
  }
}
