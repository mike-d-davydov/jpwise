package com.functest.jpwise.core;

import java.util.function.Supplier;

/**
 * A generic implementation of an equivalence partition that uses a supplier function
 * to generate values. This class can be used as a base for specific partition types
 * by providing different value generation strategies.
 *
 * @param <T> The type of values in this partition
 */
public class GenericPartition<T> extends BaseEquivalencePartition<T> {
    private final Supplier<T> valueGenerator;

    /**
     * Creates a new generic partition with a name and a value generator function.
     *
     * @param name The name of this partition
     * @param valueGenerator A function that supplies values for this partition
     */
    public GenericPartition(String name, Supplier<T> valueGenerator) {
        super(name);
        this.valueGenerator = valueGenerator;
    }

    @Override
    public T getValue() {
        return valueGenerator.get();
    }

    /**
     * Factory method to create a new generic partition.
     *
     * @param name The name of this partition
     * @param valueGenerator A function that supplies values for this partition
     * @param <T> The type of values in this partition
     * @return A new generic partition
     */
    public static <T> GenericPartition<T> of(String name, Supplier<T> valueGenerator) {
        return new GenericPartition<>(name, valueGenerator);
    }
} 