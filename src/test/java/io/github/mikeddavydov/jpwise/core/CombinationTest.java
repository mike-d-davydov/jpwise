package io.github.mikeddavydov.jpwise.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.mikeddavydov.jpwise.algo.PairwiseAlgorithm;

/** Tests for Combination class functionality. */
public class CombinationTest {
  private TestParameter browser;
  private TestParameter operatingSystem;
  private TestParameter resolution;
  private SimpleValue chrome;
  private SimpleValue firefox;
  private SimpleValue safari;
  private SimpleValue windows;
  private SimpleValue macOS;
  private SimpleValue linux;
  private SimpleValue hd;

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

    // Define browser-OS compatibility rules
    List<CompatibilityPredicate> browserOsRules =
        Arrays.asList(
            (v1, v2) -> {
              // Only apply rules if we're dealing with browser and OS parameters
              if (!(v1.getParentParameter().getName().equals("browser")
                  && v2.getParentParameter().getName().equals("operatingSystem"))) {
                return true;
              }

              // Safari only works with macOS
              if (v1.getName().equals("Safari")) {
                return v2.getName().equals("macOS");
              }
              // Chrome and Firefox work with all OS
              return true;
            });

    // Create test parameters with compatibility rules
    browser =
        new TestParameter(
            "browser",
            Arrays.<EquivalencePartition>asList(chrome, firefox, safari),
            browserOsRules);

    // Create OS and resolution parameters (needed for parent parameter references)
    operatingSystem =
        new TestParameter(
            "operatingSystem", Arrays.<EquivalencePartition>asList(windows, macOS, linux));
    resolution = new TestParameter("resolution", Arrays.<EquivalencePartition>asList(hd));
  }

  @Test
  public void testBasicCombination() {
    // Test basic combination creation and value retrieval
    Combination combination = new Combination(3);
    combination.setValue(0, chrome);
    combination.setValue(1, windows);
    combination.setValue(2, hd);

    assertEquals(combination.getValue(0), chrome, "First value should be Chrome");
    assertEquals(combination.getValue(1), windows, "Second value should be Windows");
    assertEquals(combination.getValue(2), hd, "Third value should be HD resolution");
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

    assertEquals(combo1, combo2, "Identical combinations should be equal");
    assertEquals(
        combo1.hashCode(), combo2.hashCode(), "Equal combinations should have same hash code");
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

    assertNotEquals(combo1, combo2, "Different combinations should not be equal");
  }

  @Test
  public void testCompatibilityRulesWithParameters() {
    // Test browser-OS compatibility rules
    assertTrue(
        browser.areCompatible(chrome, windows), "Chrome-Windows combination should be compatible");
    assertTrue(
        browser.areCompatible(chrome, macOS), "Chrome-macOS combination should be compatible");
    assertTrue(
        browser.areCompatible(chrome, linux), "Chrome-Linux combination should be compatible");

    assertTrue(
        browser.areCompatible(firefox, windows),
        "Firefox-Windows combination should be compatible");
    assertTrue(
        browser.areCompatible(firefox, macOS), "Firefox-macOS combination should be compatible");
    assertTrue(
        browser.areCompatible(firefox, linux), "Firefox-Linux combination should be compatible");

    assertTrue(
        browser.areCompatible(safari, macOS), "Safari-macOS combination should be compatible");
    assertFalse(
        browser.areCompatible(safari, windows),
        "Safari-Windows combination should not be compatible");
    assertFalse(
        browser.areCompatible(safari, linux), "Safari-Linux combination should not be compatible");
  }

  @Test
  public void testCombinationValidation() {
    List<TestParameter> testParams = new ArrayList<>();
    testParams.add(browser);
    testParams.add(operatingSystem);
    testParams.add(resolution);

    // Test valid combinations
    Combination validCombo = new Combination(testParams);
    validCombo.setValue(0, chrome);
    validCombo.setValue(1, windows);
    validCombo.setValue(2, hd);

    // Use a concrete GenerationAlgorithm
    GenerationAlgorithm algorithm = new PairwiseAlgorithm();

    assertTrue(
        algorithm.isValidCombination(validCombo), "Chrome-Windows-HD combination should be valid");

    // Test invalid combinations
    Combination invalidCombo = new Combination(testParams);
    invalidCombo.setValue(0, safari);
    invalidCombo.setValue(1, windows);
    invalidCombo.setValue(2, hd);
    assertFalse(
        algorithm.isValidCombination(invalidCombo),
        "Safari-Windows-HD combination should be invalid");
  }

  @Test
  public void testCombinationToString() {
    // Test string representation
    Combination combination = new Combination(3);
    combination.setValue(0, chrome);
    combination.setValue(1, windows);
    combination.setValue(2, hd);

    String expected =
        "Combination{[browser:Chrome, operatingSystem:Windows, resolution:1920x1080]}";
    assertEquals(
        combination.toString(),
        expected,
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
    assertEquals(combination.getKey(), expected, "Combination key should match expected format");
  }

  @Test
  public void testNullValueHandling() {
    // Test handling of null values
    Combination combination = new Combination(2);
    // Assert that setting a null value throws IllegalArgumentException
    assertThrows(IllegalArgumentException.class, () -> combination.setValue(0, null));

    // We can still test setting a non-null value afterwards, though the first part
    // is the main fix
    combination.setValue(1, chrome);
    assertNull(
        combination.getValue(0),
        "Value at index 0 should still be null as it was never successfully set");
    assertEquals(
        combination.getValue(1),
        chrome,
        "Should handle non-null values after failed null set attempt");
  }

  @Test
  public void testCombinationSize() {
    // Test combination size
    Combination combination = new Combination(2);
    assertEquals(combination.size(), 2, "Empty combination should have size 2");

    combination.setValue(0, chrome);
    combination.setValue(1, windows);
    assertEquals(combination.size(), 2, "Combination size should match constructor size");
  }
}
