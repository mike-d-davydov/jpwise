package com.functest.jpwise.core;

import com.functest.jpwise.util.ConditionOperator;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Utility class providing factory methods for creating common predicates to match EquivalencePartition instances.
 * This class is part of the modern DSL for defining conditions on EquivalencePartition instances.
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Match by name
 * Predicate<EquivalencePartition<?>> isSafari = nameIs("Safari");
 * 
 * // Match by parameter name
 * Predicate<EquivalencePartition<?>> isBrowserParam = parameterNameIs("browser");
 * 
 * // Combine predicates
 * Predicate<EquivalencePartition<?>> isSafariInBrowser = 
 *     and(nameIs("Safari"), parameterNameIs("browser"));
 * 
 * // Match by value
 * Predicate<EquivalencePartition<?>> isVersion116 = valueIs("116.0");
 * </pre>
 */
public final class PartitionPredicates {
    
    private PartitionPredicates() {
        // Utility class, no instantiation
    }

    /**
     * Creates a predicate that matches an EquivalencePartition by its name.
     *
     * @param name The name to match
     * @return A predicate that returns true if the partition's name equals the given name
     */
    public static Predicate<EquivalencePartition<?>> nameIs(String name) {
        return partition -> Objects.equals(partition.getName(), name);
    }

    /**
     * Creates a predicate that matches an EquivalencePartition by its parameter name.
     *
     * @param parameterName The parameter name to match
     * @return A predicate that returns true if the partition's parameter name equals the given name
     */
    public static Predicate<EquivalencePartition<?>> parameterNameIs(String parameterName) {
        return partition -> partition.getParentParameter() != null &&
                          Objects.equals(partition.getParentParameter().getName(), parameterName);
    }

    /**
     * Creates a predicate that matches an EquivalencePartition by its value.
     *
     * @param value The value to match
     * @return A predicate that returns true if the partition's value equals the given value
     */
    public static Predicate<EquivalencePartition<?>> valueIs(Object value) {
        return partition -> Objects.equals(partition.getValue(), value);
    }

    /**
     * Creates a predicate that matches an EquivalencePartition by checking if its value is in a collection.
     *
     * @param values The collection of valid values
     * @return A predicate that returns true if the partition's value is in the collection
     */
    public static Predicate<EquivalencePartition<?>> valueIn(Collection<?> values) {
        return partition -> values.contains(partition.getValue());
    }

    /**
     * Creates a predicate that matches an EquivalencePartition by checking if its name is in a collection.
     *
     * @param names The collection of valid names
     * @return A predicate that returns true if the partition's name is in the collection
     */
    public static Predicate<EquivalencePartition<?>> nameIn(Collection<String> names) {
        return partition -> names.contains(partition.getName());
    }

    /**
     * Creates a predicate that matches an EquivalencePartition by checking if its name starts with a prefix.
     *
     * @param prefix The prefix to match
     * @return A predicate that returns true if the partition's name starts with the prefix
     */
    public static Predicate<EquivalencePartition<?>> nameStartsWith(String prefix) {
        return partition -> partition.getName().startsWith(prefix);
    }

    /**
     * Creates a predicate that matches an EquivalencePartition by checking if its value contains a substring.
     *
     * @param substring The substring to search for
     * @return A predicate that returns true if the partition's value contains the substring
     */
    public static Predicate<EquivalencePartition<?>> valueContains(String substring) {
        return partition -> partition.getValue() != null &&
                          partition.getValue().toString().contains(substring);
    }

    /**
     * Combines multiple predicates with AND logic.
     *
     * @param predicates The predicates to combine
     * @return A predicate that returns true only if all given predicates return true
     */
    @SafeVarargs
    public static Predicate<EquivalencePartition<?>> and(Predicate<EquivalencePartition<?>>... predicates) {
        return partition -> {
            for (Predicate<EquivalencePartition<?>> predicate : predicates) {
                if (!predicate.test(partition)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Combines multiple predicates with OR logic.
     *
     * @param predicates The predicates to combine
     * @return A predicate that returns true if any of the given predicates returns true
     */
    @SafeVarargs
    public static Predicate<EquivalencePartition<?>> or(Predicate<EquivalencePartition<?>>... predicates) {
        return partition -> {
            for (Predicate<EquivalencePartition<?>> predicate : predicates) {
                if (predicate.test(partition)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Negates a predicate.
     *
     * @param predicate The predicate to negate
     * @return A predicate that returns the opposite of the given predicate
     */
    public static Predicate<EquivalencePartition<?>> not(Predicate<EquivalencePartition<?>> predicate) {
        return predicate.negate();
    }

    /**
     * Creates a predicate from a legacy PartitionMatcher.
     * This is a bridge method to help transition from matcher-based to predicate-based code.
     *
     * @param matcher The legacy matcher to convert
     * @return A predicate that delegates to the matcher
     */
    public static Predicate<EquivalencePartition<?>> fromMatcher(PartitionMatcher matcher) {
        return matcher;
    }

    /**
     * Creates a predicate that matches a field using a legacy Field enum and operator.
     * This is a bridge method to help transition from matcher-based to predicate-based code.
     *
     * @param field The field to match against
     * @param op The operator to use
     * @param value The value to compare with
     * @return A predicate that performs the match
     */
    public static Predicate<EquivalencePartition<?>> matches(
            PartitionMatcher.Field field,
            ConditionOperator op,
            Object value) {
        return new PartitionMatcher(field, op, value);
    }

    /**
     * Creates a predicate that matches two fields using legacy Field enums and operators.
     * This is a bridge method to help transition from matcher-based to predicate-based code.
     *
     * @param field1 First field to match
     * @param op1 First operator
     * @param value1 First value
     * @param field2 Second field to match
     * @param op2 Second operator
     * @param value2 Second value
     * @return A predicate that performs both matches
     */
    public static Predicate<EquivalencePartition<?>> matches(
            PartitionMatcher.Field field1,
            ConditionOperator op1,
            Object value1,
            PartitionMatcher.Field field2,
            ConditionOperator op2,
            Object value2) {
        return new PartitionMatcher(field1, op1, value1, field2, op2, value2);
    }
} 