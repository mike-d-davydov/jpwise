---
name: Bug report
about: Create a report to help us improve
title: '[BUG] '
labels: 'bug'
assignees: ''

---

**Describe the bug**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Create test parameters with '...'
2. Generate combinations using '....'
3. Run test with '....'
4. See error

**Expected behavior**
A clear and concise description of what you expected to happen.

**Actual behavior**
A clear and concise description of what actually happened.

**Code Sample**
```java
// Minimal code sample that reproduces the issue
TestParameter browser = new TestParameter("browser", ...);
CombinationTable results = JPWise.generatePairwise(browser, ...);
```

**Environment:**
 - JPWise version: [e.g. 1.0.0]
 - Java version: [e.g. 11.0.2]
 - OS: [e.g. Windows 10, Ubuntu 20.04]
 - Maven version: [e.g. 3.8.1]

**Stack Trace**
If applicable, add the full stack trace here.

**Additional context**
Add any other context about the problem here. 