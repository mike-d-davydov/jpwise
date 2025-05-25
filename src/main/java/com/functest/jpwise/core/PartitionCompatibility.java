package com.functest.jpwise.core;

import java.util.function.Predicate;

/**
 * Utility class for creating compatibility predicates between EquivalencePartition instances.
 * Supports legacy matcher-based approach for backward compatibility.
 */
public class PartitionCompatibility {
    private PartitionCompatibility() {
        // Utility class, no instantiation
    }

    /**
     * Creates a compatibility predicate that checks if the first partition matches a condition.
     * Legacy method for backward compatibility.
     *
     * @param thisMatcher Matcher for the first partition
     * @return A compatibility predicate
     */
    public static CompatibilityPredicate thisIs(PartitionMatcher thisMatcher) {
        return partitionsAre(thisMatcher, null);
    }

    /**
     * Creates a compatibility predicate that checks if the second partition matches a condition.
     * Legacy method for backward compatibility.
     *
     * @param thatMatcher Matcher for the second partition
     * @return A compatibility predicate
     */
    public static CompatibilityPredicate thatIs(PartitionMatcher thatMatcher) {
        return partitionsAre(null, thatMatcher);
    }

    /**
     * Creates a compatibility predicate that checks conditions on both partitions.
     * Legacy method for backward compatibility.
     *
     * @param thisMatcher Matcher for the first partition
     * @param thatMatcher Matcher for the second partition
     * @return A compatibility predicate
     */
    public static CompatibilityPredicate partitionsAre(final PartitionMatcher thisMatcher, final PartitionMatcher thatMatcher) {
        return (left, right) -> {
            boolean res = true;
            if (thisMatcher != null) {
                res = thisMatcher.test(left);
            }

            if ((thatMatcher != null) && res) {
                res = thatMatcher.test(right);
            }

            return res;
        };
    }
}
