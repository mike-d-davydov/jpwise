package com.functest.jpwise.core;

/**
 * A simple equivalence partition that always returns the same value.
 * This is useful for constant values that don't change during testing.
 *
 * @param <T> The type of value this partition represents
 */
public class SimpleValue<T> extends GenericPartition<T> {
    /**
     * Creates a new simple value partition with the same name as its string value.
     *
     * @param value The constant value for this partition
     */
    public SimpleValue(T value) {
        this(String.valueOf(value), value);
    }

    /**
     * Creates a new simple value partition with a specific name and value.
     *
     * @param name The name of this partition
     * @param value The constant value for this partition
     */
    public SimpleValue(String name, T value) {
        super(name, () -> value);
    }

    /**
     * Factory method to create a simple value partition with the same name as its string value.
     *
     * @param value The constant value for this partition
     * @return A new simple value partition
     */
    public static <T> SimpleValue<T> of(T value) {
        return new SimpleValue<>(value);
    }

    /**
     * Factory method to create a simple value partition with a specific name and value.
     *
     * @param name The name of this partition
     * @param value The constant value for this partition
     * @return A new simple value partition
     */
    public static <T> SimpleValue<T> of(String name, T value) {
        return new SimpleValue<>(name, value);
    }

    /**
     * Creates a new SimpleValue where the name is used as the string value.
     * This is a convenience factory method for cases where the value is the same as the name.
     *
     * @param name The name and value of this partition
     * @return A new SimpleValue instance
     */
    public static SimpleValue<String> of(String name) {
        return new SimpleValue<>(name, name);
    }
}
