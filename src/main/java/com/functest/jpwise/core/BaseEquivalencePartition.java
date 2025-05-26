package com.functest.jpwise.core;

/**
 * Base class for equivalence partitions. An equivalence partition represents a set of values that
 * are considered equivalent for testing purposes.
 *
 * @param <T> The type of values in this partition
 */
public abstract class BaseEquivalencePartition<T> implements EquivalencePartition<T> {
  private final String name;
  private TestParameter parent;

  protected BaseEquivalencePartition(String name) {
    this.name = name;
  }

  /**
   * Gets the name of this equivalence partition.
   *
   * @return The partition name
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Gets the parent parameter of this equivalence partition.
   *
   * @return The parent parameter
   */
  @Override
  public TestParameter getParentParameter() {
    return parent;
  }

  /**
   * Sets the parent parameter of this equivalence partition.
   *
   * @param parent The parent parameter to set
   */
  @Override
  public void setParentParameter(TestParameter parent) {
    this.parent = parent;
  }

  /**
   * Gets the value of this equivalence partition. This method is abstract and must be implemented
   * by subclasses.
   *
   * @return The current value of this partition
   */
  @Override
  public abstract T getValue();

  @Override
  public String toString() {
    return String.format("%s:%s", getName(), getValue());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EquivalencePartition)) return false;

    EquivalencePartition<?> that = (EquivalencePartition<?>) o;

    return getName().equals(that.getName());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public boolean isCompatibleWith(EquivalencePartition<?> other) {
    return getParentParameter() == null || getParentParameter().areCompatible(this, other);
  }
}
