# JPWise - Java Pairwise Testing Framework

JPWise is a powerful Java framework for generating pairwise test combinations, with support for complex parameter relationships and compatibility rules. See [All-pairs testing](https://en.wikipedia.org/wiki/All-pairs_testing) for more information about this testing approach.

## Table of Contents
- [Why JPWise?](#why-jpwise-smart--simple-test-data-generation)
- [Features](#features)
- [Installation](#installation)
- [Basic Usage](#basic-usage)
- [Advanced Usage](#advanced-usage)
- [TestNG Integration](#testng-dataprovider-integration)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [License](#license)
- [Terminology](#terminology)
- [History](#history)

## Why JPWise? Smart & Simple Test Data Generation

Manually creating comprehensive test data for features with many interacting options and complex business rules is often complex and error-prone. JPWise automates this by generating pairwise or combinatorial test sets that respect your defined compatibility rules and can use dynamic data values for more thorough testing with less effort.

**Here's JPWise in action, demonstrating key features including TestNG integration:**

```java
import io.github.mikeddavydov.jpwise.JPWise;
import io.github.mikeddavydov.jpwise.core.CombinationTable;
import io.github.mikeddavydov.jpwise.core.CompatibilityPredicate;
import io.github.mikeddavydov.jpwise.core.EquivalencePartition;
import io.github.mikeddavydov.jpwise.core.SimpleValue;
import io.github.mikeddavydov.jpwise.core.CyclingPartition;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.List;

public class JPWiseQuickDemoTest {
    private static final CombinationTable DEMO_COMBINATIONS = generateJPWiseData();

    private static CombinationTable generateJPWiseData() {
        // Define a rule: "Safari" browser is only compatible with "macOS"
        List<CompatibilityPredicate> browserRules = Arrays.asList(
            (ep1, ep2) -> {
                if (ep1.getName().equals("Safari") && !ep2.getName().equals("macOS")) {
                    return false; // Safari is incompatible with non-macOS
                }
                return true; // Otherwise compatible
            }
        );

        return JPWise.builder()
            .parameter("Browser", 
                CyclingPartition.of("Chrome", Arrays.asList("latest", "previous")), 
                SimpleValue.of("Safari"))
            .parameter("OS",
                SimpleValue.of("macOS"), 
                SimpleValue.of("Windows"))
            .generatePairwise();
    }

    @DataProvider(name = "jpwiseTestData")
    public Object[][] getTestDataFromJPWise() {
        return DEMO_COMBINATIONS.asDataProvider();
    }

    @Test(dataProvider = "jpwiseTestData")
    public void testFeatureWithVariedConfigs(String description, String browser, String os) {
        // description provides a summary (e.g., "Browser=Chrome(latest), OS=Windows")
        System.out.printf("Testing: %s%n", description);
        // Your test implementation here
    }
}
```

## Features

- **Pairwise Testing**: Generate test combinations that cover all pairs of parameter values
- **Combinatorial Testing**: Generate all possible combinations up to a specified limit
- **Fluent API**: Easy-to-use builder pattern (`JPWise.builder()`) for constructing test inputs
- **Lambda-based Rules**: Define compatibility rules using Java lambda expressions
- **Equivalence Partitioning**: Group parameter values into meaningful partitions
- **Dynamic Values**: Support for computed or fetched values at generation time
- **TestNG Integration**: Seamless integration with TestNG DataProvider

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

The most concise way to use JPWise is through the builder API:

```java
import io.github.mikeddavydov.jpwise.JPWise;
import io.github.mikeddavydov.jpwise.core.SimpleValue;

// Generate combinations
CombinationTable results = JPWise.builder()
    .parameter("browser", 
        SimpleValue.of("Chrome"),
        SimpleValue.of("Firefox"))
    .parameter("os", 
        SimpleValue.of("Windows", "11"),
        SimpleValue.of("macOS", "14.1"))
    .generatePairwise();

// Use the results
for (Combination combination : results.combinations()) {
    EquivalencePartition browserEP = combination.getValues()[0];
    EquivalencePartition osEP = combination.getValues()[1];
    System.out.printf("Browser: %s, OS: %s%n", 
        browserEP.getValue(), 
        osEP.getValue());
}
```

## Advanced Usage

### Equivalence Partitions

JPWise supports three types of equivalence partitions:

1. **`SimpleValue`**: For constant values
    ```java
    SimpleValue chrome = SimpleValue.of("Chrome", "116.0"); // Name, Value
    ```

2. **`GenericPartition`**: For dynamic value generation
    ```java
    GenericPartition dynamicBuild = GenericPartition.of(
        "Windows 11 Dynamic Build",
        () -> String.format("22H2.Build_%s", getNextBuildNumber())
    );
    ```

3. **`CyclingPartition`**: For cycling through values
    ```java
    CyclingPartition versions = CyclingPartition.of("Firefox ESR", "115.0.1esr", "115.0.2esr", "115.0.3esr");
    ```

### Compatibility Rules

Compatibility rules define which combinations of parameter values are valid. They are implemented as predicates that take two parameter values and return `true` if they are compatible. By default, all combinations are considered compatible unless explicitly marked as incompatible:

```java
List<CompatibilityPredicate> rules = Arrays.asList(
    (ep1, ep2) -> {
        // Return false only for incompatible combinations
        if (ep1.getName().equals("Safari") && !ep2.getName().equals("macOS")) {
            return false;  // Safari is incompatible with non-macOS systems
        }
        return true;  // All other combinations are compatible
    }
);

TestParameter browser = TestParameter.of("browser", browserPartitions, rules);
```

Rules are evaluated during combination generation to ensure only valid combinations are included in the test set.

## TestNG DataProvider Integration

JPWise integrates seamlessly with TestNG's DataProvider feature. Here's a concise example:

```java
@DataProvider(name = "browserConfigurations")
public Object[][] getBrowserConfigurations() {
    TestInput input = TestInput.of();
    input.add(TestParameter.of("browser", browserPartitions, compatibilityRules));
    input.add(TestParameter.of("os", osPartitions));
    return JPWise.generatePairwise(input).asDataProvider();
}

@Test(dataProvider = "browserConfigurations")
public void testBrowserCompatibility(String description, String browser, String os) {
    // Test implementation using the generated combinations
}
```

For a complete example with parameter definitions, compatibility rules, and detailed test implementation, see `JpWiseDataProviderDemoTest.java` in the test sources.

## Architecture

JPWise is designed to be extensible and modular. The core framework is lightweight and easy to extend:

- **Custom Partitions**: Create your own `EquivalencePartition` implementations by extending either `GenericPartition` or `BaseEquivalencePartition`
- **Custom Algorithms**: Implement your own test generation algorithms (e.g., N-wise testing) by extending the existing algorithm classes
- **Extensible Core**: The framework's core components are designed to be extended while maintaining compatibility with existing features

## Requirements

- Java 11 or higher
- Maven 3.6 or higher (for building from source)

## License

JPWise is released under the MIT License.

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



