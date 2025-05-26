package io.github.mikeddavydov.core;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

/** Tests for {@link CyclingPartition} class. */
public class CyclingPartitionTest {

  @Test
  public void testBasicCycling() {
    CyclingPartition partition =
        new CyclingPartition("Chrome", "116.0", Arrays.asList("116.0", "116.1", "116.2"));

    // Should cycle through values
    assertEquals(partition.getValue(), "116.0", "First value should be default");
    assertEquals(partition.getValue(), "116.1", "Should cycle to next value");
    assertEquals(partition.getValue(), "116.2", "Should cycle to last value");
    assertEquals(partition.getValue(), "116.0", "Should cycle back to first value");
  }

  @Test
  public void testDefaultValueNotInList() {
    String defaultValue = "117.0";
    List<Object> values = Arrays.asList("117.1", "117.2");
    CyclingPartition partition = new CyclingPartition("Firefox", defaultValue, values);

    // Default value should be first in cycling
    assertEquals(partition.getValue(), defaultValue, "Default value should be first");
    assertEquals(partition.getValue(), "117.1", "Should cycle to first list value");
    assertEquals(partition.getValue(), "117.2", "Should cycle to second list value");
  }

  @Test
  public void testAutomaticReset() {
    CyclingPartition partition =
        new CyclingPartition("Resolution", 1080, Arrays.asList(1080, 1440, 2160));

    // Should cycle through all values and automatically reset
    assertEquals(partition.getValue(), Integer.valueOf(1080), "First cycle");
    assertEquals(partition.getValue(), Integer.valueOf(1440), "Second cycle");
    assertEquals(partition.getValue(), Integer.valueOf(2160), "Third cycle");
    assertEquals(
        partition.getValue(), Integer.valueOf(1080), "Should automatically reset to first value");
  }

  @Test
  public void testGetName() {
    CyclingPartition partition =
        new CyclingPartition("Browser", "Chrome", Arrays.asList("Chrome", "Firefox"));
    assertEquals(partition.getName(), "Browser", "Should return partition name");
  }

  @Test
  public void testCompatibility() {
    CyclingPartition safari = new CyclingPartition("Safari", "17.0", Arrays.asList("17.0", "17.1"));
    CyclingPartition macOS = new CyclingPartition("macOS", "14.1", Arrays.asList("14.1", "14.2"));

    // Without parent parameter, should be compatible
    assertTrue(safari.isCompatibleWith(macOS), "Should be compatible without parent parameter");

    // With parent parameter and rules
    TestParameter browserParam =
        new TestParameter(
            "browser",
            Arrays.<EquivalencePartition>asList(safari),
            Arrays.asList(
                (v1, v2) -> {
                  // Safari is compatible with macOS
                  if (v1.getName().equals("Safari")) {
                    return v2.getName().equals("macOS");
                  }
                  // For other cases, allow compatibility
                  return true;
                }));
    safari.setParentParameter(browserParam);

    assertTrue(safari.isCompatibleWith(macOS), "Safari should be compatible with macOS");
  }

  @Test
  public void testEquivalentValuesImmutable() {
    String defaultValue = "Chrome";
    List<Object> values = Arrays.asList("Chrome", "Firefox");
    CyclingPartition partition = new CyclingPartition("Browser", defaultValue, values);

    // Verify cycling through all values
    assertEquals(partition.getValue(), defaultValue, "Should start with default value");
    assertEquals(partition.getValue(), "Firefox", "Should cycle to next value");
    assertEquals(partition.getValue(), defaultValue, "Should cycle back to default value");
  }
}
