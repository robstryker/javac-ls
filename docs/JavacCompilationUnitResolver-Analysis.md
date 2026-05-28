# JavacCompilationUnitResolver Analysis

## Overview

**File:** `JavacCompilationUnitResolver.java` (1736 lines)  
**Purpose:** Main entry point for parsing Java files using javac and converting to Eclipse DOM AST  
**Package:** `org.eclipse.jdt.core.dom`

This class implements `ICompilationUnitResolver` and orchestrates the entire process of:
1. Parsing Java source files using javac compiler
2. Converting javac's JCTree to Eclipse DOM using JavacConverter
3. Resolving bindings (types, methods, fields)
4. Managing compilation context and diagnostics
5. Handling working copies and file management

## Core Workflow

```
Java Source Files
    ↓
JavacCompilationUnitResolver.parse() or .resolve()
    ↓
Create javac Context + JavacTask
    ↓
task.parse() → JCCompilationUnit (javac AST)
    ↓
JavacConverter.populateCompilationUnit() → CompilationUnit (Eclipse DOM)
    ↓
Optional: task.analyze() for binding resolution
    ↓
JavacBindingResolver attached to CompilationUnit
    ↓
Return CompilationUnit with resolved bindings
```

## Key Methods

### Public API (implements ICompilationUnitResolver)

1. **`parse(String[] sourceFilePaths, ...)`** - Parse file paths to DOM
2. **`parse(ICompilationUnit[] compilationUnits, ...)`** - Parse Eclipse compilation units
3. **`resolve(String[] sourceFilePaths, ...)`** - Parse + resolve bindings from file paths
4. **`resolve(ICompilationUnit[] compilationUnits, ...)`** - Parse + resolve bindings from units
5. **`toCompilationUnit(...)`** - Main conversion method for single unit

### Internal Core Methods

1. **`parse(org.eclipse.jdt.internal.compiler.env.ICompilationUnit[], ...)`** (line 658)
   - **The heart of the resolver** - 200+ lines
   - Creates javac Context, configures options
   - Sets up JavacTask
   - Parses with javac, converts to DOM
   - Handles comments, diagnostics, problems
   - Returns Map<SourceUnit, CompilationUnit>

2. **`createAST(...)`** (line 1452) - Creates AST instance with correct settings

3. **`resolveBindings(...)`** (line 549) - Walks DOM to populate binding map

4. **`resolveRequestedBindingKeys(...)`** (line 383) - Resolves specific binding keys

## Dependencies Analysis

### Heavy Eclipse Dependencies (Cannot Directly Migrate)

| Dependency | Usage | Difficulty |
|------------|-------|------------|
| **ICompilationUnit** | Eclipse compilation unit model | 🔴 Core - used everywhere |
| **IJavaProject** | Eclipse project model | 🔴 Core - classpath config |
| **IProgressMonitor** | Progress reporting | 🟡 Easy - can stub or remove |
| **IResource, IProject, IFolder** | Eclipse resource model | 🔴 Core - file resolution |
| **IClasspathEntry** | Eclipse classpath | 🔴 Core - javac config |
| **WorkingCopyOwner** | Eclipse working copy system | 🔴 Core - in-memory changes |
| **ASTRequestor, FileASTRequestor** | Callback interfaces | 🟡 Easy - can replace with return values |
| **SearchEngine, IndexQueryRequestor** | Eclipse search/indexing | 🔴 Advanced - multi-file type resolution |
| **JavaModelManager** | Eclipse Java model | 🔴 Core - state management |

### Javac Dependencies (Already Have These) ✅

- `shaded.com.sun.tools.javac.*` - javac compiler classes
- `shaded.javax.tools.*` - Java compiler API
- All available in javac-ls

### Internal Eclipse.jdt.javac Dependencies (Need to Evaluate)

| Dependency | Purpose | Lines |
|------------|---------|-------|
| JavacUtils | Classpath/context configuration | Used |
| JavacConverter | JCTree → DOM conversion | **Already migrated** ✅ |
| JavacBindingResolver | Binding resolution | Need to evaluate |
| JavacDiagnosticProblemConverter | Problem conversion | Need to evaluate |
| CachingJarsJavaFileManager | File manager caching | Performance optimization |
| JavacResolverTaskListener | Task event listener | Core workflow |

## What Can We Salvage for javac-ls?

### ✅ Direct Use (With Modifications)

**1. Core Parsing Logic** (lines 658-864)
- The main `parse()` method flow is valuable
- Shows how to properly configure javac Context
- Shows how to create JavacTask
- Shows how to use JavacConverter
- **Extract:** Core parsing logic into simplified method

**2. AST Creation** (lines 1452-1472)
- Shows how to create AST with correct compliance levels
- Setting scanner properties
- **Extract:** AST factory method

**3. JavacConverter Usage Pattern** (lines 794-845)
- Shows correct way to use JavacConverter
- Handling javadoc diagnostics
- Post-conversion cleanup (fix positions, handle comments)
- **Extract:** Conversion helper methods

**4. Comment Handling** (lines 1474-1515)
- Scanning for javadoc and regular comments
- Integrating comments into DOM
- **Extract:** Comment scanner utilities

