package com.functest.jpwise.core;

import static java.util.Objects.requireNonNull;

import com.functest.jpwise.util.ConditionOperator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Matches parameter values based on specified conditions. Used for defining compatibility rules and
 * value constraints.
 *
 * <p>This class implements Predicate for modern usage while maintaining backward compatibility with
 * the original matcher-based approach.
 *
 * @author DavydovMD
 * @since 18.06.13
 */
public class PartitionMatcher implements Predicate<EquivalencePartition<?>> {
  private final Collection<AtomicCondition> conditions;

  /**
   * Creates a matcher with a single condition.
   *
   * @param field The field to match against
   * @param op The operator to use
   * @param value The value to compare with
   */
  public PartitionMatcher(Field field, ConditionOperator op, Object value) {
    AtomicCondition condition = new AtomicCondition(field, op, value);
    this.conditions = new ArrayList<>();
    this.conditions.add(condition);
  }

  /**
   * Creates a matcher with two conditions that must both be satisfied.
   *
   * @param field1 First field to match
   * @param op1 First operator
   * @param value1 First value
   * @param field2 Second field to match
   * @param op2 Second operator
   * @param value2 Second value
   */
  public PartitionMatcher(
      Field field1,
      ConditionOperator op1,
      Object value1,
      Field field2,
      ConditionOperator op2,
      Object value2) {
    AtomicCondition c1 = new AtomicCondition(field1, op1, value1);
    AtomicCondition c2 = new AtomicCondition(field2, op2, value2);

    this.conditions = new ArrayList<>();
    this.conditions.add(c1);
    this.conditions.add(c2);
  }

  /**
   * Creates a matcher with multiple conditions that must all be satisfied.
   *
   * @param conditions List of atomic conditions
   */
  public PartitionMatcher(List<AtomicCondition> conditions) {
    this.conditions = new ArrayList<>(conditions);
  }

  /**
   * Tests if an EquivalencePartition matches all conditions. This method implements the Predicate
   * interface for modern usage.
   *
   * @param partition The partition to test
   * @return true if the partition matches all conditions
   */
  @Override
  public boolean test(EquivalencePartition<?> partition) {
    return matches(partition);
  }

  /**
   * Legacy method for backward compatibility. Checks if a partition matches all conditions in this
   * matcher.
   *
   * @param partition The partition to check
   * @return true if the partition matches all conditions
   */
  public boolean matches(EquivalencePartition<?> partition) {
    for (AtomicCondition condition : conditions) {
      if (!condition.matches(partition)) return false;
    }
    return true;
  }

  /**
   * Combines this matcher with another predicate using AND logic. This is part of the modern
   * predicate-based API.
   *
   * @param other The predicate to combine with
   * @return A new predicate that matches both conditions
   */
  @Override
  public Predicate<EquivalencePartition<?>> and(Predicate<? super EquivalencePartition<?>> other) {
    return Predicate.super.and(other);
  }

  /**
   * Combines this matcher with another predicate using OR logic. This is part of the modern
   * predicate-based API.
   *
   * @param other The predicate to combine with
   * @return A new predicate that matches either condition
   */
  @Override
  public Predicate<EquivalencePartition<?>> or(Predicate<? super EquivalencePartition<?>> other) {
    return Predicate.super.or(other);
  }

  /**
   * Creates a negated version of this matcher. This is part of the modern predicate-based API.
   *
   * @return A new predicate that matches the opposite condition
   */
  @Override
  public Predicate<EquivalencePartition<?>> negate() {
    return Predicate.super.negate();
  }

  /** Defines which field of an EquivalencePartition to match against. */
  public enum Field {
    Value(EquivalencePartition::getValue),
    ValueName(EquivalencePartition::getName),
    ParameterName(v -> requireNonNull(v).getParentParameter().getName());

    private final Function<EquivalencePartition<?>, Object> getterFunc;

    Field(Function<EquivalencePartition<?>, Object> getter) {
      this.getterFunc = getter;
    }

    /**
     * Checks if a field of the EquivalencePartition matches the specified condition.
     *
     * @param partition The partition to check
     * @param op The comparison operator to use
     * @param locatorValue The value to compare against
     * @return true if the field matches the condition
     */
    public boolean matches(
        EquivalencePartition<?> partition, ConditionOperator op, Object locatorValue) {
      return op.apply(getGetterFunc().apply(partition), locatorValue);
    }

    public Function<EquivalencePartition<?>, Object> getGetterFunc() {
      return getterFunc;
    }
  }

  /** Represents a single atomic condition in a matcher. */
  public static class AtomicCondition {
    private final Field type;
    private final ConditionOperator op;
    private final Object value;

    AtomicCondition(Field type, ConditionOperator op, Object value) {
      this.type = type;
      this.op = op;
      this.value = value;
    }

    boolean matches(EquivalencePartition<?> paramValue) {
      return type.matches(paramValue, op, value);
    }

    @Override
    public String toString() {
      return String.format("AtomicCondition{type=%s, op=%s, value=%s}", type, op, value);
    }
  }
}
