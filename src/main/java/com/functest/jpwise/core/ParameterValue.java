package com.functest.jpwise.core;

/**
 * @author DavydovMD
 * Date: 17.06.13
 * Time: 15:49
 */

public interface ParameterValue<T> {
    TestParameter getParentParameter();

    void setParentParameter(TestParameter parameter);

    T getValue();

    String getName();

    boolean isCompatibleWith(ParameterValue<?> v2);
}
