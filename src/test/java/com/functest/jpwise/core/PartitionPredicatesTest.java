package com.functest.jpwise.core;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.function.Predicate;

import static com.functest.jpwise.core.PartitionPredicates.*;
import static org.testng.Assert.*;

/**
 * Tests for the PartitionPredicates utility class.
 */
public class PartitionPredicatesTest {
    private TestParameter browser;
    private TestParameter os;
    private SimpleValue<String> chrome;
    private SimpleValue<String> firefox;
    private SimpleValue<String> safari;
    private SimpleValue<String> windows;
    private SimpleValue<String> macOS;

    @BeforeMethod
    public void setUp() {
        // Create test values
        chrome = SimpleValue.of("Chrome", "116.0");
        firefox = SimpleValue.of("Firefox", "118.0");
        safari = SimpleValue.of("Safari", "17.0");
        windows = SimpleValue.of("Windows", "10.0");
        macOS = SimpleValue.of("macOS", "14.1");

        // Create test parameters
        browser = new TestParameter("browser", Arrays.asList(chrome, firefox, safari));
        os = new TestParameter("os", Arrays.asList(windows, macOS));
    }

    @Test
    public void testNameIs() {
        Predicate<EquivalencePartition<?>> isSafari = nameIs("Safari");
        assertTrue(isSafari.test(safari), "Should match Safari by name");
        assertFalse(isSafari.test(chrome), "Should not match Chrome");
    }

    @Test
    public void testParentNameIs() {
        Predicate<EquivalencePartition<?>> isBrowserParam = parentNameIs("browser");
        assertTrue(isBrowserParam.test(chrome), "Should match browser parameter");
        assertFalse(isBrowserParam.test(windows), "Should not match OS parameter");
    }

    @Test
    public void testValueIs() {
        Predicate<EquivalencePartition<?>> isVersion116 = valueIs("116.0");
        assertTrue(isVersion116.test(chrome), "Should match Chrome version");
        assertFalse(isVersion116.test(firefox), "Should not match Firefox version");
    }

    @Test
    public void testValueIn() {
        Predicate<EquivalencePartition<?>> validVersions = valueIn(Arrays.asList("116.0", "118.0"));
        assertTrue(validVersions.test(chrome), "Should match Chrome version");
        assertTrue(validVersions.test(firefox), "Should match Firefox version");
        assertFalse(validVersions.test(safari), "Should not match Safari version");
    }

    @Test
    public void testNameIn() {
        Predicate<EquivalencePartition<?>> validBrowsers = nameIn(Arrays.asList("Chrome", "Firefox"));
        assertTrue(validBrowsers.test(chrome), "Should match Chrome");
        assertTrue(validBrowsers.test(firefox), "Should match Firefox");
        assertFalse(validBrowsers.test(safari), "Should not match Safari");
    }

    @Test
    public void testNameStartsWith() {
        Predicate<EquivalencePartition<?>> windowsVersions = nameStartsWith("Windows");
        assertTrue(windowsVersions.test(windows), "Should match Windows");
        assertFalse(windowsVersions.test(macOS), "Should not match macOS");
    }

    @Test
    public void testValueContains() {
        Predicate<EquivalencePartition<?>> contains116 = valueContains("116");
        assertTrue(contains116.test(chrome), "Should match Chrome version containing 116");
        assertFalse(contains116.test(firefox), "Should not match Firefox version");
    }

    @Test
    public void testAnd() {
        Predicate<EquivalencePartition<?>> chromeInBrowser = and(
            nameIs("Chrome"),
            parentNameIs("browser")
        );
        assertTrue(chromeInBrowser.test(chrome), "Should match Chrome in browser parameter");
        assertFalse(chromeInBrowser.test(firefox), "Should not match Firefox");
        assertFalse(chromeInBrowser.test(windows), "Should not match Windows in OS parameter");
    }

    @Test
    public void testOr() {
        Predicate<EquivalencePartition<?>> chromeOrFirefox = or(
            nameIs("Chrome"),
            nameIs("Firefox")
        );
        assertTrue(chromeOrFirefox.test(chrome), "Should match Chrome");
        assertTrue(chromeOrFirefox.test(firefox), "Should match Firefox");
        assertFalse(chromeOrFirefox.test(safari), "Should not match Safari");
    }

    @Test
    public void testNot() {
        Predicate<EquivalencePartition<?>> notSafari = not(nameIs("Safari"));
        assertTrue(notSafari.test(chrome), "Should match Chrome");
        assertTrue(notSafari.test(firefox), "Should match Firefox");
        assertFalse(notSafari.test(safari), "Should not match Safari");
    }

    @Test
    public void testComplexPredicate() {
        // Test a complex predicate that matches:
        // (Chrome or Firefox) in browser parameter with version containing "116" or "118"
        Predicate<EquivalencePartition<?>> complexPredicate = and(
            parentNameIs("browser"),
            or(nameIs("Chrome"), nameIs("Firefox")),
            or(valueContains("116"), valueContains("118"))
        );

        assertTrue(complexPredicate.test(chrome), "Should match Chrome with version 116");
        assertTrue(complexPredicate.test(firefox), "Should match Firefox with version 118");
        assertFalse(complexPredicate.test(safari), "Should not match Safari");
        assertFalse(complexPredicate.test(windows), "Should not match Windows in OS parameter");
    }
} 