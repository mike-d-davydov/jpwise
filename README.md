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
- [Contributing](#contributing)
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
- **Partition Condition**: A predicate that matches specific characteristics of an equivalence partition

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
  - Introduced fluent API for partition conditions
  - Standardized terminology

## Author
- **Mikhail Davydov** - Main author and maintainer
- Based on original jWise library by Pan Wei

## Features
- Pairwise (2-wise) test case generation
- Support for complex parameter relationships
- Compatibility rules between parameters
- Generic type support for parameter partitions
- Rich comparison operators with fluent API:
  - Equality: `.equals()`, `.nameIs()`, `.valueIs()`
  - Containment: `.nameIn()`, `.valueIn()`, `.valueContains()`
  - Negation: `.not()`
  - Composition: `.and()`, `.or()`
- TestNG DataProvider integration
- Dynamic value generation for partitions with GenericPartition
- Thread-safe value cycling with CyclingPartition
- Fluent API for building partition conditions

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
SimpleValue<String> chrome = SimpleValue.of("Chrome");  // Value same as name
SimpleValue<String> firefox = SimpleValue.of("Firefox");
SimpleValue<String> windows = SimpleValue.of("Windows", "11");  // Explicit value
SimpleValue<String> macOS = SimpleValue.of("macOS", "14.1");

// Create test combinations using the builder
CombinationTable results = JPWise.builder()
    .parameter("browser", Arrays.asList(chrome, firefox))
    .parameter("os", Arrays.asList(windows, macOS))
    .generatePairwise();

// Use the results
for (Combination combination : results.combinations()) {
    String browserValue = combination.getValue(0).getValue();
    String osValue = combination.getValue(1).getValue();
    System.out.println(browserValue + " on " + osValue);
}
```

You can also use the direct generation method for even more concise code:

```java
CombinationTable results = JPWise.generatePairwise(
    new TestParameter("browser", Arrays.asList(chrome, firefox)),
    new TestParameter("os", Arrays.asList(windows, macOS))
);
```

Or pre-populate parameters and use them later:

```java
TestParameter browser = new TestParameter("browser", Arrays.asList(chrome, firefox));
TestParameter os = new TestParameter("os", Arrays.asList(windows, macOS));

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

1. **SimpleValue<T>**
   - Most basic implementation for constant values
   - Suitable for static, unchanging test inputs
   ```java
   SimpleValue<String> chrome = SimpleValue.of("Chrome", "116.0");
   ```

2. **GenericPartition<T>**
   - Supports dynamic value generation via a Supplier
   - Useful for values that need to be computed or fetched at runtime
   ```java
   // Dynamic version generation
   GenericPartition<String> windows = new GenericPartition<>(
       "Windows 11", 
       () -> String.format("22H2.%d", getCurrentBuild())
   );
   ```

3. **CyclingPartition<T>**
   - Cycles through a set of values
   - Thread-safe using AtomicInteger for cycling
   - Useful for testing multiple values in sequence
   ```java
   CyclingPartition<String> firefox = new CyclingPartition<>(
       "Firefox",
       "118.0.2",  // Default value
       Arrays.asList("118.0.2", "118.0.3", "118.1.0")  // Values to cycle through
   );
   ```

### Defining Compatibility Rules

JPWise provides a modern fluent API for defining compatibility rules between parameter partitions:

```java
import static com.functest.jpwise.core.PartitionPredicates.*;

// Define the parameter partitions first
List<EquivalencePartition<?>> browserPartitions = Arrays.asList(
    SimpleValue.of("Chrome"),
    SimpleValue.of("Firefox"),
    SimpleValue.of("Safari"),
    SimpleValue.of("Edge")
);

List<EquivalencePartition<?>> osPartitions = Arrays.asList(
    SimpleValue.of("Windows 11"),
    SimpleValue.of("Windows 10"),
    SimpleValue.of("macOS"),
    SimpleValue.of("Ubuntu")
);

// Define predicates using the fluent API
Predicate<EquivalencePartition<?>> isSafariBrowser = PartitionConditionBuilder.where()
    .nameIs("Safari")
    .parameterNameIs("browser")
    .build();

Predicate<EquivalencePartition<?>> isMacOS = PartitionConditionBuilder.where()
    .nameIs("macOS")
    .parameterNameIs("operatingSystem")
    .build();

// Define compatibility rules
List<CompatibilityPredicate> browserOsRules = Arrays.asList(
    // Safari only works with macOS
    (ep1, ep2) -> {
        if (isSafariBrowser.test(ep1)) return isMacOS.test(ep2);
        if (isSafariBrowser.test(ep2)) return isMacOS.test(ep1);
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
   - Use `PartitionConditionBuilder` for readable and maintainable rules
   - Combine conditions with `and()`, `or()`, and `not()`
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
import static com.functest.jpwise.core.PartitionPredicates.*;

// Browser versions with cycling values
CyclingPartition<String> chrome = new CyclingPartition<>("Chrome", "116.0.5845.96", 
    Arrays.asList("116.0.5845.96", "116.0.5845.97", "116.0.5845.98"));
CyclingPartition<String> firefox = new CyclingPartition<>("Firefox", "118.0.2",
    Arrays.asList("118.0.2", "118.0.3", "118.1.0"));
CyclingPartition<String> safari = new CyclingPartition<>("Safari", "17.0",
    Arrays.asList("17.0", "17.0.1", "17.1"));

// OS with dynamic versions using factory method
GenericPartition<String> windows11 = GenericPartition.of("Windows 11", 
    () -> "22H2 " + getLatestBuild("win11"));
GenericPartition<String> windows10 = GenericPartition.of("Windows 10", 
    () -> "22H2 " + getLatestBuild("win10"));
GenericPartition<String> macOS = GenericPartition.of("macOS", 
    () -> getLatestVersion("macos"));

// Screen resolutions using factory method
SimpleValue<String> hd = SimpleValue.of("HD", "1920x1080");
SimpleValue<String> uhd = SimpleValue.of("4K", "3840x2160");

// Define predicates for clearer rule composition
Predicate<EquivalencePartition<?>> isSafariBrowser = PartitionConditionBuilder.where()
    .nameIs("Safari")
    .parameterNameIs("browser")
    .build();

Predicate<EquivalencePartition<?>> isMacOS = PartitionConditionBuilder.where()
    .nameIs("macOS")
    .parameterNameIs("operatingSystem")
    .build();

Predicate<EquivalencePartition<?>> is4KResolution = PartitionConditionBuilder.where()
    .nameIs("4K")
    .parameterNameIs("resolution")
    .build();

Predicate<EquivalencePartition<?>> isWindows10 = PartitionConditionBuilder.where()
    .nameIs("Windows 10")
    .parameterNameIs("operatingSystem")
    .build();

// Define compatibility rules using predicates
List<CompatibilityPredicate> rules = Arrays.asList(
    // Safari only works with macOS
    (ep1, ep2) -> {
        if (isSafariBrowser.test(ep1)) return isMacOS.test(ep2);
        if (isSafariBrowser.test(ep2)) return isMacOS.test(ep1);
        return true;
    },
    // 4K not supported on Windows 10
    (ep1, ep2) -> {
        if (is4KResolution.test(ep1) && isWindows10.test(ep2)) return false;
        if (is4KResolution.test(ep2) && isWindows10.test(ep1)) return false;
        return true;
    }
);

// Generate test combinations using the builder
CombinationTable results = JPWise.builder()
    .parameter("browser", Arrays.asList(chrome, firefox, safari))
    .parameter("operatingSystem", Arrays.asList(windows11, windows10, macOS), rules)
    .parameter("resolution", Arrays.asList(hd, uhd))
    .generatePairwise();

// Verify the rules are enforced using defined predicates
for (Combination combination : results.combinations()) {
    // Safari should only appear with macOS
    if (isSafariBrowser.test(combination.getValue(0))) {
        assert isMacOS.test(combination.getValue(1)) : 
            "Safari must be paired with macOS";
    }
    
    // 4K resolution should not appear with Windows 10
    if (is4KResolution.test(combination.getValue(2))) {
        assert !isWindows10.test(combination.getValue(1)) : 
            "4K not supported on Windows 10";
    }
}
```

## TestNG DataProvider Integration

```java
@DataProvider(name = "browserConfigs")
public Object[][] getBrowserConfigs() {
    SimpleValue<String> chrome = SimpleValue.of("Chrome");  // Using name-only factory
    SimpleValue<String> firefox = SimpleValue.of("Firefox");
    
    CombinationTable results = JPWise.builder()
        .parameter("browser", Arrays.asList(chrome, firefox))
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