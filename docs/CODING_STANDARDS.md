# Coding Standards

This document establishes the patterns, code style guidelines, and quality standards for the Lumora codebase.

## Backend (Java) Standards

### 1. Naming Conventions
- Packages: Lowercase only, starting with `com.lumora`.
- Classes/Interfaces: CamelCase starting with an uppercase letter (e.g., `HnswIndex`, `SearchStrategy`).
- Methods/Variables: camelCase starting with a lowercase letter (e.g., `doSearch()`, `workspaceId`).

### 2. Design Patterns
- **Strategy Pattern**: Use for pluggable algorithms so that algorithms can be swapped dynamically.
- **Template Method Pattern**: Use inside base classes (like `AbstractSearchStrategy`) to manage timing analytics and validation.

---

## Frontend (TypeScript/React) Standards

### 1. Naming Conventions
- React Components: PascalCase filenames and function names (e.g., `Search.tsx`).
- Helper functions/hooks: camelCase (e.g., `useWorkspaces.ts`).

### 2. Formatting
- Enforced using Prettier and ESLint. Use 2-space indentation and avoid semicolons where applicable.
