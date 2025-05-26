package com.functest.jpwise.util;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * @author DavydovMD Date: 10.04.13 Time: 11:08
 */
public enum ConditionOperator {
  EQ(" = "),
  NEQ(" <> "),
  IN(" IN "),
  NOT_IN(" NOT IN "),
  IS_NULL(" IS NULL "),
  IS_NOT_NULL(" IS NOT NULL "),
  CONTAINS(" CONTAINS "),
  CONTAINS_ALL(" CONTAINS ALL ");

  private String desc;

  ConditionOperator(String desc) {
    this.desc = desc;
  }

  public boolean nullableEquals(@Nullable Object a, @Nullable Object b) {
    if (((a == null) && (b != null)) || ((b == null) && (a != null))) return false;

    return (a == b) || (a != null && a.equals(b));
  }

  public boolean apply(final Object left, final Object right) {
    switch (this) {
      case EQ:
        return nullableEquals(left, right);
      case NEQ:
        return !nullableEquals(left, right);
      case IN:
        if (right == null) {
          return (left == null);
        }
        return ((Collection<?>) right).contains(left);
      case NOT_IN:
        if (right == null) {
          return (left != null);
        }
        return !((Collection<?>) right).contains(left);
      case IS_NULL:
        return left == null;
      case IS_NOT_NULL:
        return left != null;
      case CONTAINS:
        if (left == null || right == null) {
          return false;
        }
        if (left instanceof Collection) {
          return ((Collection<?>) left).contains(right);
        }
        return left.toString().contains(right.toString());
      case CONTAINS_ALL:
        if (left == null || right == null) {
          return false;
        }
        if (!(left instanceof Collection) || !(right instanceof Collection)) {
          return false;
        }
        return ((Collection<?>) left).containsAll((Collection<?>) right);
    }
    throw new UnsupportedOperationException(
        "Not supported: " + String.valueOf(left) + " " + this + " " + String.valueOf(right));
  }

  public String getDesc() {
    return desc;
  }
}
