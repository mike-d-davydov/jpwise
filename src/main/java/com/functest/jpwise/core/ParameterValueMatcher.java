package com.functest.jpwise.core;

import com.functest.jpwise.util.ConditionOperator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * @author DavydovMD
 * Date: 18.06.13
 * Time: 11:11
 */
public class ParameterValueMatcher {
    private Collection<AtomicCondition> conditions = new ArrayList<>();

    public ParameterValueMatcher(Field field, ConditionOperator op, Object value) {
        AtomicCondition condition = new AtomicCondition(field, op, value);

        conditions = new ArrayList<>();
        conditions.add(condition);
    }

    public ParameterValueMatcher(Field field1, ConditionOperator op1, Object value1, Field field2, ConditionOperator op2, Object value2) {
        AtomicCondition c1 = new AtomicCondition(field1, op1, value1);
        AtomicCondition c2 = new AtomicCondition(field2, op2, value2);

        conditions = new ArrayList<>();
        conditions.add(c1);
        conditions.add(c2);

    }

    public ParameterValueMatcher(List<AtomicCondition> conditions) {
        this.conditions = new ArrayList<>(conditions);
    }

    public boolean matches(ParameterValue value) {
        for (AtomicCondition condition : conditions) {
            if (!condition.matches(value)) return false;
        }
        return true;
    }

    public enum Field {
        Value(ParameterValue::getValue), ValueName(ParameterValue::getName), ParameterName(v -> {
            return requireNonNull(v).getParentParameter().getName();
        });

        private Function<ParameterValue, Object> getterFunc;

        Field(Function<ParameterValue, Object> getter) {
            this.getterFunc = getter;
        }

        /**
         * Проверяем , соответствует одно из полей объекта ParameterValue заданному условию
         * В зависимости от типа @locatorValue, сравнивает поле объекта либо по значению, либо по регулярке (если locatorValue - Pattern), либо с помощью Closure
         *
         * @param value        - объект типа ParameterValue, который мы проверяем на соответствие условию
         * @param locatorValue - значение, с которым сравниваем то или иное поле value
         * @return соответствуюет/не соответствует
         */

        public boolean matches(ParameterValue value, ConditionOperator op, Object locatorValue) {
            return op.apply(getGetterFunc().apply(value), locatorValue);
        }

        public Function<ParameterValue, Object> getGetterFunc() {
            return getterFunc;
        }
    }


    public static class AtomicCondition {
        private Field type;
        private ConditionOperator op;
        private Object value;

        AtomicCondition(Field type, ConditionOperator op, Object value) {
            super();
            this.type = type;
            this.op = op;
            this.value = value;
        }

        boolean matches(ParameterValue paramValue) {
            return type.matches(paramValue, op, value);
        }

        @Override
        public String toString() {
            return String.format(
                    "AtomicCondition{type=%s, op=%s, value=%s}",
                    type,
                    op,
                    value
            );
        }
    }
}
