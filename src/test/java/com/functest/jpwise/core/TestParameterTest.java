package com.functest.jpwise.core;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit tests for {@link TestParameter} class. */
public class TestParameterTest {
  private TestParameter parameter;
  private SimpleValue chrome;
  private SimpleValue firefox;
  private SimpleValue safari;

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
  public void testConstructorSetsPartitions() {
    List<EquivalencePartition> partitions = parameter.getPartitions();
    assertEquals(partitions.size(), 3, "Should have correct number of partitions");
    assertTrue(partitions.contains(chrome), "Should contain Chrome partition");
    assertTrue(partitions.contains(firefox), "Should contain Firefox partition");
    assertTrue(partitions.contains(safari), "Should contain Safari partition");
  }

  @Test
  public void testConstructorSetsParentParameter() {
    for (EquivalencePartition partition : parameter.getPartitions()) {
      assertEquals(
          partition.getParentParameter(),
          parameter,
          "Each partition should reference the parameter as parent");
    }
  }

  @Test
  public void testGetPartitionByName() {
    assertEquals(
        parameter.getPartitionByName("Chrome"),
        chrome,
        "Should find partition by exact name match");
    assertEquals(
        parameter.getPartitionByName("Firefox"),
        firefox,
        "Should find partition by exact name match");
    assertNull(
        parameter.getPartitionByName("NonExistent"),
        "Should return null for non-existent partition name");
  }

  @Test
  public void testGetPartitionByIndex() {
    List<EquivalencePartition> partitions = parameter.getPartitions();
    for (int i = 0; i < partitions.size(); i++) {
      assertEquals(
          parameter.getPartitionByIndex(i),
          partitions.get(i),
          "Should return correct partition at index " + i);
    }
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testGetPartitionByIndexThrowsException() {
    parameter.getPartitionByIndex(3); // Should throw exception for invalid index
  }

  @Test
  public void testAreCompatibleWithNoRules() {
    assertTrue(
        parameter.areCompatible(chrome, firefox),
        "Partitions should be compatible when no rules are defined");
    assertTrue(
        parameter.areCompatible(firefox, safari),
        "Partitions should be compatible when no rules are defined");
  }

  @Test
  public void testAreCompatibleWithRules() {
    // Create a parameter with a rule that Safari is only compatible with MacOS
    SimpleValue macOS = SimpleValue.of("MacOS");
    SimpleValue windows = SimpleValue.of("Windows");

    List<CompatibilityPredicate> rules =
        Arrays.asList(
            (v1, v2) -> {
              // Safari only works with MacOS
              if (v1.getValue().equals("Safari")) {
                return v2.getValue().equals("MacOS");
              }
              return true;
            });

    TestParameter browserWithRules =
        new TestParameter("browser", Arrays.asList(chrome, firefox, safari), rules);

    assertTrue(
        browserWithRules.areCompatible(chrome, windows),
        "Chrome should be compatible with Windows");
    assertFalse(
        browserWithRules.areCompatible(safari, windows),
        "Safari should not be compatible with Windows");
    assertTrue(
        browserWithRules.areCompatible(safari, macOS), "Safari should be compatible with MacOS");
  }

  @Test
  public void testPartitionsAreImmutable() {
    List<EquivalencePartition> partitions = parameter.getPartitions();
    try {
      partitions.add(SimpleValue.of("Edge"));
      fail("Should not be able to modify the partitions list");
    } catch (UnsupportedOperationException e) {
      // Expected
    }
  }
}
