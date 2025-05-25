package com.functest.jpwise;

import com.functest.jpwise.core.ParameterValue;
import com.functest.jpwise.core.ParameterValueMatcher;
import com.functest.jpwise.core.SimpleValue;
import com.functest.jpwise.core.TestParameter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.functest.jpwise.core.ParameterValueMatcher.Field.*;
import static com.functest.jpwise.util.ConditionOperator.*;
import static org.testng.Assert.*;

/**
 * Tests for ParameterValueMatcher focusing on matching equivalence classes and their values.
 */
public class ParameterValueMatcherTest {
    private TestParameter browserParam;
    private TestParameter osParam;
    private TestParameter resolutionParam;

    @BeforeMethod
    public void setUp() {
        // Browser equivalence classes (each represents a browser family)
        ParameterValue<String> chromeBrowser = SimpleValue.of("Chrome", "116.0");  // Chrome family, specific version
        ParameterValue<String> firefoxBrowser = SimpleValue.of("Firefox", "118.0"); // Firefox family, specific version
        browserParam = new TestParameter("browser", Arrays.<ParameterValue<?>>asList(chromeBrowser, firefoxBrowser));

        // OS equivalence classes (each represents an OS family)
        ParameterValue<String> windowsOS = SimpleValue.of("Windows", "10.0.19045"); // Windows family, specific build
        ParameterValue<String> macOS = SimpleValue.of("macOS", "14.1"); // macOS family, specific version
        osParam = new TestParameter("os", Arrays.<ParameterValue<?>>asList(windowsOS, macOS));

        // Resolution equivalence classes (each represents a resolution category)
        ParameterValue<String> hdResolution = SimpleValue.of("HD", "1920x1080"); // HD class, specific resolution
        ParameterValue<String> fourKResolution = SimpleValue.of("4K", "3840x2160"); // 4K class, specific resolution
        resolutionParam = new TestParameter("resolution", Arrays.<ParameterValue<?>>asList(hdResolution, fourKResolution));
    }

    @Test
    public void testMatchEquivalenceClass() {
        // Match by equivalence class name (browser family)
        ParameterValueMatcher chromeMatcher = new ParameterValueMatcher(ValueName, EQ, "Chrome");
        assertTrue(chromeMatcher.matches(browserParam.getValueByName("Chrome")),
            "Should match Chrome browser family regardless of version");
    }

    @Test
    public void testMatchSpecificValue() {
        // Match by specific value (exact version)
        ParameterValueMatcher versionMatcher = new ParameterValueMatcher(Value, EQ, "116.0");
        assertTrue(versionMatcher.matches(browserParam.getValueByName("Chrome")),
            "Should match specific Chrome version");
        assertFalse(versionMatcher.matches(browserParam.getValueByName("Firefox")),
            "Should not match different browser version");
    }

    @Test
    public void testMatchOSFamily() {
        // Match by OS family and specific build
        @SuppressWarnings("unchecked")
        ParameterValue<String> windowsValue = (ParameterValue<String>) osParam.getValueByName("Windows");
        
        // Match family
        ParameterValueMatcher familyMatcher = new ParameterValueMatcher(ValueName, EQ, "Windows");
        assertTrue(familyMatcher.matches(windowsValue),
            "Should match Windows family");

        // Match specific build
        ParameterValueMatcher buildMatcher = new ParameterValueMatcher(Value, EQ, "10.0.19045");
        assertTrue(buildMatcher.matches(windowsValue),
            "Should match specific Windows build");
    }

    @Test
    public void testMatchResolutionCategory() {
        // Match by resolution category and specific value
        @SuppressWarnings("unchecked")
        ParameterValue<String> hdValue = (ParameterValue<String>) resolutionParam.getValueByName("HD");
        
        // Match category
        ParameterValueMatcher categoryMatcher = new ParameterValueMatcher(ValueName, EQ, "HD");
        assertTrue(categoryMatcher.matches(hdValue),
            "Should match HD resolution category");

        // Match specific resolution
        ParameterValueMatcher resolutionMatcher = new ParameterValueMatcher(Value, EQ, "1920x1080");
        assertTrue(resolutionMatcher.matches(hdValue),
            "Should match specific resolution value");
    }

    @Test
    public void testMatchMultipleConditions() {
        // Match both equivalence class and specific value
        ParameterValueMatcher complexMatcher = new ParameterValueMatcher(
            ValueName, EQ, "Chrome",  // Match Chrome family
            Value, EQ, "116.0"       // Match specific version
        );

        assertTrue(complexMatcher.matches(browserParam.getValueByName("Chrome")),
            "Should match both Chrome family and specific version");
        assertFalse(complexMatcher.matches(browserParam.getValueByName("Firefox")),
            "Should not match different browser family");
    }

    @Test
    public void testMatchParameterName() {
        // Match by parameter name (useful for cross-parameter rules)
        ParameterValueMatcher paramMatcher = new ParameterValueMatcher(
            ParameterName, EQ, "browser"
        );

        assertTrue(paramMatcher.matches(browserParam.getValueByName("Chrome")),
            "Should match browser parameter");
        assertFalse(paramMatcher.matches(osParam.getValueByName("Windows")),
            "Should not match OS parameter");
    }

    @Test
    public void testMatchInCollection() {
        // Match value in a collection of valid options
        ParameterValueMatcher inMatcher = new ParameterValueMatcher(
            Value, IN, Arrays.asList("116.0", "118.0")
        );

        assertTrue(inMatcher.matches(browserParam.getValueByName("Chrome")),
            "Should match version in valid list");
        assertTrue(inMatcher.matches(browserParam.getValueByName("Firefox")),
            "Should match version in valid list");
    }

    @Test
    public void testMatchNotInCollection() {
        // Match value not in a collection
        ParameterValueMatcher notInMatcher = new ParameterValueMatcher(
            Value, NOT_IN, Arrays.asList("119.0", "120.0")
        );

        assertTrue(notInMatcher.matches(browserParam.getValueByName("Chrome")),
            "Should match version not in list");
        assertTrue(notInMatcher.matches(browserParam.getValueByName("Firefox")),
            "Should match version not in list");
    }
}
