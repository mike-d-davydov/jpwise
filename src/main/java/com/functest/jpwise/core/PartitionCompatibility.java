package com.functest.jpwise.core;

import java.util.function.Predicate;

/**
 * Utility class for creating compatibility predicates between EquivalencePartition instances. This
 * class provides a modern Predicate-based approach for defining compatibility rules.
 */
public final class PartitionCompatibility {

  private PartitionCompatibility() {
    // Utility class, no instantiation
  }

  /**
   * Creates a compatibility predicate that checks if both partitions satisfy their respective
   * conditions. If a condition is null, it is treated as always true for that partition.
   *
   * @param conditionForLeft Predicate to test the left partition, or null for no condition
   * @param conditionForRight Predicate to test the right partition, or null for no condition
   * @return A compatibility predicate that tests both conditions
   */
  public static CompatibilityPredicate conditionsHold(
      Predicate<EquivalencePartition<?>> conditionForLeft,
      Predicate<EquivalencePartition<?>> conditionForRight) {
    return (left, right) -> {
      boolean leftResult = conditionForLeft == null || conditionForLeft.test(left);
      boolean rightResult = conditionForRight == null || conditionForRight.test(right);
      return leftResult && rightResult;
    };
  }

  /**
   * Creates a compatibility predicate that checks if the left partition satisfies a condition. The
   * right partition is always considered compatible.
   *
   * @param conditionForLeft Predicate to test the left partition
   * @return A compatibility predicate that only tests the left condition
   */
  public static CompatibilityPredicate leftSatisfies(
      Predicate<EquivalencePartition<?>> conditionForLeft) {
    return conditionsHold(conditionForLeft, null);
  }

  /**
   * Creates a compatibility predicate that checks if the right partition satisfies a condition. The
   * left partition is always considered compatible.
   *
   * @param conditionForRight Predicate to test the right partition
   * @return A compatibility predicate that only tests the right condition
   */
  public static CompatibilityPredicate rightSatisfies(
      Predicate<EquivalencePartition<?>> conditionForRight) {
    return conditionsHold(null, conditionForRight);
  }
}
