# JTFix

A parallel CLI tool that detects and fixes Java identifier naming issues while enforcing clean naming conventions.

## Overview

JTFix is designed to help Java developers detect and fix identifier naming issues such as spelling mistakes and naming convention violations.

It performs safe corrections without breaking code. Ambiguous tokens are reported and never modified automatically.

## Why JTFix?

Java codebases often contain spelling mistakes, unclear wording, and inconsistent identifier names.

JTFix scans projects to detect and safely fix these issues while improving naming consistency across entire codebases.

## Features

- Scan entire Java projects
- Parallel analysis pipeline
- Detect spelling and naming issues
- Suggest safe identifier corrections
- Enforce Java naming conventions
- Manual review for unresolved tokens
- Unix-friendly output that integrates well with tools such as fzf

## Example Usage

```bash
# Scan an entire project
jtfx scan .

# Review unresolved tokens manually
jtfx scan . --manual

# Select multiple results with fzf and apply suggestion #1
jtfx scan . | fzf --multi | cut -d: -f1 | xargs jtfx apply -s 1

# Apply a suggestion directly
jtfx apply <tokenId> -s <suggestionNumber>
```

## Architecture

JTFix is built around a fail-fast pipeline composed of small independent processing stages.

Project scan -> Parallel extraction -> Token grouping -> Parallel suggestion engine -> Apply stage

Each stage has a single responsibility and can be evolved or replaced independently. The scan and apply phases are intentionally separated to keep commands stateless and shell-friendly.

## Installation

JTFix is currently in active development and can be run from source.

```bash
git clone https://www.github.com/AurelienDelcey/JTFix-Java-Typo-Fixer.git
cd JTFix
mvn exec:java
```

Packaged installation methods may be added in future releases.

## Roadmap

- V1: Core scanning pipeline, parallel processing, suggestions engine, manual review mode, and built-in English dictionary support
- V1.x: Improved CLI UX, packaged installation, better suggestion quality, and user-defined whitelists
- V2: Smarter token resolution, advanced refactoring workflows, and specialized dictionaries for Java and business terminology

## Tech Stack

- Java 21
- Maven
- JUnit 5
- SLF4J
- Logback

## Status

JTFix is currently in active development.

The architecture and roadmap are defined, and the first public version is focused on delivering a usable core CLI with parallel scanning, safe suggestions, and manual review support.

## Author

Created by **Aurélien Delcey**

Personal project exploring Java concurrency, shell-friendly tooling, and modular software design.

- GitHub: https://github.com/AurelienDelcey
- LinkedIn: https://www.linkedin.com/in/aurelien-delcey
