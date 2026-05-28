# JavacConverter Package Structure Proposal

## Overview

This document proposes the package organization for migrating JavacConverter and JavadocConverter into javac-ls.

## Current State Analysis

### Existing Bundles

```
framework/bundles/
├── org.jboss.tools.jdt.core.dom/          # DOM-related Eclipse-compatible classes
│   └── src/main/java/
│       └── org/eclipse/jdt/
│           ├── core/dom/                   # Public DOM API (exported)
│           └── internal/
│               ├── compiler/classfmt/      # ClassFileConstants (has AccDeprecated, JDK1_5, JDK16) ✅
│               ├── compiler/util/          # Util class
│               └── core/dom/util/          # DOMASTUtil class
│
└── org.jboss.tools.javac.ls.server/        # Server implementation
    └── src/main/java/
        └── org/jboss/tools/javac/ls/server/
            └── model/
                └── classpath/              # Classpath discovery
```

### Already Available in javac-ls

✅ **org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants** (in dom bundle)
- Has `AccDeprecated = 0x100000`
- Has `JDK1_5 = ((long)MAJOR_VERSION_1_5 << 16) + MINOR_VERSION_0`
- Has `JDK16 = ((long)MAJOR_VERSION_16 << 16) + MINOR_VERSION_0`

✅ **org.eclipse.jdt.internal.core.dom.util.DOMASTUtil** (in dom bundle)
- Utility methods for AST/DOM operations
- Good candidate for additional DOM utilities

## Proposed Package Structure

### Option 1: Internal Javac Package (RECOMMENDED) ✅

Create a new internal package specifically for javac-related DOM conversion:

```
org.jboss.tools.jdt.core.dom/
└── src/main/java/
    └── org/eclipse/jdt/internal/core/dom/
        ├── javac/                                    # NEW: Javac converters
        │   ├── JavacConverter.java                   # Main JCTree → DOM converter (3813 lines)
        │   ├── JavadocConverter.java                 # DCTree → Javadoc converter (1005 lines)
        │   └── javadoc/                              # NEW: Javadoc support
        │       ├── JavacJdtMarkupParser.java         # (116 lines)
        │       ├── JavacJdtMarkupTag.java            # (29 lines)
        │       └── JavacJdtMarkupTagAttribute.java   # (29 lines)
        │
        └── util/
            ├── DOMASTUtil.java                       # EXISTING
            └── JavacDOMUtil.java                     # NEW: Javac-specific utilities
                                                      #   - findMatch()
                                                      #   - isGenerated()
                                                      #   - FAKE_IDENTIFIER constant
```

**Rationale:**
- Converters work with DOM → belong in dom bundle
- Internal implementation → use `internal` package
- Javac-specific → separate `javac` subpackage keeps things organized
- Utilities in `util` package follow existing pattern

**Package declarations:**
```java
package org.eclipse.jdt.internal.core.dom.javac;           // For converters
package org.eclipse.jdt.internal.core.dom.javac.javadoc;   // For javadoc helpers
package org.eclipse.jdt.internal.core.dom.util;            // For utilities
```

### Option 2: Server-Side Package (Alternative)

Create packages under the server bundle:

```
org.jboss.tools.javac.ls.server/
└── src/main/java/
    └── org/jboss/tools/javac/ls/server/
        └── compiler/
            └── dom/
                ├── converter/
                │   ├── JavacConverter.java
                │   └── JavadocConverter.java
                ├── javadoc/
                │   └── ... (javadoc helpers)
                └── util/
                    └── JavacConverterUtil.java
```

**Rationale:**
- Server bundle is our main implementation
- More independence from Eclipse package structure
- Clearer ownership (JBoss namespace)

**Package declarations:**
```java
package org.jboss.tools.javac.ls.server.compiler.dom.converter;
package org.jboss.tools.javac.ls.server.compiler.dom.javadoc;
package org.jboss.tools.javac.ls.server.compiler.dom.util;
```

## Dependency Management

### Constants - Use Existing ClassFileConstants ✅

**No new constants needed!** Already available:

```java
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

// Use these directly:
ClassFileConstants.AccDeprecated
ClassFileConstants.JDK1_5
ClassFileConstants.JDK16
```

### RecoveryScanner.FAKE_IDENTIFIER

**Only need to copy 1 constant:**

```java
// In JavacDOMUtil.java or DOMASTUtil.java
public static final char[] FAKE_IDENTIFIER = "$missing$".toCharArray();
public static final String FAKE_IDENTIFIER_STRING = new String(FAKE_IDENTIFIER);
```

### Utility Methods

**Add to new JavacDOMUtil.java** (or extend DOMASTUtil.java):

```java
package org.eclipse.jdt.internal.core.dom.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MarkerAnnotation;

public class JavacDOMUtil {
    
    // From RecoveryScanner
    public static final char[] FAKE_IDENTIFIER = "$missing$".toCharArray();
    public static final String FAKE_IDENTIFIER_STRING = new String(FAKE_IDENTIFIER);
    
    /**
     * Find a match for text in content, handling Unicode escapes.
     * Copied from old JavacUtils.findMatch()
     */
    public static int[] findMatch(String content, String text, int searchStart, int searchEnd) {
        // ~50 lines from JavacUtils.findMatch()
        // Implementation handles \uXXXX unicode escapes
    }
    
    /**
     * Check if AST node is generated by Lombok or similar.
     * Copied from DOMCodeSelector.isGenerated()
     */
    public static boolean isGenerated(ASTNode node) {
        // ~20 lines checking for @lombok.Generated annotation
        if (node == null) return false;
        boolean[] result = {false};
        node.accept(new ASTVisitor() {
            @Override
            public void endVisit(MarkerAnnotation markerAnnotation) {
                if (!result[0]) {
                    result[0] = "lombok.Generated".equals(
                        markerAnnotation.getTypeName().getFullyQualifiedName()
                    );
                    super.endVisit(markerAnnotation);
                }
            }
        });
        return result[0];
    }
}
```

