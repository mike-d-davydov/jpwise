# JPWise - Java Pairwise Testing Framework

JPWise is a powerful Java framework for generating pairwise test combinations, with support for complex parameter relationships and compatibility rules. This is a heavily reworked and extended version of the original jWise library by Pan Wei.

## History

- **2010**: Original jWise library created by Pan Wei
- **2013**: Major rework by Mikhail Davydov
  - Complete rewrite of core algorithms
  - Added support for generic types
  - Improved compatibility rules system
  - Added comprehensive test suite
- **2025**: Modern uplift
  - Updated to latest dependency versions
  - Enhanced test coverage
  - Improved documentation and examples
  - Added TestNG DataProvider integration

## Author
- **Mikhail Davydov** - Main author and maintainer
- Based on original jWise library by Pan Wei

## Features
- Pairwise (2-wise) test case generation
- Support for complex parameter value relationships
- Compatibility rules between parameters
- Generic type support for parameter values
- Rich comparison operators (EQ, NEQ, IN, NOT_IN, CONTAINS, CONTAINS_ALL)
- TestNG DataProvider integration

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
// Define parameters and values
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

// Add compatibility rules if needed
List<CompatibilityPredicate> rules = Arrays.<CompatibilityPredicate>asList(
    (v1, v2) -> {
        if (v1.getName().equals("Safari")) {
            return v2.getName().equals("MacOS");
        }
        return true;
    }
);

// Generate test combinations
TestInput input = new TestInput();
input.add(new TestParameter("browser", browser.getValues(), rules));
input.add(os);

TestGenerator generator = new TestGenerator(input);
generator.generate(new PairwiseAlgorithm());

// Get results
for (Combination combination : generator.result().combinations()) {
    System.out.println(combination);
}
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
mvn test -Dtest=TestClassName     # Run specific test
```

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments
- Pan Wei for the original jWise library that served as the foundation for this project
- All contributors who have helped improve and extend the framework

## Requirements

- Java 8 or higher
- Maven 3.x

## Usage

### Basic Example

```java
// Create test parameters
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

// Create test input with parameters
TestInput input = new TestInput();
input.add(browser);
input.add(os);

// Generate pairwise combinations
TestGenerator generator = new TestGenerator(input);
generator.generate(new PairwiseAlgorithm());

// Get results
CombinationTable table = generator.result();
for (Combination combination : table.combinations()) {
    System.out.println(combination);
}
```

### Advanced Usage

#### Adding Value Compatibility Rules

```java
// Define compatibility rules
List<CompatibilityPredicate> rules = Arrays.asList(
    ValueCompatibility.valuesAre(
        new ParameterValueMatcher(Field.NAME, ConditionOperator.EQ, "Safari"),
        new ParameterValueMatcher(Field.NAME, ConditionOperator.NEQ, "Windows")
    )
);

TestParameter browser = new TestParameter("browser", browserValues, rules);
```

#### Using Combinatorial Algorithm

```java
TestGenerator generator = new TestGenerator(input);
generator.generate(new CombinatorialAlgorithm(), 99); // Generate all possible combinations
```

## Architecture

The framework consists of several key components:

- `TestParameter`: Represents a test parameter with its possible values
- `ParameterValue`: Interface for parameter values
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

## Running Tests

To run all tests:
```bash
mvn test
```

To run a specific test class:
```bash
mvn test -Dtest=TestClassName
```

For example:
```bash
mvn test -Dtest=JpWiseDataProviderDemoTest
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Original author: Ng Pan Wei
- Contributors: Mikhail Davydov

 
 
 
 
