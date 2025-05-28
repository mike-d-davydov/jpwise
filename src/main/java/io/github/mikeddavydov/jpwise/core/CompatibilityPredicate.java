package io.github.mikeddavydov.jpwise.core;

/** A predicate that determines if two equivalence partitions are compatible. */
@FunctionalInterface
public interface CompatibilityPredicate {
  /**
   * Tests if two equivalence partitions are compatible.
   *
   * @param v1 The first equivalence partition
   * @param v2 The second equivalence partition
   * @return true if the partitions are compatible, false otherwise
   */
  boolean test(EquivalencePartition v1, EquivalencePartition v2);
}