## Migration Changes Summary

### 1. Package Declarations

**Option 1 (Recommended):**
```java
// FROM:
package org.eclipse.jdt.core.dom;

// TO:
package org.eclipse.jdt.internal.core.dom.javac;
```

**Option 2 (Alternative):**
```java
// TO:
package org.jboss.tools.javac.ls.server.compiler.dom.converter;
```

### 2. Import Changes

```java
// Add utility import (Option 1):
import org.eclipse.jdt.internal.core.dom.util.JavacDOMUtil;

// Keep existing Eclipse imports (we have the bundle):
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

// Replace logging:
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Keep javac imports (shaded):
import shaded.com.sun.tools.javac.tree.JCTree.*;
import shaded.com.sun.tools.javac.util.Context;
```

### 3. Code Changes

```java
// Replace ILog with SLF4J:
private static final Logger LOG = LoggerFactory.getLogger(JavacConverter.class);

// FROM:
ILog.get().error("message", ex);
// TO:
LOG.error("message", ex);

// Replace RecoveryScanner:
// FROM:
new String(RecoveryScanner.FAKE_IDENTIFIER)
// TO:
JavacDOMUtil.FAKE_IDENTIFIER_STRING

// Replace JavacUtils.findMatch:
// FROM:
JavacUtils.findMatch(content, text, start, end)
// TO:
JavacDOMUtil.findMatch(content, text, start, end)

// Replace DOMCodeSelector.isGenerated:
// FROM:
DOMCodeSelector.isGenerated(node)
// TO:
JavacDOMUtil.isGenerated(node)

// Replace scanner.complianceLevel:
// FROM:
this.ast.scanner.complianceLevel < ClassFileConstants.JDK1_5
// TO:
this.ast.apiLevel() < AST.JLS3  // JLS3 = Java 1.5

// FROM:
this.ast.scanner.complianceLevel < ClassFileConstants.JDK16
// TO:
this.ast.apiLevel() < AST.JLS16  // JLS16 = Java 16

// ClassFileConstants.AccDeprecated - no change needed!
javadoc.getParent().setFlags(
    javadoc.getParent().getFlags() | ClassFileConstants.AccDeprecated
);
```

## Recommendation

✅ **Use Option 1: Internal Javac Package**

**Reasons:**
1. Converters work with Eclipse DOM classes → naturally belong in dom bundle
2. Follows existing Eclipse package conventions (org.eclipse.jdt.internal.*)
3. ClassFileConstants already available in same bundle
4. Keeps DOM-related code together
5. Internal package → won't pollute public API
6. Separate `javac` subpackage clearly indicates javac-specific implementation

**Structure:**
```
org.jboss.tools.jdt.core.dom
└── org.eclipse.jdt.internal.core.dom.javac          # Converters
    └── javadoc                                       # Javadoc helpers
└── org.eclipse.jdt.internal.core.dom.util           # Utilities (JavacDOMUtil)
```

## Implementation Steps

1. ✅ Create `org.eclipse.jdt.internal.core.dom.util.JavacDOMUtil` class
   - Add FAKE_IDENTIFIER constants
   - Add findMatch() method (copy from JavacUtils)
   - Add isGenerated() method (copy from DOMCodeSelector)

2. ✅ Create `org.eclipse.jdt.internal.core.dom.javac.javadoc` package
   - Copy JavacJdtMarkupParser.java
   - Copy JavacJdtMarkupTag.java
   - Copy JavacJdtMarkupTagAttribute.java
   - Update package declarations

3. ✅ Create `org.eclipse.jdt.internal.core.dom.javac.JavadocConverter`
   - Copy from old project
   - Update package declaration
   - Update imports (ILog → SLF4J, utility methods)
   - Update javadoc helper package references

4. ✅ Create `org.eclipse.jdt.internal.core.dom.javac.JavacConverter`
   - Copy from old project
   - Update package declaration
   - Update imports
   - Replace ILog calls with SLF4J
   - Replace utility method calls
   - Replace scanner.complianceLevel with ast.apiLevel()

5. ✅ Write integration tests
   - Test JCTree → DOM conversion
   - Test various Java language features
   - Verify source position mapping

## Files Summary

**New Files:**
- `org.eclipse.jdt.internal.core.dom.util.JavacDOMUtil` (~100 lines)
- `org.eclipse.jdt.internal.core.dom.javac.JavacConverter` (3813 lines)
- `org.eclipse.jdt.internal.core.dom.javac.JavadocConverter` (1005 lines)
- `org.eclipse.jdt.internal.core.dom.javac.javadoc.JavacJdtMarkupParser` (116 lines)
- `org.eclipse.jdt.internal.core.dom.javac.javadoc.JavacJdtMarkupTag` (29 lines)
- `org.eclipse.jdt.internal.core.dom.javac.javadoc.JavacJdtMarkupTagAttribute` (29 lines)

**Total:** ~5100 lines across 6 files

**Existing Files (no changes):**
- `org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants` ✅
- `org.eclipse.jdt.internal.core.dom.util.DOMASTUtil` ✅
