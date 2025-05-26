package io.github.mikeddavydov.core;

/**
 * A simple equivalence partition that always returns the same value. This is useful for constant
 * values that don't change during testing.
 */
public class SimpleValue extends GenericPartition {
  /**
   * Creates a new simple value partition with the same name as its string value.
   *
   * @param value The constant value for this partition
   */
  public SimpleValue(Object value) {
    this(String.valueOf(value), value);
  }

  /**
   * Creates a new simple value partition with a specific name and value.
   *
   * @param name The name of this partition
   * @param value The constant value for this partition
   */
  public SimpleValue(String name, Object value) {
    super(name, () -> value);
  }

  /**
   * Factory method to create a simple value partition with the same name as its string value.
   *
   * @param value The constant value for this partition
   * @return A new simple value partition
   */
  public static SimpleValue of(Object value) {
    return new SimpleValue(value);
  }

  /**
   * Factory method to create a simple value partition with a specific name and value.
   *
   * @param name The name of this partition
   * @param value The constant value for this partition
   * @return A new simple value partition
   */
  public static SimpleValue of(String name, Object value) {
    return new SimpleValue(name, value);
  }

  /**
   * Creates a new SimpleValue where the name is used as the string value. This is a convenience
   * factory method for cases where the value is the same as the name.
   *
   * @param name The name and value of this partition
   * @return A new SimpleValue instance
   */
  public static SimpleValue of(String name) {
    return new SimpleValue(name, name);
  }
}
