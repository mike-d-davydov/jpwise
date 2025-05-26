package io.github.mikeddavydov.core;

import java.util.function.Supplier;

/**
 * A generic implementation of an equivalence partition that uses a supplier function to generate
 * values. This class can be used as a base for specific partition types by providing different
 * value generation strategies.
 */
public class GenericPartition extends BaseEquivalencePartition {
  private final Supplier<Object> valueGenerator;

  /**
   * Creates a new generic partition with a name and a value generator function.
   *
   * @param name The name of this partition
   * @param valueGenerator A function that supplies values for this partition
   */
  public GenericPartition(String name, Supplier<Object> valueGenerator) {
    super(name);
    this.valueGenerator = valueGenerator;
  }

  @Override
  public Object getValue() {
    return valueGenerator.get();
  }

  /**
   * Factory method to create a new generic partition.
   *
   * @param name The name of this partition
   * @param valueGenerator A function that supplies values for this partition
   * @return A new generic partition
   */
  public static GenericPartition of(String name, Supplier<Object> valueGenerator) {
    return new GenericPartition(name, valueGenerator);
  }
}
