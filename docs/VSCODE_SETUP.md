# VSCode Setup for JPWise

This document explains how to configure VSCode for optimal development experience with the JPWise project.

## Required Extensions

Install the following VSCode extensions (they should be automatically suggested when you open the project):

1. **Run on Save** (`emeraldwalk.runonsave`) - Automatically runs Spotless formatting when you save Java files
2. **Extension Pack for Java** (`vscjava.vscode-java-pack`) - Complete Java development support
3. **Maven for Java** (`vscjava.vscode-maven`) - Maven integration

## Automatic Formatting Setup

The project is configured to automatically format Java files using Spotless when you save them. Here's how it works:

### Current Configuration

- **Format on Save**: Disabled for built-in Java formatter
- **Spotless on Save**: Enabled via the "Run on Save" extension
- **Import Organization**: Automatically organizes imports on save

### How It Works

1. When you save a `.java` file, the "Run on Save" extension automatically runs:
   ```bash
   mvn spotless:apply -q
   ```

2. This applies Google Java Format styling to all Java files in the project

3. The formatting happens in the background with minimal output

### Manual Formatting

You can also format manually using:

- **Command Palette** (`Ctrl+Shift+P`): Run "Tasks: Run Task" → "Spotless Apply"
- **Keyboard Shortcut**: `Ctrl+Shift+F` (configured for Spotless Apply)
- **Terminal**: `mvn spotless:apply`

## Logging Configuration

The project uses Logback for logging with the following configuration:

- **Console Output**: Only WARN level and above
- **File Output**: All logs written to `target/jpwise.log`
- **Spotless Logs**: Suppressed to reduce noise

## Troubleshooting

### Spotless Not Running on Save

1. Ensure the "Run on Save" extension is installed and enabled
2. Check that the workspace settings in `.vscode/settings.json` are correct
3. Verify that Maven is available in your PATH

### Formatting Issues

1. Run `mvn spotless:check` to see formatting violations
2. Run `mvn spotless:apply` to fix all violations
3. Check the console output for any Maven errors

### Performance Issues

If automatic formatting is too slow:

1. Disable format-on-save: Set `"emeraldwalk.runonsave"` to `[]` in settings
2. Use manual formatting with `Ctrl+Shift+F`
3. Consider running Spotless only before commits using Git hooks

## Alternative Setup (Manual Formatting Only)

If you prefer manual formatting, you can disable automatic formatting:

```json
{
    "emeraldwalk.runonsave": {
        "commands": []
    }
}
```

Then use the keyboard shortcuts or tasks to format when needed. 