# JavacCompilationUnitResolver - Methods Worth Extracting

## Quick Answer

**The Difference:**
- **parse()** = AST structure only (no type info) - fast
- **resolve()** = AST + full type resolution (bindings) - slow but powerful
- **toCompilationUnit()** = workhorse that both call with `resolveBindings` flag

## Method Hierarchy

```
Public API Methods
├── parse(String[] paths, ...)           → line 532
├── parse(ICompilationUnit[], ...)       → line 483
├── resolve(String[] paths, ...)         → line 214
└── resolve(ICompilationUnit[], ...)     → line 300
         ↓
    All call:
         ↓
toCompilationUnit(sourceUnit, resolveBindings, ...) → line 593
         ↓
    Which calls:
         ↓
MAIN IMPLEMENTATION: parse(ICompilationUnit[], ...) → line 658
         ↓
    Uses:
         ↓
    Helper methods:
    - createAST() → line 1452
    - scanJavacCommentScanner() → line 1474
    - scanECJCommentScanner() → line 1501
    - addCommentsToUnit() → line 1517
    - markProblemNodesMalformed() → line 1015
    - depthFirstFixNodePositions() → line 1028
```

## Methods Worth Saving

### 🟢 HIGH VALUE - Extract These

#### 1. Main parse() Implementation (lines 658-864) ⭐⭐⭐
**Why:** This IS the resolver - all the javac integration happens here

**What it does:**
```java
private Map<ICompilationUnit, CompilationUnit> parse(
    ICompilationUnit[] sourceUnits,
    int apiLevel,
    Map<String, String> compilerOptions,
    boolean resolveBindings,  // ← key parameter
    int flags,
    IJavaProject javaProject,
    List<Classpath> extraClasspath,
    WorkingCopyOwner workingCopyOwner,
    int focalPoint,
    IProgressMonitor monitor
)
```

**Core logic (~200 lines of useful code):**
1. Create javac Context with caching optimizations
2. Configure javac Options from compiler options
3. Set up JavacFileManager with classpaths
4. Create JavaFileObjects from source units
5. Create JavacTask
6. **task.parse()** → get JCCompilationUnit
7. **JavacConverter.populateCompilationUnit()** → convert to DOM
8. Handle comments (javadoc + regular)
9. Fix node positions
10. If resolveBindings: **task.analyze()** + attach JavacBindingResolver

**Eclipse dependencies to remove:**
- IJavaProject → use simple classpath List<File>
- ICompilationUnit → use String source + String filename
- IProgressMonitor → remove or simple callback
- WorkingCopyOwner → skip (no working copies)

**Simplified signature for javac-ls:**
```java
public CompilationUnit parse(
    String sourceContent,
    String fileName,
    List<File> classpath,
    int apiLevel,
    Map<String, String> compilerOptions,
    boolean resolveBindings
)
```

---

#### 2. createAST() (lines 1452-1472) ⭐⭐⭐
**Why:** Shows correct AST initialization

```java
private AST createAST(
    Map<String, String> options, 
    int level, 
    Context context, 
    int flags
)
```

**What it does:**
- Creates AST with correct API level
- Sets preview features flag
- Configures scanner source/compliance levels
- Sets AST flags

**Worth extracting:** YES - needed for every parse

---

#### 3. scanJavacCommentScanner() (lines 1474-1499) ⭐⭐
**Why:** Extracts javadoc and regular comments

```java
private Scanner scanJavacCommentScanner(
    List<Comment> missingComments,
    CompilationUnit unit,
    Context context,
    String rawText,
    JavacConverter converter
)
```

**What it does:**
- Creates JavadocTokenizer to scan for comments
- Finds comments not already attached to nodes
- Converts javac comments to DOM Comments

**Worth extracting:** YES if we want javadoc support

---

#### 4. addCommentsToUnit() (lines 1517-1528) ⭐⭐
**Why:** Attaches comments to CompilationUnit

```java
static void addCommentsToUnit(
    Collection<Comment> comments, 
    CompilationUnit res
)
```

**What it does:**
- Merges comment lists
- Sorts by position
- Attaches to CompilationUnit

**Worth extracting:** YES - simple and useful

---

### 🟡 MEDIUM VALUE - Consider These

#### 5. depthFirstFixNodePositions() (lines 1028-1059) ⭐
**Why:** Fixes position inconsistencies after conversion

**What it does:**
- Walks DOM tree
- Fixes parent nodes that don't encompass children
- Collects javadoc comments

**Worth extracting:** Maybe - depends on JavacConverter quality

---

#### 6. removeRecoveredNodes() (lines 1061-1091) ⭐
**Why:** Cleans up error recovery nodes

