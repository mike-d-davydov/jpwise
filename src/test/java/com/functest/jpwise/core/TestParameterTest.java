package com.functest.jpwise.core;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link TestParameter} class.
 */
public class TestParameterTest {
    private TestParameter parameter;
    private SimpleValue<String> chrome;
    private SimpleValue<String> firefox;
    private SimpleValue<String> safari;

    @BeforeMethod
    public void setUp() {
        chrome = SimpleValue.of("Chrome");
        firefox = SimpleValue.of("Firefox");
        safari = SimpleValue.of("Safari");
        parameter = new TestParameter("browser", Arrays.asList(chrome, firefox, safari));
    }

    @Test
    public void testConstructorSetsName() {
        assertEquals(parameter.getName(), "browser", "Parameter name should be set correctly");
    }

    @Test
    public void testConstructorSetsValues() {
        List<ParameterValue<?>> values = parameter.getValues();
        assertEquals(values.size(), 3, "Should have correct number of values");
        assertTrue(values.contains(chrome), "Should contain Chrome value");
        assertTrue(values.contains(firefox), "Should contain Firefox value");
        assertTrue(values.contains(safari), "Should contain Safari value");
    }

    @Test
    public void testConstructorSetsParentParameter() {
        for (ParameterValue<?> value : parameter.getValues()) {
            assertEquals(value.getParentParameter(), parameter, 
                "Each value should reference the parameter as parent");
        }
    }

    @Test
    public void testGetValueByName() {
        assertEquals(parameter.getValueByName("Chrome"), chrome, 
            "Should find value by exact name match");
        assertEquals(parameter.getValueByName("Firefox"), firefox, 
            "Should find value by exact name match");
        assertNull(parameter.getValueByName("NonExistent"), 
            "Should return null for non-existent value name");
    }

    @Test
    public void testGetValueByIndex() {
        List<ParameterValue<?>> values = parameter.getValues();
        for (int i = 0; i < values.size(); i++) {
            assertEquals(parameter.getValueByIndex(i), values.get(i), 
                "Should return correct value at index " + i);
        }
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testGetValueByIndexThrowsException() {
        parameter.getValueByIndex(3); // Should throw exception for invalid index
    }

    @Test
    public void testAreCompatibleWithNoRules() {
        assertTrue(parameter.areCompatible(chrome, firefox), 
            "Values should be compatible when no rules are defined");
        assertTrue(parameter.areCompatible(firefox, safari), 
            "Values should be compatible when no rules are defined");
    }

    @Test
    public void testAreCompatibleWithRules() {
        // Create a parameter with a rule that Safari is only compatible with MacOS
        SimpleValue<String> macOS = SimpleValue.of("MacOS");
        SimpleValue<String> windows = SimpleValue.of("Windows");

        List<CompatibilityPredicate> rules = Arrays.asList(
            (v1, v2) -> {
                if (v1.getValue().equals("Safari")) {
                    return v2.getValue().equals("MacOS");
                }
                if (v2.getValue().equals("Safari")) {
                    return v1.getValue().equals("MacOS");
                }
                return true;
            }
        );

        TestParameter browserWithRules = new TestParameter("browser", 
            Arrays.asList(chrome, firefox, safari), rules);

        assertTrue(browserWithRules.areCompatible(chrome, windows), 
            "Chrome should be compatible with Windows");
        assertFalse(browserWithRules.areCompatible(safari, windows), 
            "Safari should not be compatible with Windows");
        assertTrue(browserWithRules.areCompatible(safari, macOS), 
            "Safari should be compatible with MacOS");
    }

    @Test
    public void testValuesAreImmutable() {
        List<ParameterValue<?>> values = parameter.getValues();
        try {
            values.add(SimpleValue.of("Edge"));
            fail("Should not be able to modify the values list");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
} 