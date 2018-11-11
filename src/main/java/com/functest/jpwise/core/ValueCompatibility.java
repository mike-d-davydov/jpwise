package com.functest.jpwise.core;


public class ValueCompatibility {

    /**
     * Parameter value to be used when all other values are inapplicatble
     *
     * @param thisMatcher
     * @return
     */
    public static CompatibilityPredicate thisIs(ParameterValueMatcher thisMatcher) {
        return valuesAre(thisMatcher, null);
    }

    public static CompatibilityPredicate thatIs(ParameterValueMatcher thatMatcher) {
        return valuesAre(null, thatMatcher);
    }

    public static CompatibilityPredicate valuesAre(final ParameterValueMatcher thisMatcher, final ParameterValueMatcher thatMatcher) {
        return (left, right) -> {
            boolean res = true;
            if (thisMatcher != null) {
                res = thisMatcher.matches(left);
            }

            if ((thatMatcher != null) && res) {
                res = thatMatcher.matches(right);
            }

            return res;
        };
    }

}