**5. Option Configuration** (lines 699-700, 971-993)
- Shows javac option setup
- Compiler flags for parsing vs. analysis
- **Extract:** Option configuration helpers

### 🟡 Adaptable (Need Significant Changes)

**1. File Object Management** (lines 1114-1210)
- Converting paths to JavaFileObjects
- Handling virtual sources
- **Adapt:** Remove Eclipse IResource dependencies, use Path/File directly

**2. Classpath Configuration** (JavacUtils.configureJavacContext)
- We already have similar logic in our WorkspaceModel
- **Adapt:** Use our classpath discovery system

**3. Binding Resolution** (lines 383-460, 545-590)
- Useful patterns for resolving bindings
- **Adapt:** Simplify for our use case (no working copies, callbacks)

### 🔴 Not Usable (Too Eclipse-Specific)

**1. Working Copy Management** (lines 599-656)
- Requires Eclipse working copy system
- **Replace:** Simple in-memory cache or skip

**2. Search/Index Integration** (lines 1286-1405)
- Finding secondary types via Eclipse index
- **Replace:** Simple file scanning or skip

**3. Progress Monitoring**
- Eclipse IProgressMonitor throughout
- **Replace:** Remove or use simple callback

**4. Resource Path Resolution**
- IProject.findMember() style lookups
- **Replace:** Direct Path/File operations

## Recommended Extraction Strategy

### Phase 1: Core Parser (Minimal)

Create simplified `JavacDOMParser` class with:

```java
package org.jboss.tools.javac.ls.server.compiler.dom;

public class JavacDOMParser {
    
    /**
     * Parse Java source to DOM AST (no binding resolution)
     */
    public CompilationUnit parse(
        String sourceContent,
        String fileName,
        int apiLevel,
        Map<String, String> compilerOptions
    ) {
        // Extracted from JavacCompilationUnitResolver.parse()
        // - Create Context
        // - Create JavacTask  
        // - Parse to JCCompilationUnit
        // - Convert with JavacConverter
        // - Handle comments
        // - Return CompilationUnit
    }
    
    /**
     * Parse with binding resolution
     */
    public CompilationUnit parseAndResolve(
        String sourceContent,
        String fileName,
        List<File> classpath,
        int apiLevel,
        Map<String, String> compilerOptions
    ) {
        // Same as parse() but calls task.analyze()
        // and attaches JavacBindingResolver
    }
}
```

**Lines to extract:** ~658-864 (core parse method), ~1452-1472 (AST creation), ~794-845 (conversion)

**Estimated extraction:** ~400 lines of useful logic

### Phase 2: Binding Resolver (If Needed)

Evaluate `JavacBindingResolver` separately - may need for type hierarchy queries.

### Phase 3: Advanced Features (Later)

- Multi-file compilation
- Incremental parsing
- Caching compiled units

## Implementation Complexity

| Feature | Complexity | Priority | Effort |
|---------|-----------|----------|--------|
| Basic parsing (no bindings) | 🟢 Low | High | 1-2 days |
| Binding resolution | 🟡 Medium | High | 2-3 days |
| Comment integration | 🟢 Low | Medium | 1 day |
| Multi-file parsing | 🟡 Medium | Low | 2-3 days |
| Working copy support | 🔴 High | Low | N/A (skip) |

## Key Insights from Code

1. **Context is King** - javac Context must be carefully configured with:
   - Names (cached for reuse)
   - JavacFileManager (with caching)
   - Options (compliance, source, target)
   - DiagnosticListener
   
2. **Parsing vs. Analysis** - Clear separation:
   - `task.parse()` - Get JCTree only
   - `task.analyze()` - Perform full type checking and binding

3. **Comment Handling is Complex** - Multiple scanners:
   - JavadocTokenizer for comments
   - ECJ Scanner for line tables
   - Custom merge logic

4. **Position Fixing Required** - Post-conversion cleanup:
   - Fix parent/child position inconsistencies
   - Handle recovered nodes
   - Attach orphaned javadoc

5. **JavacConverter is Well-Used** - The migration we just did was correct!
   - Created once per compilation unit
   - Passed context, raw text, docEnabled flag
   - Called `populateCompilationUnit()`

## Files to Review Next

1. **JavacBindingResolver** - How bindings are resolved
2. **JavacResolverTaskListener** - Task event handling during compilation
3. **JavacDiagnosticProblemConverter** - Problem conversion
4. **CachingJarsJavaFileManager** - Performance optimizations

## Recommendation for javac-ls

**Start Small:** Create simplified `JavacDOMParser` that:
1. Takes String source content + filename
2. Creates Context with minimal options
3. Parses to JCTree
4. Converts to CompilationUnit with JavacConverter
5. Returns CompilationUnit (no bindings initially)

**Then Expand:** Add binding resolution once basic parsing works.

This gives us the ability to:
- Parse Java files to DOM for AI queries
- Cache DOM trees for performance
- Implement type hierarchy searches (with bindings)
- Support method/field searches

**Estimated Initial Implementation:** 3-5 days for basic parser with our already-migrated JavacConverter.
