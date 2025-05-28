# VS Code Setup Guide

This document explains how to configure VS Code for optimal development experience with the JPWise project.

## Required Extensions

1. **Language Support for Java** (`vscjava.vscode-java-pack`) - Complete Java development support
2. **CheckStyle for Java** (`shengchen.vscode-checkstyle`) - Provides CheckStyle integration
3. **Maven for Java** (`vscjava.vscode-maven`) - Maven integration
4. **Run on Save** (`emeraldwalk.runonsave`) - Automatically runs formatting when you save Java files

## Configuration

The project is configured to automatically format Java files when you save them. Here's how it works:

### Current Configuration

- **Format on Save**: Enabled for built-in Java formatter
- **CheckStyle on Save**: Enabled via the "Run on Save" extension
- **Import Organization**: Automatically organizes imports on save

### How It Works

1. When you save a `.java` file, the following happens automatically:
   - Code is formatted according to Google Java Style
   - Imports are organized
   - CheckStyle rules are validated

2. The formatting happens in the background with minimal output

### Manual Formatting

You can also format manually using:

- **Command Palette** (`Ctrl+Shift+P`): Run "Format Document"
- **Keyboard Shortcut**: `Ctrl+Shift+F` (configured for Format Document)
- **Terminal**: `mvn checkstyle:check`

## Troubleshooting

### Formatting Not Working

If formatting is not working automatically:

1. Ensure all required extensions are installed and enabled
2. Check VS Code settings in `.vscode/settings.json`
3. Verify that Maven is available in your PATH
4. Try running `mvn checkstyle:check` to see any issues

### Performance Issues

If automatic formatting is too slow:

1. Disable format-on-save: Set `"editor.formatOnSave"` to `false` in settings
2. Use manual formatting with `Ctrl+Shift+F`
3. Consider running CheckStyle only before commits using Git hooks 