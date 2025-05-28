package io.github.mikeddavydov.jpwise.core;

import static org.testng.Assert.*;

import java.util.Arrays;
import org.testng.annotations.Test;

/** Tests for {@link CyclingPartition} class. */
public class CyclingPartitionTest {

  @Test
  public void testBasicCycling() {
    CyclingPartition partition = CyclingPartition.of("Chrome", "116.0", "116.1", "116.2");

    // Should cycle through values
    assertEquals(partition.getValue(), "116.0", "First value should be first in list");
    assertEquals(partition.getValue(), "116.1", "Should cycle to next value");
    assertEquals(partition.getValue(), "116.2", "Should cycle to last value");
    assertEquals(partition.getValue(), "116.0", "Should cycle back to first value");
  }

  @Test
  public void testAutomaticReset() {
    CyclingPartition partition = CyclingPartition.of("Resolution", 1080, 1440, 2160);

    // Should cycle through all values and automatically reset
    assertEquals(partition.getValue(), Integer.valueOf(1080), "First cycle");
    assertEquals(partition.getValue(), Integer.valueOf(1440), "Second cycle");
    assertEquals(partition.getValue(), Integer.valueOf(2160), "Third cycle");
    assertEquals(
        partition.getValue(), Integer.valueOf(1080), "Should automatically reset to first value");
  }

  @Test
  public void testGetName() {
    CyclingPartition partition = CyclingPartition.of("Browser", "Chrome", "Firefox");
    assertEquals(partition.getName(), "Browser", "Should return partition name");
  }

  @Test
  public void testCompatibility() {
    CyclingPartition safari = CyclingPartition.of("Safari", "17.0", "17.1");
    CyclingPartition macOS = CyclingPartition.of("macOS", "14.1", "14.2");

    // Without parent parameter, should be compatible
    assertTrue(safari.isCompatibleWith(macOS), "Should be compatible without parent parameter");

    // With parent parameter and rules
    TestParameter browserParam =
        TestParameter.of(
            "browser",
            Arrays.asList(safari),
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
    CyclingPartition partition = CyclingPartition.of("Browser", "Chrome", "Firefox");

    // Verify cycling through all values
    assertEquals(partition.getValue(), "Chrome", "Should start with first value");
    assertEquals(partition.getValue(), "Firefox", "Should cycle to next value");
    assertEquals(partition.getValue(), "Chrome", "Should cycle back to first value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValues() {
    CyclingPartition.of("Browser", (Object[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyValues() {
    CyclingPartition.of("Browser");
  }
}
