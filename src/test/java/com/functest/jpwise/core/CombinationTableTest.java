package com.functest.jpwise.core;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit tests for {@link CombinationTable} class. */
public class CombinationTableTest {
  private CombinationTable table;
  private Combination combination1;
  private Combination combination2;
  private SimpleValue chrome;
  private SimpleValue firefox;
  private SimpleValue windows;
  private SimpleValue linux;
  private TestParameter browser;
  private TestParameter os;

  @BeforeMethod
  public void setUp() {
    table = new CombinationTable();

    // Create test values and parameters
    chrome = SimpleValue.of("Chrome");
    firefox = SimpleValue.of("Firefox");
    windows = SimpleValue.of("Windows");
    linux = SimpleValue.of("Linux");

    // Create parameters with their values
    browser = new TestParameter("browser", Arrays.<EquivalencePartition>asList(chrome, firefox));
    os = new TestParameter("os", Arrays.<EquivalencePartition>asList(windows, linux));

    // Create test combinations using the parameters
    combination1 = new Combination(2);
    combination1.setValue(
        0, browser.getPartitionByName("Chrome")); // Use parameter to get partition
    combination1.setValue(1, os.getPartitionByName("Windows")); // Use parameter to get partition

    combination2 = new Combination(2);
    combination2.setValue(
        0, browser.getPartitionByName("Firefox")); // Use parameter to get partition
    combination2.setValue(1, os.getPartitionByName("Linux")); // Use parameter to get partition
  }

  @Test
  public void testNewTableIsEmpty() {
    assertEquals(table.size(), 0, "New table should be empty");
    assertTrue(table.combinations().isEmpty(), "New table should have no combinations");
  }

  @Test
  public void testAddAndSize() {
    table.add(combination1);
    assertEquals(table.size(), 1, "Size should be 1 after adding one combination");

    table.add(combination2);
    assertEquals(table.size(), 2, "Size should be 2 after adding two combinations");
  }

  @Test
  public void testCombinations() {
    table.add(combination1);
    table.add(combination2);

    List<Combination> combinations = table.combinations();
    assertEquals(combinations.size(), 2, "Should return all combinations");
    assertTrue(combinations.contains(combination1), "Should contain first combination");
    assertTrue(combinations.contains(combination2), "Should contain second combination");
  }

  @Test
  public void testAsRowMapList() {
    table.add(combination1);
    table.add(combination2);

    List<Map<String, Object>> rows = table.asRowMapList();
    assertEquals(rows.size(), 2, "Should create one map per combination");

    // Check first row
    Map<String, Object> row1 = rows.get(0);
    assertEquals(row1.get("browser"), chrome.getValue(), "Should map browser parameter correctly");
    assertEquals(row1.get("os"), windows.getValue(), "Should map os parameter correctly");
    assertTrue(
        row1.containsKey("combination_description"), "Should include combination description");

    // Check second row
    Map<String, Object> row2 = rows.get(1);
    assertEquals(row2.get("browser"), firefox.getValue(), "Should map browser parameter correctly");
    assertEquals(row2.get("os"), linux.getValue(), "Should map os parameter correctly");
    assertTrue(
        row2.containsKey("combination_description"), "Should include combination description");
  }

  @Test
  public void testAsDataProvider() {
    table.add(combination1);
    table.add(combination2);

    Object[][] data = table.asDataProvider();
    assertEquals(data.length, 2, "Should create one row per combination");

    // Check first row
    Object[] row1 = data[0];
    assertTrue(
        row1[0].toString().contains("Chrome"), "First element should be combination description");
    assertEquals(row1[1], chrome.getValue(), "Should include parameter values");
    assertEquals(row1[2], windows.getValue(), "Should include parameter values");

    // Check second row
    Object[] row2 = data[1];
    assertTrue(
        row2[0].toString().contains("Firefox"), "First element should be combination description");
    assertEquals(row2[1], firefox.getValue(), "Should include parameter values");
    assertEquals(row2[2], linux.getValue(), "Should include parameter values");
  }

  @Test
  public void testBreadthWithEmptyTable() {
    assertEquals(table.breadth(), -1, "Should return -1 for empty table");
  }

  @Test
  public void testBreadthWithCombinations() {
    table.add(combination1);
    assertEquals(table.breadth(), 2, "Should return number of parameters");
  }

  @Test
  public void testSpanWithEmptyTable() {
    assertEquals(table.span(), -1, "Should return -1 for empty table");
  }

  @Test
  public void testSpanWithCombinations() {
    table.add(combination1);
    table.add(combination2);

    // We have 2 unique pairs:
    // Chrome-Windows
    // Firefox-Linux
    assertEquals(table.span(), 2, "Should count unique parameter value pairs");
  }

  @Test
  public void testSpanWithOverlappingCombinations() {
    // Create a combination that reuses some partitions
    Combination combination3 = new Combination(2);
    combination3.setValue(
        0, browser.getPartitionByName("Chrome")); // Use parameter to get partition
    combination3.setValue(1, os.getPartitionByName("Linux")); // Use parameter to get partition

    table.add(combination1); // Chrome-Windows
    table.add(combination2); // Firefox-Linux
    table.add(combination3); // Chrome-Linux

    // We now have 3 unique pairs:
    // Chrome-Windows
    // Firefox-Linux
    // Chrome-Linux
    assertEquals(table.span(), 3, "Should count unique parameter value pairs");
  }

  @Test
  public void testToString() {
    assertEquals(
        table.toString(),
        "CombinationTable{0 combinations.}",
        "Should format empty table correctly");

    table.add(combination1);
    assertTrue(table.toString().contains("1 combinations"), "Should include combination count");
    assertTrue(table.toString().contains("Chrome"), "Should include first combination details");
  }

  @Test
  public void testEquivalencePartitionParentRelationship() {
    table.add(combination1);

    // Verify that values in combinations maintain their parameter relationships
    EquivalencePartition browserValue = combination1.getValue(0);
    EquivalencePartition osValue = combination1.getValue(1);

    assertEquals(
        browserValue.getParentParameter(),
        browser,
        "Browser partition should reference browser parameter");
    assertEquals(osValue.getParentParameter(), os, "OS partition should reference os parameter");
  }
}
