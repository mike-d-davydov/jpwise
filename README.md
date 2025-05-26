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

- **Parameter**: A variable or aspect of the system under test (e.g., browser, operating system)
- **Equivalence Partition**: A group of values that are expected to be handled similarly by the system
- **Value**: A specific concrete value within an equivalence partition
- **Combination**: A set of specific values, one from each parameter's partition
- **Compatibility Rule**: A constraint defining which values from different parameters can be combined
- **Pairwise Testing**: A test design technique that tests all possible pairs of parameter values


## History

- **2010**: Original jWise library created by Pan Wei
- **2013**: Major rework by Mikhail Davydov
  - Complete rewrite of core algorithms
  - Added support for generic types
  - Improved compatibility rules system
  - Added comprehensive test suite
- **2025**: Modern uplift and API improvements
  - Updated to latest dependency versions
  - Enhanced test coverage
  - Improved documentation and examples
  - Added TestNG DataProvider integration
  - Added support for dynamic value generation for partitions
  - Standardized terminology

## Author
- **Mikhail Davydov** - Main author and maintainer
- Based on original jWise library by Pan Wei

## Features
- Pairwise (2-wise) and combinatorial test case generation
- Support for complex parameter relationships
- Compatibility rules between parameters
- TestNG DataProvider integration
- Dynamic value generation for partitions with GenericPartition
- Thread-safe value cycling with CyclingPartition

## Installation

Add to your pom.xml:
```xml
<dependency>
    <groupId>com.functest</groupId>
    <artifactId>jpwise</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Basic Usage

The most concise way to use JPWise is through the builder API:

```java
// Define parameter values using convenient factory methods
SimpleValue chrome = SimpleValue.of("Chrome");  // Value same as name
SimpleValue firefox = SimpleValue.of("Firefox");
SimpleValue windows = SimpleValue.of("Windows", "11");  // Explicit value
SimpleValue macOS = SimpleValue.of("macOS", "14.1");

// Create test combinations using the builder
CombinationTable results = JPWise.builder()
    .parameter("browser", Arrays.<EquivalencePartition>asList(chrome, firefox))
    .parameter("os", Arrays.<EquivalencePartition>asList(windows, macOS))
    .generatePairwise();

// Use the results
for (Combination combination : results.combinations()) {
    Object browserValue = combination.getValue(0).getValue();
    Object osValue = combination.getValue(1).getValue();
    System.out.println(browserValue + " on " + osValue);
}
```

You can also use the direct generation method for even more concise code:

```java
CombinationTable results = JPWise.generatePairwise(
    new TestParameter("browser", Arrays.<EquivalencePartition>asList(chrome, firefox)),
    new TestParameter("os", Arrays.<EquivalencePartition>asList(windows, macOS))
);
```

Or pre-populate parameters and use them later:

```java
TestParameter browser = new TestParameter("browser", Arrays.<EquivalencePartition>asList(chrome, firefox));
TestParameter os = new TestParameter("os", Arrays.<EquivalencePartition>asList(windows, macOS));

CombinationTable results = JPWise.withParameters(browser, os)
    .generatePairwise();
```

## Advanced Usage

### Understanding Equivalence Partitions

JPWise uses equivalence partitions to group test inputs that are expected to behave similarly. A key concept is that a TestParameter can contain multiple equivalence partitions, each representing a different class of values for that parameter.

For example, a "Browser" parameter might have separate partitions for:
- Latest Chrome versions (as a cycling partition)
- Stable Firefox versions (as another cycling partition)
- Legacy IE versions (as simple values)
- Beta versions (as dynamic values)

The framework provides several implementations:

1. **SimpleValue**
   - Most basic implementation for constant values
   - Suitable for static, unchanging test inputs
   ```java
   SimpleValue chrome = SimpleValue.of("Chrome", "116.0");
   ```

2. **GenericPartition**
   - Supports dynamic value generation via a Supplier
   - Useful for values that need to be computed or fetched at runtime
   ```java
   // Dynamic version generation
   GenericPartition windows = new GenericPartition(
       "Windows 11", 
       () -> String.format("22H2.%d", getCurrentBuild())
   );
   ```

3. **CyclingPartition**
   - Cycles through a set of values
   - Thread-safe using AtomicInteger for cycling
   - Useful for testing multiple values in sequence
   ```java
   CyclingPartition firefox = new CyclingPartition(
       "Firefox",
       "118.0.2",  // Default value
       Arrays.asList("118.0.2", "118.0.3", "118.1.0")  // Values to cycle through
   );
   ```

### Defining Compatibility Rules

JPWise provides direct method calls for defining compatibility rules between parameter partitions. When you define a rule on a parameter, the framework ensures that `ep1` represents a value from that parameter and `ep2` represents a value from the other parameter being tested. This eliminates the need for manual parameter identification and swapping logic in your rules.

```java
// Define the parameter partitions first
List<EquivalencePartition> browserPartitions = Arrays.asList(
    SimpleValue.of("Chrome"),
    SimpleValue.of("Firefox"),
    SimpleValue.of("Safari"),
    SimpleValue.of("Edge")
);

