package com.functest.jpwise.core;

/**
 * @author DavydovMD
 * Date: 17.06.13
 * Time: 15:53
 */
public class SimpleValue implements ParameterValue {
    private volatile static int uniqueId = 0;
    private Object val;
    private int id;
    private String name;
    private TestParameter parentParameter = null;

    public SimpleValue(Object paramVal, String name) {
        super();
        this.id = uniqueId;
        uniqueId++;
        this.val = paramVal;
        this.name = name;
    }

    public static SimpleValue of(Object value) {
        return new SimpleValue(value, String.valueOf(value));
    }

    public static SimpleValue of(String inName, Object value) {
        return new SimpleValue(value, inName);
    }

    public int getId() {
        return id;
    }

    @Override
    public TestParameter getParentParameter() {
        return parentParameter;
    }

    public void setParentParameter(TestParameter parameter) {
        this.parentParameter = parameter;
    }

    @Override
    public Object get() {
        return val;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isCompatibleWith(ParameterValue thatValue) {
        return (parentParameter == null) || parentParameter.areCompatible(this, thatValue);
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(id).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SimpleValue) && (((SimpleValue) obj).getId() == id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append((parentParameter != null) ? parentParameter.getName() : "").append(":");
        sb.append(String.valueOf(val).equals(name) ? String.valueOf(val) : (String.valueOf(name)));
        return sb.toString();
    }
}
