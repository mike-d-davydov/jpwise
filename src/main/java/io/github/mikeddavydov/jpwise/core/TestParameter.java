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
package io.github.mikeddavydov.jpwise.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
 * // Simple parameter without compatibility rules using varargs
 * TestParameter browser = TestParameter.of("browser",
 *     SimpleValue.of("Chrome"),
 *     SimpleValue.of("Firefox"));
 *
 * // Simple parameter without compatibility rules using collection
 * TestParameter browser = TestParameter.of("browser",
 *     Arrays.asList(SimpleValue.of("Chrome"), SimpleValue.of("Firefox")));
 *
 * // Parameter with compatibility rules using builder style
 * List&lt;CompatibilityPredicate&gt; rules = Arrays.asList((ep1, ep2) -&gt; {
 *   // Safari only works with macOS
 *   if (ep1.getName().equals("Safari")
 *       &amp;&amp; ep2.getParentParameter().getName().equals("operatingSystem")) {
 *     return ep2.getName().equals("macOS");
 *   }
 *   return true;
 * });
 * TestParameter browser = TestParameter.of("browser", partitions, rules);
 * </pre>
 *
 * @author panwei, davydovmd
 * @see EquivalencePartition
 * @see CompatibilityPredicate
 */
public class TestParameter {
  private String name;
  private List<EquivalencePartition> partitions;
  private List<CompatibilityPredicate> dependencies;

  /**
   * Creates a new test parameter with the specified name and equivalence partitions.
   *
   * @param name The name of the parameter (used for reporting and identification)
   * @param partitions Variable number of equivalence partitions for this parameter
   * @return A new TestParameter instance
   */
  public static TestParameter of(String name, EquivalencePartition... partitions) {
    return new TestParameter(name, Arrays.asList(partitions));
  }

  /**
   * Creates a new test parameter with the specified name and equivalence partitions.
   *
   * @param name The name of the parameter (used for reporting and identification)
   * @param partitions Collection of equivalence partitions for this parameter
   * @return A new TestParameter instance
   */
  public static TestParameter of(String name, Collection<EquivalencePartition> partitions) {
    return new TestParameter(name, partitions);
  }

  /**
   * Creates a new test parameter with name, equivalence partitions, and compatibility rules.
   *
   * @param name The name of the parameter (used for reporting and identification)
   * @param partitions Collection of equivalence partitions for this parameter
   * @param dependencies List of compatibility rules for this parameter's partitions
   * @return A new TestParameter instance
   */
  public static TestParameter of(
      String name,
      Collection<EquivalencePartition> partitions,
      List<CompatibilityPredicate> dependencies) {
    return new TestParameter(name, partitions, dependencies);
  }

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
    this.partitions = new ArrayList<>(partitions);
    this.dependencies = new ArrayList<>();
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
    this.partitions = new ArrayList<>(partitions);
    this.dependencies = new ArrayList<>(dependencies);
    for (EquivalencePartition partition : partitions) {
      partition.setParentParameter(this);
    }
  }

  /**
   * Creates a copy of an existing test parameter. This creates a deep copy of the parameter,
   * including its partitions and dependencies.
   *
   * @param other The parameter to copy
   */
  public TestParameter(TestParameter other) {
    this.name = other.name;
    this.partitions = new ArrayList<>(other.partitions);
    this.dependencies = new ArrayList<>(other.dependencies);
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
      if (partition.getName().equals(name)) {
        return partition;
      }
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
    return Collections.unmodifiableList(partitions);
  }

  /**
   * Gets all compatibility rules for this parameter.
   *
   * @return An immutable collection of compatibility rules
   */
  public List<CompatibilityPredicate> getDependencies() {
    return Collections.unmodifiableList(dependencies);
  }

  /**
   * Checks if two equivalence partitions are compatible according to this parameter's rules. This
   * method applies all compatibility predicates to determine if the partitions can be used together
   * in a test combination.
   *
   * @param v1 The first equivalence partition to check
   * @param v2 The second equivalence partition to check
   * @return true if the partitions are compatible, false otherwise
   */
  public boolean areCompatible(EquivalencePartition v1, EquivalencePartition v2) {
    if (v1 == null || v2 == null) {
      return false;
    }
    for (CompatibilityPredicate rule : dependencies) {
      if (!rule.test(v1, v2)) {
        return false;
      }
    }
    return true;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TestParameter)) {
      return false;
    }

    TestParameter that = (TestParameter) o;
    if (!name.equals(that.name)) {
      return false;
    }
    return partitions.equals(that.partitions);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + partitions.hashCode();
    return result;
  }
}
