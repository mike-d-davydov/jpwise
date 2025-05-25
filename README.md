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
- Rich comparison operators (EQ, NEQ, IN, NOT_IN, CONTAINS, CONTAINS_ALL)
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

JPWise uses equivalence partitions to group test inputs that are expected to behave similarly. The framework provides several implementations:

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

// Using the fluent API with PartitionConditionBuilder
Predicate<EquivalencePartition<?>> isSafari = PartitionConditionBuilder.where()
    .nameIs("Safari")
    .build();

Predicate<EquivalencePartition<?>> isBrowserVersion116 = PartitionConditionBuilder.where()
    .parameterNameIs("browser")
    .valueContains("116")
    .build();

// Combining conditions
Predicate<EquivalencePartition<?>> isModernBrowser = PartitionConditionBuilder.where()
    .parameterNameIs("browser")
    .nameIn("Chrome", "Firefox")
    .not(valueContains("legacy"))
    .build();

// Complex rules using the fluent API
List<CompatibilityPredicate> browserOsRules = Arrays.asList(
    (v1, v2) -> {
        // Safari only works with macOS
        if (PartitionConditionBuilder.where().nameIs("Safari").build().test(v1)) {
            return PartitionConditionBuilder.where()
                .nameIs("macOS")
                .parameterNameIs("operatingSystem")
                .build()
                .test(v2);
        }
        return true; // Other browsers work with all OS
    }
);

TestParameter browser = new TestParameter("browser", browserPartitions, browserOsRules);
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

// Define compatibility rules using the fluent API
List<CompatibilityPredicate> rules = Arrays.asList(
    // Safari only works with macOS
    (v1, v2) -> !PartitionConditionBuilder.where()
        .nameIs("Safari")
        .parameterNameIs("browser")
        .build()
        .test(v1) || 
        PartitionConditionBuilder.where()
        .nameIs("macOS")
        .parameterNameIs("operatingSystem")
        .build()
        .test(v2),
    
    // 4K not supported on older Windows 10
    (v1, v2) -> !PartitionConditionBuilder.where()
        .nameIs("4K")
        .parameterNameIs("resolution")
        .build()
        .test(v1) ||
        !PartitionConditionBuilder.where()
        .nameIs("Windows 10")
        .valueContains("19045")
        .build()
        .test(v2)
);

// Create test generator
TestInput input = new TestInput();
input.add(browser);
input.add(operatingSystem);
input.add(resolution);
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

## Advanced Usage

###Adding Value Compatibility Rules (Parameter Dependencies)

This is a key feature of JPWise, allowing you to define constraints where the validity of one parameter's value depends on the value of another.

```java 
 Advanced Usage

### Adding Value Compatibility Rules (Parameter Dependencies)

A key strength of JPWise is its ability to define and enforce compatibility rules between parameter values, ensuring that generated test combinations are realistic and valid for your system's constraints. This is achieved using the `CompatibilityPredicate` interface.

**Scenario: Browser and Operating System Compatibility**

Let's define rules where certain browsers are only compatible with specific operating systems.

```java
// 1. Define your TestParameters
TestParameter browserParam = new TestParameter("browser", Arrays.asList(
    SimpleValue.of("Chrome"),
    SimpleValue.of("Firefox"),
    SimpleValue.of("Safari"),
    SimpleValue.of("Edge")
));

TestParameter osParam = new TestParameter("operatingSystem", Arrays.asList(
    SimpleValue.of("Windows 11"),
    SimpleValue.of("Windows 10"),
    SimpleValue.of("macOS"),
    SimpleValue.of("Ubuntu") 
    // Using names from your JpWiseDemoTest for consistency
));

// 2. Define your CompatibilityPredicates as a list
List<CompatibilityPredicate> browserOsCompatibilityRules = Arrays.asList(
    (val1, val2) -> { // val1 and val2 are the two EquivalencePartition instances being considered for a pair
        EquivalencePartition<?> partition1 = val1;
        EquivalencePartition<?> partition2 = val2;

        // Ensure correct order for consistent rule application (e.g., browser is always first for this logic)
        // This makes the rule logic simpler as you know which parameter partition1 and partition2 refer to.
        if ("operatingSystem".equals(partition1.getParentParameter().getName()) && 
            "browser".equals(partition2.getParentParameter().getName())) {
            // Swap if OS came first
            partition1 = val2; 
            partition2 = val1;
        }

        // Proceed only if we have a browser value and an OS value
        if (!"browser".equals(partition1.getParentParameter().getName()) ||
            !"operatingSystem".equals(partition2.getParentParameter().getName())) {
            return true; // This rule doesn't apply to other parameter pairs
        }

        String browserName = partition1.getName();
        String osName = partition2.getName();

        // Rule 1: Safari is only compatible with macOS
        if (browserName.equals("Safari")) {
            return osName.equals("macOS");
        }

        // Rule 2: Edge is only compatible with Windows (10 or 11)
        if (browserName.equals("Edge")) {
            return osName.startsWith("Windows");
        }
        
        // Add more rules as needed...

        // By default, other combinations (e.g., Chrome/Firefox with any OS) are compatible
        return true;
    }
    // You can add more predicates to the list for other, unrelated rules if needed
);

// 3. Create TestInput and associate rules with one of the involved parameters
TestInput constrainedInput = new TestInput();
// The rules are associated with the 'browser' parameter here. The predicate logic
// then correctly identifies and constrains its pairing with 'operatingSystem'.
constrainedInput.add(new TestParameter("browser", browserParam.getPartitions(), browserOsCompatibilityRules));
constrainedInput.add(osParam); 
// You can add other parameters as well (e.g., screenResolution)
// constrainedInput.add(screenResolution);

