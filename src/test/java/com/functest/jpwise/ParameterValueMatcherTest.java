package com.functest.jpwise;

import com.functest.jpwise.core.EquivalencePartition;
import com.functest.jpwise.core.PartitionMatcher;
import com.functest.jpwise.core.SimpleValue;
import com.functest.jpwise.core.TestParameter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.functest.jpwise.core.PartitionMatcher.Field.*;
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
        EquivalencePartition<String> chromeBrowser = SimpleValue.of("Chrome", "116.0");  // Chrome family, specific version
        EquivalencePartition<String> firefoxBrowser = SimpleValue.of("Firefox", "118.0"); // Firefox family, specific version
        browserParam = new TestParameter("browser", Arrays.<EquivalencePartition<?>>asList(chromeBrowser, firefoxBrowser));

        // OS equivalence classes (each represents an OS family)
        EquivalencePartition<String> windowsOS = SimpleValue.of("Windows", "10.0.19045"); // Windows family, specific build
        EquivalencePartition<String> macOS = SimpleValue.of("macOS", "14.1"); // macOS family, specific version
        osParam = new TestParameter("os", Arrays.<EquivalencePartition<?>>asList(windowsOS, macOS));

        // Resolution equivalence classes (each represents a resolution category)
        EquivalencePartition<String> hdResolution = SimpleValue.of("HD", "1920x1080"); // HD class, specific resolution
        EquivalencePartition<String> fourKResolution = SimpleValue.of("4K", "3840x2160"); // 4K class, specific resolution
        resolutionParam = new TestParameter("resolution", Arrays.<EquivalencePartition<?>>asList(hdResolution, fourKResolution));
    }

    @Test
    public void testMatchEquivalenceClass() {
        // Match by equivalence class name (browser family)
        PartitionMatcher chromeMatcher = new PartitionMatcher(ValueName, EQ, "Chrome");
        assertTrue(chromeMatcher.matches(browserParam.getPartitionByName("Chrome")),
            "Should match Chrome browser");
    }

    @Test
    public void testMatchSpecificValue() {
        // Match by specific value (exact version)
        PartitionMatcher versionMatcher = new PartitionMatcher(Value, EQ, "116.0");
        assertTrue(versionMatcher.matches(browserParam.getPartitionByName("Chrome")),
            "Should match Chrome browser");
        assertFalse(versionMatcher.matches(browserParam.getPartitionByName("Firefox")),
            "Should not match Firefox browser");
    }

    @Test
    public void testMatchOSFamily() {
        // Match by OS family and specific build
        EquivalencePartition<String> windowsValue = (EquivalencePartition<String>) osParam.getPartitionByName("Windows");
        
        // Match family
        PartitionMatcher familyMatcher = new PartitionMatcher(ValueName, EQ, "Windows");
        assertTrue(familyMatcher.matches(windowsValue),
            "Should match Windows family");

        // Match specific build
        PartitionMatcher buildMatcher = new PartitionMatcher(Value, EQ, "10.0.19045");
        assertTrue(buildMatcher.matches(windowsValue),
            "Should match specific Windows build");
    }

    @Test
    public void testMatchResolutionCategory() {
        // Match by resolution category and specific value
        EquivalencePartition<String> hdValue = (EquivalencePartition<String>) resolutionParam.getPartitionByName("HD");
        
        // Match category
        PartitionMatcher categoryMatcher = new PartitionMatcher(ValueName, EQ, "HD");
        assertTrue(categoryMatcher.matches(hdValue),
            "Should match HD resolution category");

        // Match specific resolution
        PartitionMatcher resolutionMatcher = new PartitionMatcher(Value, EQ, "1920x1080");
        assertTrue(resolutionMatcher.matches(hdValue),
            "Should match specific resolution value");
    }

    @Test
    public void testMatchMultipleConditions() {
        // Match both equivalence class and specific value
        PartitionMatcher complexMatcher = new PartitionMatcher(
            ValueName, EQ, "Chrome",  // Match Chrome family
            Value, EQ, "116.0"       // Match specific version
        );

        assertTrue(complexMatcher.matches(browserParam.getPartitionByName("Chrome")),
            "Should match Chrome browser");
        assertFalse(complexMatcher.matches(browserParam.getPartitionByName("Firefox")),
            "Should not match Firefox browser");
    }

    @Test
    public void testMatchParameterName() {
        // Match by parameter name (useful for cross-parameter rules)
        PartitionMatcher paramMatcher = new PartitionMatcher(
            ParameterName, EQ, "browser"
        );

        assertTrue(paramMatcher.matches(browserParam.getPartitionByName("Chrome")),
            "Should match Chrome browser");
        assertFalse(paramMatcher.matches(osParam.getPartitionByName("Windows")),
            "Should not match Windows OS");
    }

    @Test
    public void testMatchInCollection() {
        // Match value in a collection of valid options
        PartitionMatcher inMatcher = new PartitionMatcher(
            Value, IN, Arrays.asList("116.0", "118.0")
        );

        assertTrue(inMatcher.matches(browserParam.getPartitionByName("Chrome")),
            "Should match Chrome browser");
        assertTrue(inMatcher.matches(browserParam.getPartitionByName("Firefox")),
            "Should match Firefox browser");
    }

    @Test
    public void testMatchNotInCollection() {
        // Match value not in a collection
        PartitionMatcher notInMatcher = new PartitionMatcher(
            Value, NOT_IN, Arrays.asList("119.0", "120.0")
        );

        assertTrue(notInMatcher.matches(browserParam.getPartitionByName("Chrome")),
            "Should match Chrome browser");
        assertTrue(notInMatcher.matches(browserParam.getPartitionByName("Firefox")),
            "Should match Firefox browser");
    }
}
