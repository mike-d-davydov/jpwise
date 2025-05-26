package io.github.mikeddavydov.core;

/**
 * @author DavydovMD Date: 20.06.13 Time: 14:16
 */
@FunctionalInterface
public interface CompatibilityPredicate {
  /**
   * Tests if two parameter values are compatible according to this predicate.
   *
   * @param v1 The first parameter value
   * @param v2 The second parameter value
   * @return true if the values are compatible, false otherwise
   */
  boolean areCompatible(EquivalencePartition v1, EquivalencePartition v2);
}
