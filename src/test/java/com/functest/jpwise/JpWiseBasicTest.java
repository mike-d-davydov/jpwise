package com.functest.jpwise;


import com.functest.jpwise.algo.CombinatorialAlgorithm;
import com.functest.jpwise.algo.PairwiseAlgorithm;
import com.functest.jpwise.core.*;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static com.functest.jpwise.core.ParameterValueMatcher.Field.ParameterName;
import static com.functest.jpwise.core.ParameterValueMatcher.Field.Value;
import static com.functest.jpwise.core.ValueCompatibility.thatIs;
import static com.functest.jpwise.core.ValueCompatibility.thisIs;
import static com.functest.jpwise.util.ConditionOperator.EQ;
import static com.functest.jpwise.util.ConditionOperator.NEQ;
import static java.util.Arrays.asList;

/**
 * @author DavydovMD
 * Date: 17.06.13
 * Time: 18:19
 */
@SuppressWarnings("AssertStatement")
public class JpWiseBasicTest {
    private final static Logger logger = LoggerFactory.getLogger(JpWiseBasicTest.class);

    private TestInput getTestInput() {
        TestInput input = new TestInput();
        input.add(new TestParameter("parameterA",
                asList(SimpleValue.of("A1"), SimpleValue.of("A2", 3)))
        );
        input.add(new TestParameter("parameterB",
                ImmutableList.of(SimpleValue.of("b")))
        );
        input.add(new TestParameter("parameterC",
                        asList(
                                SimpleValue.of("C1", 1),
                                SimpleValue.of("C2", null),
                                SimpleValue.of("C3", null),
                                SimpleValue.of("C4", Math.PI))
                )
        );
        input.add(new TestParameter("parameterD",
                asList(
                        SimpleValue.of("D1", "Qwerty")
                        , SimpleValue.of("D2", Boolean.TRUE)
                        , SimpleValue.of("D3", "D-3")
                        , SimpleValue.of("D4", Math.E)
                ),
                asList(
                        // D1  (Qwerty) value is compatible with any other parameters
                        thisIs(new ParameterValueMatcher(Value, EQ, "Qwerty")),
                        // Other D values  are compatible with any values of parameterB and parameterC
                        thatIs(new ParameterValueMatcher(ParameterName, NEQ, "parameterA")),
                        // Values of D except for D1 are only compatible with value "3" of parameterA
                        thatIs(new ParameterValueMatcher(ParameterName, EQ, "parameterA", Value, EQ, 3))
                )
        ));
        return input;
    }

    @Test
    public void testPairs() {
        logger.info("========= Pairwise ==================");

        TestInput input = getTestInput();
        TestGenerator generator = new TestGenerator(input);
        generator.generate(new PairwiseAlgorithm());
        CombinationTable table = generator.result();
        table.combinations();
        assert !table.combinations().isEmpty();
        for (Combination c : table.combinations()) {
            logger.info(String.valueOf(c));
        }

    }

    @Test
    public void testCombi() {
        logger.info("========= Combinatorial ==================");
        TestInput input = getTestInput();

        TestGenerator generator = new TestGenerator(input);
        generator.generate(new CombinatorialAlgorithm(), 99);
        CombinationTable table = generator.result();

        assert !table.combinations().isEmpty();

        for (Combination c : table.combinations()) {
            logger.info(String.valueOf(c));
        }
    }

}
