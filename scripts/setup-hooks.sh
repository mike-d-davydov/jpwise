#!/bin/bash

# JPWise Git Hooks Setup Script
# This script installs Git hooks for the JPWise project

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
HOOKS_DIR="$PROJECT_ROOT/.git/hooks"

echo "Setting up JPWise Git hooks..."

# Check if we're in a Git repository
if [ ! -d "$PROJECT_ROOT/.git" ]; then
    echo "Error: Not in a Git repository"
    exit 1
fi

# Create hooks directory if it doesn't exist
mkdir -p "$HOOKS_DIR"

# Install pre-commit hook
echo "Installing pre-commit hook..."
cp "$SCRIPT_DIR/pre-commit" "$HOOKS_DIR/pre-commit"
chmod +x "$HOOKS_DIR/pre-commit"

# Install commit-msg hook
echo "Installing commit-msg hook..."
cp "$SCRIPT_DIR/commit-msg" "$HOOKS_DIR/commit-msg"
chmod +x "$HOOKS_DIR/commit-msg"

# Create a simple pre-push hook
echo "Installing pre-push hook..."
cat > "$HOOKS_DIR/pre-push" << 'EOF'
#!/bin/bash

# JPWise Pre-push Hook
# This hook runs before pushing to ensure all tests pass

echo "Running pre-push checks..."

# Run full test suite
echo "Running full test suite..."
if ! mvn clean test; then
    echo "Error: Tests failed. Push aborted."
    exit 1
fi

echo "Pre-push checks passed!"
exit 0
EOF

chmod +x "$HOOKS_DIR/pre-push"

# Make utility scripts executable
echo "Making utility scripts executable..."
chmod +x "$SCRIPT_DIR/spotbugs-critical-check.sh"

echo "Git hooks installed successfully!"
echo ""
echo "Installed hooks:"
echo "  - pre-commit: Runs compilation and tests before each commit"
echo "  - commit-msg: Validates commit message format"
echo "  - pre-push: Runs full test suite before pushing"
echo ""
echo "To disable hooks temporarily, use:"
echo "  git commit --no-verify"
echo "  git push --no-verify"
echo ""
echo "To uninstall hooks, run:"
echo "  rm .git/hooks/pre-commit .git/hooks/commit-msg .git/hooks/pre-push" 