// 4. Generate combinations
TestGenerator generator = new TestGenerator(constrainedInput);
generator.generate(new PairwiseAlgorithm());

// 5. Use the results
System.out.println("Pairwise combinations respecting Browser-OS compatibility:");
for (Combination combination : generator.result().combinations()) {
    System.out.println(combination);
    // Expected: No (Safari, Windows), (Safari, Ubuntu), (Edge, macOS), (Edge, Ubuntu)
    // Will see: (Safari, macOS), (Edge, Windows 11), (Edge, Windows 10)
    // And all combinations for Chrome & Firefox with all OS.
}
```

## Using Combinatorial Algorithm (All Combinations)

To generate all possible combinations (n-wise, where n is the number of parameters), use the `CombinatorialAlgorithm`.

```java
// Assuming 'input' is defined and populated with TestParameters (e.g., from Basic Usage)
TestInput input = new TestInput(); /* ... populate input ... */

TestGenerator generator = new TestGenerator(input);
// The second argument to generate() for CombinatorialAlgorithm can be a limit.
generator.generate(new CombinatorialAlgorithm(), 1000); // Generate all combinations, up to a safety limit of 1000

CombinationTable table = generator.result();
for (Combination combination : table.combinations()) {
    System.out.println(combination);
}
```

### Advanced Usage

#### Adding Value Compatibility Rules

```java
// Define parameters (e.g., browser and OS)
TestParameter browserParam = new TestParameter("browser", Arrays.asList(
    SimpleValue.of("Chrome"),
    SimpleValue.of("Firefox"),
    SimpleValue.of("Safari")
));

TestParameter osParam = new TestParameter("os", Arrays.asList(
    SimpleValue.of("Windows"),
    SimpleValue.of("MacOS"),
    SimpleValue.of("Linux")
));

// Define a compatibility rule: Safari browser is only compatible with MacOS
// This rule will be evaluated for pairs of values being considered for a combination.
List<CompatibilityPredicate> rules = Arrays.asList(
    (valueFromPair1, valueFromPair2) -> {
        // Check if this rule applies to the parameters of the current values
        String param1Name = valueFromPair1.getParentParameter().getName();
        String val1Name = valueFromPair1.getName();
        String param2Name = valueFromPair2.getParentParameter().getName();
        String val2Name = valueFromPair2.getName();

        // Rule: If browser is Safari, OS must be MacOS
        if (param1Name.equals("browser") && val1Name.equals("Safari") && 
            param2Name.equals("os") && !val2Name.equals("MacOS")) {
            return false; // Incompatible
        }
        // Symmetrically, if os is MacOS and browser is Safari
        if (param1Name.equals("os") && val1Name.equals("MacOS") && 
            param2Name.equals("browser") && !val2Name.equals("Safari")) {
            // This specific symmetric check might be redundant if the first one covers it,
            // or could be structured to handle parameter order flexibly.
            // For simplicity here, we assume the generator tries pairs in different orders
            // or that the predicate should be robust to the order of v1, v2.
            // The first check is the primary one for "Safari -> requires MacOS"
        }
        
        // Add other rules similarly...
        // e.g., Firefox not compatible with Linux (hypothetical)
        // if (param1Name.equals("browser") && val1Name.equals("Firefox") && 
        //     param2Name.equals("os") && val2Name.equals("Linux")) {
        //     return false; 
        // }

        return true; // Default to compatible if no specific rule makes it incompatible
    }
);

TestInput input = new TestInput();
// One way to apply rules: Associate with a specific parameter (if it primarily constrains that parameter's values with others)
// input.add(new TestParameter("browser", browserParam.getPartitions(), rules)); 
// input.add(osParam);
// OR, if your framework supports global rules applied to the TestInput (ideal for cross-parameter rules):
// input.add(browserParam);
// input.add(osParam);
// input.setGlobalCompatibilityRules(rules); // Assuming such a method exists or can be added

// For now, let's assume rules are applied during generation based on all TestParameters in the input.
// The exact mechanism of rule application (global vs. parameter-associated) needs to be clear from JPWise's API.
// The example below assumes the generator intelligently uses the rules.
// We will refine this example. For now, let's show the structure.

// This section needs a more complete example of how rules are registered and applied.
// For instance, if using the lambda predicate (valueFromPair1, valueFromPair2):
// It's often applied globally or to the TestGenerator.
// Example:
// TestGenerator generator = new TestGenerator(input, rules); // If constructor takes global rules
// generator.generate(new PairwiseAlgorithm());

// Placeholder for a more fleshed-out example here based on your library's actual API for rules.
// For now, the previous "Basic Usage" section had an example of associating rules with a TestParameter:
// input.add(new TestParameter("browser", browser.getPartitions(), rules)); // This implies the rule primarily involves 'browser'
// input.add(os);
// This is the part we need to make super clear with a real-world scenario.
```

#### Using Combinatorial Algorithm

```java
TestGenerator generator = new TestGenerator(input);
generator.generate(new CombinatorialAlgorithm(), 99); // Generate all possible combinations
```

## Architecture

The framework consists of several key components:

- `TestParameter`: Represents a test parameter with its possible equivalence partitions
- `EquivalencePartition`: Interface for parameter equivalence partitions
- `TestGenerator`: Main class for generating test combinations
- `GenerationAlgorithm`: Base class for test generation algorithms
- `PairwiseAlgorithm`: Implements pairwise test generation
- `CombinatorialAlgorithm`: Implements full combinatorial test generation
- `CombinationTable`: Holds and manages generated test combinations

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Acknowledgments

- Original author: Ng Pan Wei
- Contributors: Mikhail Davydov