# Contributing to JPWise

Thank you for your interest in contributing to JPWise! This document provides guidelines and information for contributors.

## Development Setup

### Prerequisites
- Java 8 or higher
- Maven 3.x
- Git

### Getting Started

1. **Fork and Clone**
   ```bash
   git clone https://github.com/your-username/jpwise.git
   cd jpwise
   ```

2. **Install Git Hooks**
   ```bash
   ./scripts/setup-hooks.sh
   ```
   This installs local Git hooks that will:
   - Run compilation and tests before each commit
   - Validate commit message format
   - Run full test suite before pushing

3. **Build and Test**
   ```bash
   mvn clean compile test
   ```

## Development Workflow

### Git Hooks

The project uses Git hooks to maintain code quality:

- **pre-commit**: Runs compilation and tests before allowing commits
- **commit-msg**: Enforces conventional commit message format
- **pre-push**: Runs full test suite before pushing

To bypass hooks temporarily (not recommended):
```bash
git commit --no-verify
git push --no-verify
```

### Commit Message Format

We use [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

**Types:**
- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Changes that do not affect the meaning of the code
- `refactor`: A code change that neither fixes a bug nor adds a feature
- `test`: Adding missing tests or correcting existing tests
- `chore`: Changes to the build process or auxiliary tools
- `ci`: Changes to CI configuration files and scripts
- `build`: Changes that affect the build system or external dependencies
- `perf`: A code change that improves performance
- `revert`: Reverts a previous commit

**Examples:**
```
feat: add new pairwise algorithm
fix(core): resolve compatibility rule bug
docs: update README with examples
test: add integration tests for JPWise
refactor: simplify TestParameter class
```

## Code Quality

### Automated Checks

The project uses several tools for code quality:

- **SpotBugs**: Static analysis for bug detection (only critical issues fail builds)
- **Checkstyle**: Code style checking (Google Java Style)
- **Spotless**: Automatic code formatting
- **JaCoCo**: Code coverage reporting

### Running Quality Checks

```bash
# Run all quality checks (SpotBugs generates reports but doesn't fail)
mvn clean compile checkstyle:check

# Check for critical SpotBugs issues only (can fail build)
./scripts/spotbugs-critical-check.sh

# Format code automatically
mvn spotless:apply

# Generate coverage report
mvn clean test jacoco:report
```

### SpotBugs Critical vs Non-Critical Issues

**Critical Issues (Priority 1) - WILL FAIL BUILD:**
- Security vulnerabilities
- Null pointer dereferences
- Resource leaks
- Critical concurrency issues
- SQL injection risks

**Non-Critical Issues (Priority 2-4) - INFORMATIONAL ONLY:**
- Code style suggestions
- Performance optimizations
- Minor correctness issues
- Best practice recommendations

View all issues in the HTML report: `target/spotbugs.html`

## GitHub Actions CI/CD

The project uses GitHub Actions for continuous integration:

### Workflows

1. **CI Workflow** (`.github/workflows/ci.yml`)
   - Runs on push/PR to main/develop branches
   - Tests on multiple Java versions (8, 11, 17, 21)
   - Tests on multiple OS (Ubuntu, Windows, macOS)
   - Runs code quality checks
   - Generates coverage reports

2. **Release Workflow** (`.github/workflows/release.yml`)
   - Triggers on version tags (v*)
   - Creates GitHub releases
   - Publishes artifacts to GitHub Packages
   - Generates release notes

3. **Dependabot** (`.github/dependabot.yml`)
   - Automatically updates dependencies
   - Creates PRs for Maven and GitHub Actions updates

### Status Badges

Add these to your fork's README if desired:

```markdown
![CI](https://github.com/your-username/jpwise/workflows/CI/badge.svg)
![Release](https://github.com/your-username/jpwise/workflows/Release/badge.svg)
```

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TestClassName

# Run tests with coverage
mvn clean test jacoco:report
```

### Test Structure

- Unit tests: `src/test/java/com/functest/jpwise/core/`
- Integration tests: `src/test/java/com/functest/jpwise/`
- Test naming: `*Test.java`

### Writing Tests

- Use TestNG annotations
- Follow existing test patterns
- Include both positive and negative test cases
- Test edge cases and error conditions
- Maintain or improve code coverage

## Pull Request Process

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Changes**
   - Follow coding standards
   - Add tests for new functionality
   - Update documentation if needed

3. **Commit Changes**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

4. **Push and Create PR**
   ```bash
   git push origin feature/your-feature-name
   ```
   - Use the PR template
   - Link related issues
   - Ensure all CI checks pass

5. **Code Review**
   - Address reviewer feedback
   - Keep PR focused and small
   - Squash commits if requested

## Release Process

Releases are automated through GitHub Actions:

1. **Create Release Tag**
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

2. **Automated Process**
   - GitHub Actions creates release
   - Artifacts are built and attached
   - Release notes are generated
   - Packages are published

## Code Style

### Java Style Guide

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Spotless for automatic formatting
- Maximum line length: 100 characters
- Use meaningful variable and method names
- Add JavaDoc for public APIs

### Example Code Style

```java
/**
 * Represents a test parameter with multiple equivalence partitions.
 *
 * @author Your Name
 */
public class TestParameter {
  private final String name;
  private final List<EquivalencePartition> partitions;

  /**
   * Creates a new test parameter.
   *
   * @param name the parameter name
   * @param partitions the equivalence partitions
   */
  public TestParameter(String name, List<EquivalencePartition> partitions) {
    this.name = requireNonNull(name, "Parameter name cannot be null");
    this.partitions = new ArrayList<>(requireNonNull(partitions, "Partitions cannot be null"));
  }
}
```

## Getting Help

- **Issues**: Use GitHub issues for bugs and feature requests
- **Discussions**: Use GitHub discussions for questions
- **Documentation**: Check the README and JavaDoc

## License

By contributing, you agree that your contributions will be licensed under the MIT License. 