List<EquivalencePartition> osPartitions = Arrays.asList(
    SimpleValue.of("Windows 11"),
    SimpleValue.of("Windows 10"),
    SimpleValue.of("macOS"),
    SimpleValue.of("Ubuntu")
);

// Define compatibility rules using simplified lambdas
List<CompatibilityPredicate> browserOsRules = Arrays.asList(
    // Safari only works with macOS
    // Framework ensures ep1 is browser, ep2 is OS for this rule context
    (ep1, ep2) -> {
        if (ep1.getName().equals("Safari")) {
            return ep2.getName().equals("macOS");
        }
        return true;
    }
);

// Create test combinations using the builder
CombinationTable results = JPWise.builder()
    .parameter("browser", browserPartitions, browserOsRules)
    .parameter("os", osPartitions)
    .generatePairwise();

// Or use the direct method
results = JPWise.generatePairwise(
    new TestParameter("browser", browserPartitions, browserOsRules),
    new TestParameter("os", osPartitions)
);
```

### Best Practices

1. **Choosing Partition Types**
   - Use `SimpleValue` for constant values that don't change
   - Use `GenericPartition` for dynamic value generation
   - Use `CyclingPartition` when you need to test multiple values

2. **Defining Compatibility Rules**
   - Write focused CompatibilityPredicates assuming a primary parameter context, as the framework handles symmetric evaluation of rules
   - Use direct method calls on EquivalencePartition for readable and maintainable rules
   - Use standard Java logical operators (`&&`, `||`, `!`) for combining conditions
   - Keep rules focused and well-documented

3. **Parameter Organization**
   - Group related parameters together
   - Use clear, descriptive names for parameters and their partitions
   - Document any special relationships or constraints

4. **Testing Considerations**
   - Test both positive and negative compatibility cases
   - Verify cycling behavior when using `CyclingPartition`
   - Check edge cases in dynamic value generation

### Example: Complex Test Scenario

```java
// Browser versions with cycling values
CyclingPartition chrome = new CyclingPartition("Chrome", "116.0.5845.96", 
    Arrays.asList("116.0.5845.96", "116.0.5845.97", "116.0.5845.98"));
CyclingPartition firefox = new CyclingPartition("Firefox", "118.0.2",
    Arrays.asList("118.0.2", "118.0.3", "118.1.0"));
CyclingPartition safari = new CyclingPartition("Safari", "17.0",
    Arrays.asList("17.0", "17.0.1", "17.1"));

// OS with dynamic versions using factory method
GenericPartition windows11 = GenericPartition.of("Windows 11", 
    () -> "22H2 " + getLatestBuild("win11"));
GenericPartition windows10 = GenericPartition.of("Windows 10", 
    () -> "22H2 " + getLatestBuild("win10"));
GenericPartition macOS = GenericPartition.of("macOS", 
    () -> getLatestVersion("macos"));

// Screen resolutions using factory method
SimpleValue hd = SimpleValue.of("HD", "1920x1080");
SimpleValue uhd = SimpleValue.of("4K", "3840x2160");

