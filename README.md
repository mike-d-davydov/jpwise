# JPWise - Java Pairwise Testing Framework

JPWise is a powerful Java framework for generating pairwise test combinations, with support for complex parameter relationships and compatibility rules. This is a heavily reworked and extended version of the original jWise library by Pan Wei.

## Table of Contents
- [History](#history)
- [Terminology](#terminology)
- [Author](#author)
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

```java
// Define parameters and their partitions
TestParameter browser = new TestParameter("browser", Arrays.asList(
    SimpleValue.of("Chrome"),
    SimpleValue.of("Firefox"),
    SimpleValue.of("Safari")
));

TestParameter os = new TestParameter("os", Arrays.asList(
    SimpleValue.of("Windows"),
    SimpleValue.of("MacOS"),
    SimpleValue.of("Linux")
));

// Create test input
TestInput input = new TestInput();
input.add(browser);
input.add(os);

// Generate test combinations
TestGenerator generator = new TestGenerator(input);
generator.generate(new PairwiseAlgorithm());

// Get results
CombinationTable results = generator.result();
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

Predicate<EquivalencePartition<?>> isEdgeBrowser = PartitionConditionBuilder.where()
    .nameIs("Edge")
    .parameterNameIs("browser")
    .build();

Predicate<EquivalencePartition<?>> isWindowsOS = PartitionConditionBuilder.where()
    .parameterNameIs("operatingSystem")
    .nameIn("Windows 11", "Windows 10")
    .build();

// Complex rules using the predicates
List<CompatibilityPredicate> browserOsRules = Arrays.asList(
    // Safari only works with macOS
    (ep1, ep2) -> {
        if (isSafariBrowser.test(ep1)) return isMacOS.test(ep2);
        if (isSafariBrowser.test(ep2)) return isMacOS.test(ep1);
        return true;
    },
    // Edge only works with Windows
    (ep1, ep2) -> {
        if (isEdgeBrowser.test(ep1)) return isWindowsOS.test(ep2);
        if (isEdgeBrowser.test(ep2)) return isWindowsOS.test(ep1);
        return true;
    }
);

// Create parameters with rules
TestParameter browser = new TestParameter("browser", browserPartitions, browserOsRules);
TestParameter os = new TestParameter("os", osPartitions);

// Create test input
TestInput input = new TestInput();
input.add(browser);  // Rules are associated with the browser parameter
input.add(os);
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
// Browser versions with cycling values
TestParameter browser = new TestParameter("browser", Arrays.asList(
    new CyclingPartition<>("Chrome", "116.0.5845.96", 
        Arrays.asList("116.0.5845.96", "116.0.5845.97", "116.0.5845.98")),
    new CyclingPartition<>("Firefox", "118.0.2",
        Arrays.asList("118.0.2", "118.0.3", "118.1.0")),
    new CyclingPartition<>("Safari", "17.0",
        Arrays.asList("17.0", "17.0.1", "17.1"))
));

// OS with dynamic version/build numbers
TestParameter operatingSystem = new TestParameter("operatingSystem", Arrays.asList(
    new GenericPartition<>("Windows 11", () -> "22H2 " + getLatestBuild("win11")),
    new GenericPartition<>("Windows 10", () -> "22H2 " + getLatestBuild("win10")),
    new GenericPartition<>("macOS", () -> getLatestVersion("macos"))
));

// Screen resolutions (static values)
TestParameter resolution = new TestParameter("resolution", Arrays.asList(
    SimpleValue.of("HD", "1920x1080"),
    SimpleValue.of("4K", "3840x2160")
));

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

Predicate<EquivalencePartition<?>> isOldWindows10 = PartitionConditionBuilder.where()
    .nameIs("Windows 10")
    .valueContains("19045")
    .parameterNameIs("operatingSystem")
    .build();

// Define compatibility rules with explicit symmetry handling
List<CompatibilityPredicate> rules = Arrays.asList(
    // Safari only works with macOS
    // This rule is symmetric - it should be checked regardless of parameter order
    (ep1, ep2) -> {
        // Check both directions explicitly for clarity
        if (isSafariBrowser.test(ep1)) return isMacOS.test(ep2);
        if (isSafariBrowser.test(ep2)) return isMacOS.test(ep1);
        return true; // Other browsers work with all OS
    },
    
    // 4K not supported on older Windows 10
    // This rule is symmetric - the constraint applies regardless of parameter order
    (ep1, ep2) -> {
        // First direction: 4K resolution with OS
        if (is4KResolution.test(ep1) && isOldWindows10.test(ep2)) return false;
        // Reverse direction: OS with 4K resolution
        if (is4KResolution.test(ep2) && isOldWindows10.test(ep1)) return false;
        return true; // All other combinations are valid
    }
);

// Create test input and associate rules
TestInput input = new TestInput();

// Associate rules with relevant parameters
// Note: When rules are associated with a parameter, they are primarily evaluated
// when that parameter is being considered in a pair. However, it's still good
// practice to make rules symmetric for robustness and clarity.
browser.setCompatibilityRules(rules);    // Rules affecting browser
resolution.setCompatibilityRules(rules);  // Rules affecting resolution

input.add(browser);
input.add(operatingSystem);
input.add(resolution);

// Generate combinations
TestGenerator generator = new TestGenerator(input);
generator.generate(new PairwiseAlgorithm());

// The generated combinations will respect all compatibility rules,
// regardless of the order in which parameters are paired
CombinationTable results = generator.result();

// Verify the rules are enforced
for (Combination combination : results.combinations()) {
    // Safari should only appear with macOS
    if (isSafariBrowser.test(combination.getValue(0)) || 
        isSafariBrowser.test(combination.getValue(1)) ||
        isSafariBrowser.test(combination.getValue(2))) {
        assert isMacOS.test(combination.getValue(1)) : "Safari must be paired with macOS";
    }
    
    // 4K resolution should not appear with old Windows 10
    if (is4KResolution.test(combination.getValue(2))) {
        assert !isOldWindows10.test(combination.getValue(1)) : "4K not supported on old Windows 10";
    }
}

This example demonstrates several important concepts:

1. **Explicit Predicate Definition**: We define predicates separately for better readability and reuse.

2. **Symmetric Rule Handling**: Each rule explicitly handles both possible orders of parameters:
   - When checking Safari compatibility, we test both `(Safari, OS)` and `(OS, Safari)` pairs
   - For 4K resolution compatibility, we check both `(4K, Windows)` and `(Windows, 4K)` pairs

3. **Rule Association**: Rules are associated with specific parameters via `setCompatibilityRules`:
   - When associated with `browser`, rules are primarily evaluated when browser partitions are being paired
   - When associated with `resolution`, rules are evaluated when resolution partitions are being paired
   - Making rules symmetric ensures correct behavior regardless of parameter order

4. **Verification**: The example includes verification code to ensure rules are properly enforced in the generated combinations.
```

## TestNG DataProvider Integration
```java
@DataProvider(name = "browserConfigs")
public Object[][] getBrowserConfigs() {
    TestParameter browser = new TestParameter("browser", Arrays.asList(
        SimpleValue.of("Chrome", "116.0"),
        SimpleValue.of("Firefox", "118.0")
    ));
    
    TestInput input = new TestInput();
    input.add(browser);
    
    TestGenerator generator = new TestGenerator(input);
    generator.generate(new PairwiseAlgorithm());
    
    return generator.result().asDataProvider();
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