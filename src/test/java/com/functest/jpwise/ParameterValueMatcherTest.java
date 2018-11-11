package com.functest.jpwise;

import com.functest.jpwise.core.ParameterValue;
import com.functest.jpwise.core.ParameterValueMatcher;
import com.functest.jpwise.core.SimpleValue;
import com.functest.jpwise.core.TestParameter;
import com.functest.jpwise.util.ConditionOperator;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static com.functest.jpwise.core.ParameterValueMatcher.Field.ParameterName;
import static com.functest.jpwise.util.ConditionOperator.IN;
import static com.functest.jpwise.util.ConditionOperator.NOT_IN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * @author DavydovMD
 * Date: 20.06.13
 * Time: 11:36
 */
@SuppressWarnings("AssertStatement")
public class ParameterValueMatcherTest {

    @Test
    public void valueMatcherTest() {

        ParameterValueMatcher matcher = new ParameterValueMatcher(ParameterValueMatcher.Field.ValueName, ConditionOperator.EQ, "Bob");

        assert matcher.matches(SimpleValue.of("Bob"));
        assert matcher.matches(SimpleValue.of("Bob", "Bobby"));
        assert !matcher.matches(SimpleValue.of("Бобр", "Bob"));

        matcher = new ParameterValueMatcher(ParameterValueMatcher.Field.Value, ConditionOperator.EQ, String.class);

        assert matcher.matches(SimpleValue.of("Строчечка", String.class));
        assert matcher.matches(SimpleValue.of(String.class));
        assert !matcher.matches(SimpleValue.of("Цыфирь", Integer.class));


        TestParameter p1 = new TestParameter("param1", new ArrayList<>(asList(SimpleValue.of("Value1"), SimpleValue.of("Value2"))));

        ParameterValue v1 = p1.getValueByName("Value1");
        ParameterValue v2 = p1.getValueByName("Value2");

        matcher = new ParameterValueMatcher(ParameterValueMatcher.Field.ValueName, ConditionOperator.NEQ, "Value1");

        assert matcher.matches(v2);
        assert !matcher.matches(v1);

        TestParameter p2 = new TestParameter("param2", new ArrayList<>(asList(SimpleValue.of("Value1"), SimpleValue.of("Value2"))));

        matcher = new ParameterValueMatcher(
                ParameterValueMatcher.Field.ValueName, IN, asList("Value1", "Value2"), ParameterName, NOT_IN, singletonList("param2"));
        assert matcher.matches(v1);
        assert matcher.matches(v2);
        assert !matcher.matches(p2.getValueByIndex(0));
        assert !matcher.matches(p2.getValueByIndex(1));
    }
}
