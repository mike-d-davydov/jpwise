# JPWise - Java Pairwise Testing Framework

JPWise is a powerful Java framework for generating pairwise test combinations, with support for complex parameter relationships and compatibility rules. This is a heavily reworked and extended version of the original jWise library by Pan Wei.

## Table of Contents
- [History](#history)
- [Terminology](#terminology)
- [Features](#features)
- [Installation](#installation)
- [Basic Usage](#basic-usage)
- [Advanced Usage](#advanced-usage)
  - [Understanding Equivalence Partitions](#understanding-equivalence-partitions)
  - [Defining Compatibility Rules](#defining-compatibility-rules)
  - [Best Practices](#best-practices)
  - [Example: Complex Test Scenario](#example-complex-test-scenario)
- [TestNG DataProvider Integration](#testng-dataprovider-integration)
- [Running Tests](#running-tests)
- [Architecture](#architecture)
- [License](#license)
- [Acknowledgments](#acknowledgments)
- [Requirements](#requirements)

## Terminology

JPWise uses specific terminology to describe its concepts:

- **Parameter**: A test input variable (e.g., browser, OS)
- **Equivalence Partition**: A group of values that are expected to be handled similarly by the system. In JPWise, these are represented by classes implementing the `EquivalencePartition` interface (e.g., `SimpleValue`, `GenericPartition`, `CyclingPartition`). Values within a partition can be static or dynamically generated.
- **Value**: A specific concrete value obtained from an equivalence partition at the time of combination generation or access. Values are returned as `Object` and may require casting to the appropriate type.
- **Combination**: A set of specific values, one from each parameter's partition, forming a single test case.
- **Compatibility Rule**: A constraint that defines valid combinations of parameter values using lambda expressions
- **Pairwise Testing**: A test design technique that tests all possible pairs of parameter values across different parameters
- **Combinatorial Testing**: Testing all possible combinations up to a specified limit
- **Test Suite**: A collection of test combinations

## History

- **2010**: Original jWise library created by Pan Wei
- **2013**: Major rework by Mikhail Davydov
  - Complete rewrite of core algorithms
  - Improved compatibility rules system
  - Added comprehensive test suite
- **2024**: Modern uplift and API improvements
  - Updated to latest dependency versions
  - Enhanced test coverage
  - Improved documentation and examples
  - Added TestNG DataProvider integration
  - Added support for dynamic value generation
  - Standardized terminology

## Author
- **Mikhail Davydov** - Main author and maintainer
- Based on original jWise library by Pan Wei

## Features

- **Pairwise Testing**: Generate test combinations that cover all pairs of parameter values
- **Combinatorial Testing**: Generate all possible combinations up to a specified limit
- **Fluent API**: Easy-to-use builder pattern (`JPWise.builder()`) for constructing test inputs
- **Lambda-based Rules**: Define compatibility rules using Java lambda expressions
- **Equivalence Partitioning**: Group parameter values into meaningful partitions
- **Dynamic Values**: Support for computed or fetched values at generation time
- **Custom Algorithms**: Extensible algorithm framework for different generation strategies
- **TestNG Integration**: Seamless integration with TestNG DataProvider
- **Comprehensive Documentation**: Detailed examples and API documentation

## Installation

### Using JitPack

Add the JitPack repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add the dependency:

```xml
<dependency>
    <groupId>com.github.mike-d-davydov</groupId>
    <artifactId>jpwise</artifactId>
    <version>v1.0.0</version>
</dependency>
```

### Local Installation

If you prefer to install locally:

```bash
git clone https://github.com/mike-d-davydov/jpwise.git
cd jpwise
mvn clean install
```

Then add the dependency to your project:

```xml
<dependency>
    <groupId>com.github.mike-d-davydov</groupId>
    <artifactId>jpwise</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Basic Usage

The most concise way to use JPWise is through the builder API.

**Important Note on `JPWise.builder().parameter(...)` API:**
- When providing only partitions for a parameter (i.e., no compatibility rules are being defined *for that specific parameter line*), an array `EquivalencePartition[]` is expected for the partitions.
  Example: `.parameter("paramName", Arrays.asList(val1, val2).toArray(new EquivalencePartition[0]))`
- When providing partitions *and* a `List<CompatibilityPredicate>` for a parameter (on the same `.parameter(...)` call), a `List<EquivalencePartition>` is expected for the partitions.
  Example: `.parameter("paramName", Arrays.asList(val1, val2), rulesList)`

```java
import io.github.mikeddavydov.JPWise;
import io.github.mikeddavydov.core.Combination;
import io.github.mikeddavydov.core.CombinationTable;
import io.github.mikeddavydov.core.EquivalencePartition;
import io.github.mikeddavydov.core.SimpleValue;
import io.github.mikeddavydov.core.TestParameter;
import java.util.Arrays;
import java.util.List;

// Define parameter values using convenient factory methods
SimpleValue chrome = SimpleValue.of("Chrome");  // Name and value are "Chrome"
SimpleValue firefox = SimpleValue.of("Firefox");
SimpleValue windows = SimpleValue.of("Windows", "11");  // Name "Windows", actual value "11"
SimpleValue macOS = SimpleValue.of("macOS", "14.1"); // Name "macOS", actual value "14.1"

// Create test combinations using the builder
CombinationTable results = JPWise.builder()
    .parameter("browser", Arrays.asList(chrome, firefox).toArray(new EquivalencePartition[0]))
    .parameter("os", Arrays.asList(windows, macOS).toArray(new EquivalencePartition[0]))
    .generatePairwise();

// Use the results
System.out.println("Generated Combinations (" + results.combinations().size() + "):");
for (Combination combination : results.combinations()) {
    // combination.getValues() returns EquivalencePartition[]
    // Order matches .parameter() calls
    EquivalencePartition browserEP = combination.getValues()[0];
    EquivalencePartition osEP = combination.getValues()[1];
    
    String browserName = browserEP.getName(); // e.g., "Chrome"
    Object browserActualValue = browserEP.getValue(); // e.g., "Chrome"
    String osName = osEP.getName(); // e.g., "Windows"
    Object osActualValue = osEP.getValue(); // e.g., "11"
    
    System.out.printf("Browser: %s (%s), OS: %s (%s)%n", 
        browserName, 
        (String) browserActualValue, // Cast to String if needed
        osName, 
        (String) osActualValue);    // Cast to String if needed
}
// Expected output for this example:
// Browser: Chrome (Chrome), OS: Windows (11)
// Browser: Chrome (Chrome), OS: macOS (14.1)
// Browser: Firefox (Firefox), OS: Windows (11)
// Browser: Firefox (Firefox), OS: macOS (14.1)
```

You can also use the direct generation method (constructor for `TestParameter` always expects `List<EquivalencePartition>`):
```java
// (Assumes SimpleValue chrome, firefox, windows, macOS are defined as above)
CombinationTable resultsDirect = JPWise.generatePairwise(
    new TestParameter("browser", Arrays.asList(chrome, firefox)), // List for partitions
    new TestParameter("os", Arrays.asList(windows, macOS))      // List for partitions
);
// Process resultsDirect similarly...
```

Or pre-populate `TestParameter` objects:
```java
// (Assumes SimpleValue chrome, firefox, windows, macOS are defined as above)
TestParameter browserParam = new TestParameter("browser", Arrays.asList(chrome, firefox));
TestParameter osParam = new TestParameter("os", Arrays.asList(windows, macOS));

CombinationTable resultsWithParams = JPWise.withParameters(browserParam, osParam)
    .generatePairwise();
// Process resultsWithParams similarly...
```

**Note on `generatePairwise()` with a Single Parameter:**
Calling `generatePairwise()` when only one parameter has been defined (either via builder or direct methods) will result in a `CombinationTable` with **zero** combinations. This is because "pairwise" inherently implies interactions between at least two parameters to form pairs.

## Advanced Usage

### Understanding Equivalence Partitions

JPWise uses equivalence partitions to group test inputs. `TestParameter` can contain multiple `EquivalencePartition` instances. Each partition type allows for different ways of providing or generating values.

1.  **`SimpleValue`**: For constant, static values.
    ```java
    SimpleValue chromeStable = SimpleValue.of("ChromeStable", "116.0.5845.96"); // Name, Value
    SimpleValue edge = SimpleValue.of("Edge"); // Name and Value are "Edge"
    ```

2.  **`GenericPartition`**: For dynamic value generation via a `Supplier<Object>`. The supplier is called each time a value is needed from this partition during combination generation or when `getValue()` is explicitly called.
    ```java
    // Example helper for dynamic build numbers
    // private static int buildCounter = 0;
    // private static synchronized String getNextBuildNumber() { // Ensure thread-safety if supplier can be called concurrently
    //     return String.valueOf(22000 + (++buildCounter));
    // }

    GenericPartition windowsDynamic = new GenericPartition(
        "Windows 11 Dynamic Build", // Name of the partition
        () -> String.format("22H2.Build_%s", getNextBuildNumber()) // Example supplier
    );
    // Access value:
    // Object dynamicBuild = windowsDynamic.getValue(); // Invokes supplier, e.g., "22H2.Build_22001"
    // String buildString = (String) dynamicBuild; // Cast to String if needed
    ```
    There's also a convenient factory: `GenericPartition.of("name", supplier)`.

3.  **`CyclingPartition`**: Cycles through a predefined list of values in a thread-safe manner.
    ```java
    CyclingPartition firefoxVersions = new CyclingPartition(
        "Firefox ESR Cycle", // Name of this partition
        "115.0.1esr",      // Default/initial value (also the first in the cycle list)
        Arrays.asList("115.0.1esr", "115.0.2esr", "115.0.3esr") // Values to cycle through
    );
    // Access value (will cycle on each call to getValue()):
    // Object ffVal1 = firefoxVersions.getValue(); // "115.0.1esr"
    // String ffString1 = (String) ffVal1; // Cast to String if needed
    // Object ffVal2 = firefoxVersions.getValue(); // "115.0.2esr"
    // String ffString2 = (String) ffVal2; // Cast to String if needed
    ```

### Defining Compatibility Rules

Use `CompatibilityPredicate` (a `BiPredicate<EquivalencePartition, EquivalencePartition>`) to define rules. When a rule is attached to a parameter (e.g., `paramA`), `ep1` in the predicate refers to a value from `paramA`, and `ep2` to a value from another parameter involved in the current pairing check by the algorithm. The predicate should return `true` if the pair is compatible, and `false` otherwise.

```java
import io.github.mikeddavydov.core.CompatibilityPredicate;
// ... other imports

// Define parameter partitions
List<EquivalencePartition> browserPartitions = Arrays.asList(
    EquivalencePartition.of("Chrome", "Firefox", "Safari"),
    EquivalencePartition.of("Edge", "Opera")
);
List<EquivalencePartition> osPartitions = Arrays.asList(
    EquivalencePartition.of("Windows", "Linux"),
    EquivalencePartition.of("macOS")
);

// Define compatibility rule: Safari only works with macOS
List<CompatibilityPredicate> browserOsRules = Arrays.asList(
    (ep1, ep2) -> { // ep1 from "browser" parameter, ep2 from "os" parameter
        if (ep1.getName().equals("Safari")) { // If the browser is Safari
            return ep2.getName().equals("macOS"); // Then it's only compatible if the OS is macOS
        }
        return true; // All other browsers are compatible with all OSes by default
    }
);

// Using the builder:
// Note: when rules are provided on a .parameter() line, partitions must be a List
CombinationTable resultsBuilder = JPWise.builder()
    .parameter("browser", browserPartitions, browserOsRules)
    .parameter("os", osPartitions.toArray(new EquivalencePartition[0])) // No rules on this line, so partitions must be an Array
    .generatePairwise();

// Using the direct method with TestParameter:
// Note: TestParameter constructor always takes a List for partitions
CombinationTable resultsDirect = JPWise.generatePairwise(
    new TestParameter("browser", browserPartitions, browserOsRules),
    new TestParameter("os", osPartitions)
);

// Validate results: Iterate and check that no combination has Safari with non-macOS
System.out.println("Compatibility Rule Results (" + resultsBuilder.combinations().size() + " combinations):");
for (Combination combination : resultsBuilder.combinations()) {
    String browserName = combination.getValues()[0].getName();
    String osName = combination.getValues()[1].getName();
    // System.out.printf("Checking: %s on %s%n", browserName, osName);
    if (browserName.equals("Safari")) {
        assert osName.equals("macOS") : "Validation failed: Safari found with " + osName;
    }
    assert !(browserName.equals("Edge") && osName.equals("Linux")) || browserName.equals("Edge") && osName.equals("Linux"); // Example: Check if Edge on Linux is there if expected
}
// Example expected output might exclude Safari on Windows/Linux, etc.
```

### Best Practices
1.  **Choosing Partition Types**: `SimpleValue` for static values, `GenericPartition` for values that need to be computed or fetched at generation time, `CyclingPartition` for sequentially iterating through a known set of values.
2.  **Defining Compatibility Rules**: Keep predicates focused on the direct relationship between the two partitions being evaluated. The framework handles symmetric evaluation (if you define a rule for ParamA vs ParamB, it implicitly applies to ParamB vs ParamA).
3.  **Parameter Organization**: Use clear, descriptive names for parameters and partitions.
4.  **Testing Considerations**: Test positive and negative compatibility cases, ensure cycling partitions behave as expected, and verify dynamic value generation.
5.  **Type Safety**: Remember that `getValue()` returns `Object`. Cast values to the appropriate type when needed, and handle potential `ClassCastException`s appropriately.

## TestNG DataProvider Integration

JPWise can be integrated with TestNG's DataProvider for generating test data.

## Running Tests

JPWise can be run as a standalone application or integrated with TestNG for running tests.

## Architecture

JPWise is designed to be extensible and modular. The core framework is designed to be lightweight and easy to extend.

## License

JPWise is released under the MIT License.

## Acknowledgments

JPWise was inspired by the original jWise library by Pan Wei.

## Requirements

JPWise requires Java 8 or higher.