**What it does:**
- Removes nodes marked as RECOVERED
- Removes empty variable declarations

**Worth extracting:** Maybe - only if we want clean AST without errors

---

#### 7. markProblemNodesMalformed() (lines 1015-1026) ⭐
**Why:** Marks nodes with errors as MALFORMED

**Worth extracting:** Maybe - for better error reporting

---

### 🔴 LOW VALUE - Skip These

#### 8. toCompilationUnit() (line 593) ❌
**Why:** Just a wrapper around main parse() with working copy handling

**Skip because:** We don't need working copy logic

---

#### 9. Public parse() and resolve() overloads ❌
**Why:** Just delegate to toCompilationUnit()

**Skip because:** We can create simpler API directly

---

#### 10. resolveRequestedBindingKeys() (line 383) ❌
**Why:** Resolves binding keys by string (for Eclipse refactoring tools)

**Skip because:** We don't need this Eclipse-specific feature

---

#### 11. Working copy methods (createMockUnit, etc.) ❌
**Skip because:** No working copy support needed

---

#### 12. Search/index integration (lines 1286-1405) ❌
**Skip because:** We'll use simple file scanning instead

---

## Recommended Extraction Plan

### Step 1: Core Parser (~150 lines)

Extract from **main parse() method (658-864)**:

```java
public class JavacDOMParser {
    
    public CompilationUnit parse(
        String sourceContent,
        String fileName,
        List<File> classpath,
        int apiLevel,
        Map<String, String> compilerOptions,
        boolean resolveBindings
    ) {
        // 1. Create Context (lines 669-696)
        Context context = new Context();
        Names names = new Names(context);
        context.put(Names.namesKey, names);
        CachingJarsJavaFileManager.preRegister(context);
        // ... etc
        
        // 2. Configure options (lines 699-700)
        JavacUtils.configureJavacContext(context, compilerOptions, null, false, false);
        Options javacOptions = Options.instance(context);
        javacOptions.put("allowStringFolding", Boolean.FALSE.toString());
        // ... etc
        
        // 3. Create file manager and set classpath (lines 702-712)
        JavacFileManager fileManager = (JavacFileManager)context.get(JavaFileManager.class);
        fileManager.setLocation(StandardLocation.CLASS_PATH, classpath);
        
        // 4. Create JavaFileObject from source (simplified from lines 714-724)
        JavaFileObject fileObject = new VirtualSourceFile(fileName, sourceContent);
        fileManager.cache(fileObject, CharBuffer.wrap(sourceContent));
        
        // 5. Create AST (call createAST helper)
        AST ast = createAST(compilerOptions, apiLevel, context, 0);
        CompilationUnit result = ast.newCompilationUnit();
        
        // 6. Create JavacTask (lines 740)
        JavacTask task = ((JavacTool)compiler).getTask(...);
        
        // 7. Parse (lines 754-769)
        JCCompilationUnit javacUnit = task.parse().iterator().next();
        
        // 8. Convert (lines 794-796)
        JavacConverter converter = new JavacConverter(
            ast, javacUnit, context, sourceContent, 
            docEnabled, -1
        );
        converter.populateCompilationUnit(result, javacUnit);
        
        // 9. Handle comments (call helpers)
        List<Comment> comments = new ArrayList<>();
        scanJavacCommentScanner(comments, result, context, sourceContent, converter);
        addCommentsToUnit(comments, result);
        
        // 10. Optionally resolve bindings (lines 848)
        if (resolveBindings) {
            task.analyze();
            JavacBindingResolver resolver = new JavacBindingResolver(...);
            ast.setBindingResolver(resolver);
        }
        
        return result;
    }
}
```

### Step 2: Add Helpers (~80 lines)

- `createAST()` - 20 lines
- `scanJavacCommentScanner()` - 30 lines  
- `addCommentsToUnit()` - 15 lines
- `VirtualSourceFile` inner class - 15 lines

**Total: ~230 lines for working parser**

---

## What We Get

With these ~230 lines extracted, we can:

**Without bindings (parse mode):**
- Get AST structure
- Navigate types, methods, fields
- Get source positions
- Parse javadoc

**With bindings (resolve mode):**
- Resolve type references → ITypeBinding
- Resolve method calls → IMethodBinding
- Resolve field accesses → IVariableBinding
- Type hierarchy queries
- Find all references

## Complexity Estimate

- **Extract core parse logic:** 2-3 hours
- **Extract helpers:** 1 hour
- **Remove Eclipse deps:** 1-2 hours
- **Test with examples:** 1-2 hours

**Total:** ~1 day of work for basic working parser

Then we can decide if we need JavacBindingResolver (for type queries).
