package com.functest.jpwise.core;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for Combination class functionality.
 */
public class CombinationTest {
    private TestParameter browser;
    private TestParameter operatingSystem;
    private TestParameter resolution;
    private SimpleValue<String> chrome;
    private SimpleValue<String> firefox;
    private SimpleValue<String> safari;
    private SimpleValue<String> windows;
    private SimpleValue<String> macOS;
    private SimpleValue<String> linux;
    private SimpleValue<String> hd;
    private SimpleValue<String> uhd;

    @BeforeMethod
    public void setUp() {
        // Initialize test values
        chrome = SimpleValue.of("Chrome");
        firefox = SimpleValue.of("Firefox");
        safari = SimpleValue.of("Safari");
        windows = SimpleValue.of("Windows");
        macOS = SimpleValue.of("macOS");
        linux = SimpleValue.of("Linux");
        hd = SimpleValue.of("1920x1080");
        uhd = SimpleValue.of("3840x2160");

        // Define browser-OS compatibility rules
        List<CompatibilityPredicate> browserOsRules = Arrays.asList(
            (v1, v2) -> {
                // Safari only works with macOS
                if (v1.getName().equals("Safari")) {
                    return v2.getName().equals("macOS");
                }
                if (v2.getName().equals("Safari")) {
                    return v1.getName().equals("macOS");
                }
                // Chrome and Firefox work with all OS
                return true;
            }
        );

        // Create test parameters with compatibility rules
        browser = new TestParameter("browser", Arrays.<ParameterValue<?>>asList(chrome, firefox, safari), browserOsRules);
        operatingSystem = new TestParameter("operatingSystem", Arrays.<ParameterValue<?>>asList(windows, macOS, linux));
        resolution = new TestParameter("resolution", Arrays.<ParameterValue<?>>asList(hd, uhd));
    }

    @Test
    public void testBasicCombination() {
        // Test basic combination creation and value retrieval
        Combination combination = new Combination(3);
        combination.setValue(0, chrome);
        combination.setValue(1, windows);
        combination.setValue(2, hd);

        assertEquals(combination.getValue(0), chrome,
            "First value should be Chrome");
        assertEquals(combination.getValue(1), windows,
            "Second value should be Windows");
        assertEquals(combination.getValue(2), hd,
            "Third value should be HD resolution");
    }

    @Test
    public void testCombinationEquality() {
        // Test combination equality
        Combination combo1 = new Combination(2);
        combo1.setValue(0, chrome);
        combo1.setValue(1, windows);

        Combination combo2 = new Combination(2);
        combo2.setValue(0, chrome);
        combo2.setValue(1, windows);

        assertEquals(combo1, combo2,
            "Identical combinations should be equal");
        assertEquals(combo1.hashCode(), combo2.hashCode(),
            "Equal combinations should have same hash code");
    }

    @Test
    public void testCombinationInequality() {
        // Test combination inequality
        Combination combo1 = new Combination(2);
        combo1.setValue(0, chrome);
        combo1.setValue(1, windows);

        Combination combo2 = new Combination(2);
        combo2.setValue(0, firefox);
        combo2.setValue(1, linux);

        assertNotEquals(combo1, combo2,
            "Different combinations should not be equal");
    }

    @Test
    public void testCompatibilityRulesWithParameters() {
        // Test browser-OS compatibility rules
        assertTrue(browser.areCompatible(chrome, windows),
            "Chrome-Windows combination should be compatible");
        assertTrue(browser.areCompatible(chrome, macOS),
            "Chrome-macOS combination should be compatible");
        assertTrue(browser.areCompatible(chrome, linux),
            "Chrome-Linux combination should be compatible");

        assertTrue(browser.areCompatible(firefox, windows),
            "Firefox-Windows combination should be compatible");
        assertTrue(browser.areCompatible(firefox, macOS),
            "Firefox-macOS combination should be compatible");
        assertTrue(browser.areCompatible(firefox, linux),
            "Firefox-Linux combination should be compatible");

        assertTrue(browser.areCompatible(safari, macOS),
            "Safari-macOS combination should be compatible");
        assertFalse(browser.areCompatible(safari, windows),
            "Safari-Windows combination should not be compatible");
        assertFalse(browser.areCompatible(safari, linux),
            "Safari-Linux combination should not be compatible");
    }

    @Test
    public void testCombinationValidation() {
        // Test valid combinations
        Combination validCombo = new Combination(3);
        validCombo.setValue(0, chrome);
        validCombo.setValue(1, windows);
        validCombo.setValue(2, hd);

        // Create a mock algorithm that uses the browser's compatibility rules
        GenerationAlgorithm algorithm = new GenerationAlgorithm() {
            @Override
            public void generate(TestGenerator testGenerator, int nwise) {}

            @Override
            public boolean isCompatible(ParameterValue v1, ParameterValue v2) {
                if (v1.getParentParameter() == browser) {
                    return browser.areCompatible(v1, v2);
                }
                if (v2.getParentParameter() == browser) {
                    return browser.areCompatible(v2, v1);
                }
                return true;
            }
        };

        assertTrue(validCombo.checkNoConflicts(algorithm),
            "Chrome-Windows-HD combination should be valid");

        // Test invalid combinations
        Combination invalidCombo = new Combination(3);
        invalidCombo.setValue(0, safari);
        invalidCombo.setValue(1, windows);
        invalidCombo.setValue(2, hd);
        assertFalse(invalidCombo.checkNoConflicts(algorithm),
            "Safari-Windows-HD combination should be invalid");
    }

    @Test
    public void testCombinationToString() {
        // Test string representation
        Combination combination = new Combination(3);
        combination.setValue(0, chrome);
        combination.setValue(1, windows);
        combination.setValue(2, hd);

        String expected = "Combination{[browser:Chrome, operatingSystem:Windows, resolution:1920x1080]}";
        assertEquals(combination.toString(), expected,
            "Combination string representation should match expected format");
    }

    @Test
    public void testCombinationKey() {
        // Test combination key generation
        Combination combination = new Combination(3);
        combination.setValue(0, chrome);
        combination.setValue(1, windows);
        combination.setValue(2, hd);

        String expected = "Chrome|Windows|1920x1080";
        assertEquals(combination.getKey(), expected,
            "Combination key should match expected format");
    }

    @Test
    public void testNullValueHandling() {
        // Test handling of null values
        Combination combination = new Combination(2);
        combination.setValue(0, null);
        combination.setValue(1, chrome);

        assertNull(combination.getValue(0),
            "Should handle null values");
        assertEquals(combination.getValue(1), chrome,
            "Should handle non-null values after null");
    }

    @Test
    public void testCombinationSize() {
        // Test combination size
        Combination combination = new Combination(2);
        assertEquals(combination.size(), 2,
            "Empty combination should have size 2");

        combination.setValue(0, chrome);
        combination.setValue(1, windows);
        assertEquals(combination.size(), 2,
            "Combination size should match constructor size");
    }
} 