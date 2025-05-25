# JPWise - Java Pairwise Testing Framework

JPWise is a Java library for generating test combinations using pairwise and combinatorial testing algorithms. It helps reduce the number of test cases while maintaining good test coverage by generating optimal combinations of test parameters.

## Features

- Pairwise test case generation (2-wise combinations)
- Full combinatorial test case generation
- Support for parameter value compatibility rules
- Flexible parameter value definitions
- TestNG integration
- Detailed logging and reporting

## Requirements

- Java 8 or higher
- Maven 3.x

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.googlecode.groovy-toy-orm</groupId>
    <artifactId>groovy-toy-orm</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

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

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Original author: Ng Pan Wei
- Contributors: Mikhail Davydov

 
 
 
 
