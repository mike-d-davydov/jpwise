package com.functest.jpwise.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import static com.functest.jpwise.core.PartitionPredicates.*;

/**
 * A fluent builder for creating complex predicates that match EquivalencePartition instances.
 * This builder allows chaining multiple conditions using a readable, fluent API.
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Simple condition
 * Predicate<EquivalencePartition<?>> isSafari = 
 *     PartitionConditionBuilder.where()
 *         .nameIs("Safari")
 *         .build();
 * 
 * // Complex condition
 * Predicate<EquivalencePartition<?>> isBrowserVersion116 = 
 *     PartitionConditionBuilder.where()
 *         .parameterNameIs("browser")
 *         .valueContains("116")
 *         .build();
 * 
 * // Multiple conditions with custom predicate
 * Predicate<EquivalencePartition<?>> customCondition = 
 *     PartitionConditionBuilder.where()
 *         .nameIn("Chrome", "Firefox")
 *         .matches(p -> p.getValue() != null)
 *         .build();
 * </pre>
 */
public class PartitionConditionBuilder {
    private Predicate<EquivalencePartition<?>> currentPredicate;

    private PartitionConditionBuilder() {
        this.currentPredicate = partition -> true; // Default to always true
    }

    /**
     * Creates a new builder instance.
     *
     * @return A new PartitionConditionBuilder
     */
    public static PartitionConditionBuilder where() {
        return new PartitionConditionBuilder();
    }

    /**
     * Adds a condition that matches the partition's name.
     *
     * @param name The name to match
     * @return This builder for chaining
     */
    public PartitionConditionBuilder nameIs(String name) {
        return and(PartitionPredicates.nameIs(name));
    }

    /**
     * Adds a condition that matches the partition's parameter name.
     *
     * @param parameterName The parameter name to match
     * @return This builder for chaining
     */
    public PartitionConditionBuilder parameterNameIs(String parameterName) {
        return and(PartitionPredicates.parameterNameIs(parameterName));
    }

    /**
     * Adds a condition that matches the partition's value.
     *
     * @param value The value to match
     * @return This builder for chaining
     */
    public <V> PartitionConditionBuilder valueIs(V value) {
        return and(PartitionPredicates.valueIs(value));
    }

    /**
     * Adds a condition that matches if the partition's name is in a set of names.
     *
     * @param names The names to match against
     * @return This builder for chaining
     */
    public PartitionConditionBuilder nameIn(String... names) {
        return and(PartitionPredicates.nameIn(Arrays.asList(names)));
    }

    /**
     * Adds a condition that matches if the partition's value is in a collection.
     *
     * @param values The values to match against
     * @return This builder for chaining
     */
    public PartitionConditionBuilder valueIn(Collection<?> values) {
        return and(PartitionPredicates.valueIn(values));
    }

    /**
     * Adds a condition that matches if the partition's name starts with a prefix.
     *
     * @param prefix The prefix to match
     * @return This builder for chaining
     */
    public PartitionConditionBuilder nameStartsWith(String prefix) {
        return and(PartitionPredicates.nameStartsWith(prefix));
    }

    /**
     * Adds a condition that matches if the partition's value contains a substring.
     *
     * @param substring The substring to search for
     * @return This builder for chaining
     */
    public PartitionConditionBuilder valueContains(String substring) {
        return and(PartitionPredicates.valueContains(substring));
    }

    /**
     * Adds a custom predicate to the chain of conditions.
     *
     * @param customPredicate The custom predicate to add
     * @return This builder for chaining
     */
    public PartitionConditionBuilder matches(Predicate<EquivalencePartition<?>> customPredicate) {
        return and(customPredicate);
    }

    /**
     * Adds a condition that matches if any of the given predicates match.
     *
     * @param predicates The predicates to combine with OR
     * @return This builder for chaining
     */
    @SafeVarargs
    public final PartitionConditionBuilder anyOf(Predicate<EquivalencePartition<?>>... predicates) {
        return and(PartitionPredicates.or(predicates));
    }

    /**
     * Adds a condition that matches if all of the given predicates match.
     *
     * @param predicates The predicates to combine with AND
     * @return This builder for chaining
     */
    @SafeVarargs
    public final PartitionConditionBuilder allOf(Predicate<EquivalencePartition<?>>... predicates) {
        return and(PartitionPredicates.and(predicates));
    }

    /**
     * Adds a condition that is the negation of the given predicate.
     *
     * @param predicate The predicate to negate
     * @return This builder for chaining
     */
    public PartitionConditionBuilder not(Predicate<EquivalencePartition<?>> predicate) {
        return and(PartitionPredicates.not(predicate));
    }

    /**
     * Builds the final predicate that combines all added conditions.
     *
     * @return The combined predicate
     */
    public Predicate<EquivalencePartition<?>> build() {
        return currentPredicate;
    }

    private PartitionConditionBuilder and(Predicate<EquivalencePartition<?>> predicate) {
        currentPredicate = currentPredicate.and(predicate);
        return this;
    }
} 