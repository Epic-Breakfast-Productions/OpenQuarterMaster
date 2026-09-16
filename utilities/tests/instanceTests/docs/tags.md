# Tags

This document outlines the tags used to identify the tests.

## Usage

`mvn test -Dcucumber.filter.tags="@smoke and @fast"`

## Tags

### Features

Tests that test specific features are denoted with: 

`@feature`

Features are identified by the following tag structure:

`@feature-#`

`@feature-#.#`

### Testing Types

#### Error handling

`@type-error`
