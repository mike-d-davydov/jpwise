package com.functest.jpwise.core;

/**
 * @author DavydovMD
 * Date: 20.06.13
 * Time: 14:16
 */
public interface CompatibilityPredicate {
    boolean isCompatible(ParameterValue thisValue, ParameterValue thatValue);
}