// Define compatibility rules using simplified lambdas
List<CompatibilityPredicate> browserOsRules = Arrays.asList(
    // Safari only works with macOS
    // Framework ensures ep1 is browser, ep2 is OS for this rule context
    (ep1, ep2) -> {
        if (ep1.getName().equals("Safari")) {
            return ep2.getName().equals("macOS");
        }
        return true;
    }
);

List<CompatibilityPredicate> resolutionOsRules = Arrays.asList(
    // 4K not supported on older Windows 10 builds
    // Framework ensures ep1 is resolution, ep2 is OS for this rule context
    (ep1, ep2) -> {
        if (ep1.getName().equals("4K") && ep2.getName().equals("Windows 10")) {
            // Check if this is an older Windows 10 build that doesn't support 4K
            String osValue = (String) ep2.getValue();
            if (osValue != null && osValue.contains("22H2") && 
                osValue.contains("19045")) { // Older build number
                return false;
            }
        }
        return true;
    }
);

// Generate test combinations using the builder
CombinationTable results = JPWise.builder()
    .parameter("browser", Arrays.<EquivalencePartition>asList(chrome, firefox, safari), browserOsRules)
    .parameter("operatingSystem", Arrays.<EquivalencePartition>asList(windows11, windows10, macOS))
    .parameter("resolution", Arrays.<EquivalencePartition>asList(hd, uhd), resolutionOsRules)
    .generatePairwise();

// Verify the rules are enforced using direct method calls
for (Combination combination : results.combinations()) {
    EquivalencePartition browserValue = combination.getValue(0);
    EquivalencePartition osValue = combination.getValue(1);
    EquivalencePartition resolutionValue = combination.getValue(2);
    
    // Safari should only appear with macOS
    if (browserValue.getName().equals("Safari")) {
        assert osValue.getName().equals("macOS") : 
            "Safari must be paired with macOS";
    }
    
    // 4K resolution should not appear with older Windows 10 builds
    if (resolutionValue.getName().equals("4K") && osValue.getName().equals("Windows 10")) {
        String osValueStr = (String) osValue.getValue();
        assert !(osValueStr != null && osValueStr.contains("22H2") && osValueStr.contains("19045")) : 
            "4K not supported on older Windows 10 builds";
    }
}
```

## TestNG DataProvider Integration

```java
@DataProvider(name = "browserConfigs")
public Object[][] getBrowserConfigs() {
    SimpleValue chrome = SimpleValue.of("Chrome");  // Using name-only factory
    SimpleValue firefox = SimpleValue.of("Firefox");
    
    CombinationTable results = JPWise.builder()
        .parameter("browser", Arrays.<EquivalencePartition>asList(chrome, firefox))
        .generatePairwise();
    
    return results.asDataProvider();
}

@Test(dataProvider = "browserConfigs")
public void testBrowsers(String description, String browser) {
    // Test implementation
}
```

## Running Tests

```bash
mvn test                           # Run all tests
mvn test -Dtest=TestClassName      # Run specific test
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

* Pan Wei for the original jWise library that served as the foundation for this project
* All contributors who have helped improve and extend the framework

## Requirements

Java 8 or higher
Maven 3.x

## Architecture

The framework consists of several key components:

- `TestParameter`: Represents a test parameter that can contain multiple equivalence partitions. For example, a "Browser" parameter might have separate partitions for stable versions, beta versions, and legacy versions.
- `EquivalencePartition`: Interface for parameter partitions, implemented by:
  - `SimpleValue`: For constant values
  - `GenericPartition`: For dynamic value generation
  - `CyclingPartition`: For cycling through a sequence of values
- `TestGenerator`: Main class for generating test combinations. It works by:
  1. Collecting all partitions from each parameter
  2. Generating pairs of values from compatible partitions
  3. Combining pairs into complete test cases
- `GenerationAlgorithm`: Base class for test generation algorithms
- `PairwiseAlgorithm`: Implements pairwise test generation by:
  1. Creating a queue of all possible value pairs
  2. Building complete combinations that cover each pair
  3. Respecting compatibility rules during generation
- `CombinatorialAlgorithm`: Implements full combinatorial test generation
- `CombinationTable`: Holds and manages generated test combinations