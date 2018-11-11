package com.functest.jpwise.util;

import java.util.Collection;

/**
 * @author DavydovMD
 * Date: 10.04.13
 * Time: 11:08
 */
public enum ConditionOperator {
    EQ(" = "), NEQ(" <> "), IN(" IN "), NOT_IN(" NOT IN "), IS_NULL(" IS NULL "), IS_NOT_NULL(" IS NOT NULL ");

    private String desc;


    ConditionOperator(String desc) {
        this.desc = desc;
    }

    public boolean nullableEquals(Object a, Object b) {
        if (
                ((a == null) && (b != null))
                        ||
                        ((b == null) && (a != null))
        ) return false;

        return ((a == null) && (b == null)) || (a.equals(b));

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
                return ((Collection) right).contains(left);
            case NOT_IN:
                if (right == null) {
                    return (left != null);
                }
                return !((Collection) right).contains(left);
            case IS_NULL:
                return left == null;
            case IS_NOT_NULL:
                return left != null;
        }
        throw new UnsupportedOperationException("Not supported: " + String.valueOf(left) + " " + this + " " + String.valueOf(right));
    }

    public String getDesc() {
        return desc;
    }
}
