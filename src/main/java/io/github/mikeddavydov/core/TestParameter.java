/**
 * Copyright (c) 2010 Ng Pan Wei, 2013 Mikhail Davydov
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mikeddavydov.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Represents a test parameter with its equivalence partitions and compatibility rules. A test
 * parameter is a variable in the test space that can take on different partitions. For example, a
 * "browser" parameter might have partitions like "Chrome", "Firefox", and "Safari".
 *
 * <p>Parameters can also have compatibility rules that define which partitions are compatible with
 * partitions of other parameters. For example, "Safari" might only be compatible with "MacOS" in an
 * operating system parameter.
 *
 * <p>Example usage:
 *
 * <pre>
 * // Simple parameter without compatibility rules
 * TestParameter browser = new TestParameter("browser",
 *     Arrays.asList(SimpleValue.of("Chrome"), SimpleValue.of("Firefox")));
 *
 * // Parameter with compatibility rules using direct method calls
 * List<CompatibilityPredicate> rules = Arrays.asList((ep1, ep2) -> {
 *   // Safari only works with macOS
 *   if (ep1.getName().equals("Safari")
 *       && ep2.getParentParameter().getName().equals("operatingSystem")) {
 *     return ep2.getName().equals("macOS");
 *   }
 *   return true;
 * });
 * TestParameter browser = new TestParameter("browser", partitions, rules);
 * </pre>
 *
 * @author panwei, davydovmd
 * @see EquivalencePartition
 * @see CompatibilityPredicate
 */
public class TestParameter {
  private String name;
  private ImmutableList<EquivalencePartition> partitions;
  private Collection<CompatibilityPredicate> dependencies = new ArrayList<>();

  /**
   * Creates a new test parameter with the specified name and equivalence partitions. This
   * constructor creates a parameter without any compatibility rules.
   *
   * @param theName The name of the parameter (used for reporting and identification)
   * @param partitions Collection of equivalence partitions for this parameter
   */
  public TestParameter(String theName, Collection<EquivalencePartition> partitions) {
    super();
    name = theName;
    this.partitions = ImmutableList.copyOf(partitions);

    for (EquivalencePartition partition : partitions) {
      partition.setParentParameter(this);
    }
  }

  /**
   * Creates a new test parameter with name, equivalence partitions, and compatibility rules. The
   * compatibility rules define which equivalence partitions of this parameter are compatible with
   * partitions of other parameters.
   *
   * @param theName The name of the parameter (used for reporting and identification)
   * @param partitions Collection of equivalence partitions for this parameter
   * @param dependencies List of compatibility rules for this parameter's partitions
   */
  public TestParameter(
      String theName,
      Collection<EquivalencePartition> partitions,
      List<CompatibilityPredicate> dependencies) {
    super();
    name = theName;
    this.dependencies = ImmutableList.copyOf(dependencies);
    this.partitions = ImmutableList.copyOf(partitions);
    for (EquivalencePartition partition : partitions) {
      partition.setParentParameter(this);
    }
  }

  /**
   * Gets a partition by its name.
   *
   * @param name The name of the partition to find
   * @return The partition with the given name, or null if not found
   */
  public EquivalencePartition getPartitionByName(String name) {
    for (EquivalencePartition partition : partitions) {
      if (partition.getName().equals(name)) return partition;
    }
    return null;
  }

  /**
   * Gets a partition by its index.
   *
   * @param i The index of the partition to get
   * @return The partition at the given index
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  public EquivalencePartition getPartitionByIndex(int i) {
    return partitions.get(i);
  }

  /**
   * Gets the name of this parameter.
   *
   * @return The parameter name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets all equivalence partitions for this parameter.
   *
   * @return An immutable list of all equivalence partitions
   */
  public List<EquivalencePartition> getPartitions() {
    return partitions;
  }

  /**
   * Gets all compatibility rules for this parameter.
   *
   * @return An immutable collection of compatibility rules
   */
  public Collection<CompatibilityPredicate> getDependencies() {
    return new ArrayList<>(dependencies);
  }

  /**
   * Checks if two equivalence partitions are compatible according to this parameter's rules. This
   * method applies all compatibility predicates to determine if the partitions can be used together
   * in a test combination.
   *
   * @param partition1 The first equivalence partition to check
   * @param partition2 The second equivalence partition to check
   * @return true if the partitions are compatible, false otherwise
   */
  public boolean areCompatible(EquivalencePartition partition1, EquivalencePartition partition2) {
    // If no dependencies, everything is compatible
    if (dependencies.isEmpty()) return true;

    // Check all dependencies - if any returns false, the partitions are
    // incompatible
    for (CompatibilityPredicate predicate : dependencies) {
      if (!predicate.areCompatible(partition1, partition2)
          || !predicate.areCompatible(partition2, partition1)) {
        return false;
      }
    }

    // All dependencies returned true (either because they don't apply or they
    // approve)
    return true;
  }
}
