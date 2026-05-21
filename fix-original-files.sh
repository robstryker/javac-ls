#!/bin/bash
# Fix: Move original JDT files from modifications commit to original commit

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

JDT_REPO="/home/rob/code/work/ide/eclipse/jdt/eclipse.jdt.core/org.eclipse.jdt.core"
BUNDLE_DIR="framework/bundles/org.jboss.tools.jdt.core.dom/src/main/java"

echo -e "${GREEN}Fixing original JDT files placement${NC}"
echo ""
echo "These files should be in commit 1 (original), not commit 2 (modifications):"
echo "  - IProblem.java"
echo "  - CategorizedProblem.java"
echo "  - ClassFileConstants.java"
echo "  - DefaultProblem.java"
echo "  - ProblemReasons.java"
echo ""

# Step 1: Reset to before our commits
echo -e "${YELLOW}Step 1: Resetting to base commit${NC}"
git reset --hard 8a2583c

# Step 2: Get the original commit content
echo -e "${YELLOW}Step 2: Checking out original commit files${NC}"
git checkout c9d7b9d -- framework/bundles/org.jboss.tools.jdt.core.dom
git checkout c9d7b9d -- framework/bundles/pom.xml
git checkout c9d7b9d -- pom.xml
git checkout c9d7b9d -- targetplatform

# Step 3: Now add the missing original JDT files
echo -e "${YELLOW}Step 3: Adding original JDT files that were missing${NC}"

ORIGINAL_FILES=(
    "org/eclipse/jdt/core/compiler/IProblem.java"
    "org/eclipse/jdt/core/compiler/CategorizedProblem.java"
    "org/eclipse/jdt/core/compiler/ProblemReasons.java"
    "org/eclipse/jdt/internal/compiler/classfmt/ClassFileConstants.java"
    "org/eclipse/jdt/internal/compiler/problem/DefaultProblem.java"
)

for file in "${ORIGINAL_FILES[@]}"; do
    jdt_file="$JDT_REPO/compiler/$file"
    target_file="$BUNDLE_DIR/$file"

    if [ -f "$jdt_file" ]; then
        echo "  Copying: $file"
        mkdir -p "$(dirname "$target_file")"
        cp "$jdt_file" "$target_file"
    else
        echo -e "${RED}  Warning: $jdt_file not found${NC}"
    fi
done

# Step 4: Commit original files
echo -e "${YELLOW}Step 4: Committing original JDT files${NC}"
git add -A

git commit -m "Copy original Eclipse JDT DOM files (unmodified)

Copied unmodified Eclipse JDT DOM API files from Eclipse JDT Core.
This commit contains the original files before any modifications.

Files copied from:
- Eclipse JDT Core (org.eclipse.jdt.core)
- DOM API classes from org.eclipse.jdt.core.dom package
- Supporting compiler interfaces and classes
  - IProblem.java
  - CategorizedProblem.java
  - ProblemReasons.java
  - ClassFileConstants.java
  - DefaultProblem.java

The next commit will apply our modifications to remove compiler
dependencies and make this bundle standalone.

Source: Eclipse JDT Core
Repository: /home/rob/code/work/ide/eclipse/jdt/eclipse.jdt.core

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

echo -e "${GREEN}✓ Commit 1 created with all original files${NC}"

# Step 5: Now restore our modified files
echo -e "${YELLOW}Step 5: Checking out modified files${NC}"
git checkout 9aa3917 -- .

# Step 6: Commit modifications
echo -e "${YELLOW}Step 6: Committing modifications${NC}"
git add -A

git commit -m "Remove compiler dependencies and refactor JDT DOM bundle

Applied modifications to make JDT DOM bundle standalone without Eclipse
JDT compiler dependencies.

## Removed Scanner Dependencies

- DefaultCommentMapper: Replaced Scanner with char[] scanning, custom helpers
- SimpleName: Use Character.isJavaIdentifierStart/Part instead of Scanner
- Javadoc, Statement, SimpleType: Removed Scanner/compiler imports
- CompilationUnit: Updated initCommentMapper() to accept char[] source

## Created Helper Classes

- JavaCoreConstants: Version constants, compiler options (replaces JavaCore)
- DOMConstants: Validation methods, literal parsing, type constants
- CompilationUnitResolverConstants: Bit flags, IntArrayList

## Stubbed/Minimal Implementations

- ASTParser: Entire implementation commented out, throws UnsupportedOperationException
- Problem infrastructure: Minimal stubs (Messages, Util, ProblemSeverities, ProblemReporter)
- Modified CategorizedProblem to use DOMConstants.NO_STRINGS (instead of CharOperation)
- Modified ClassFileConstants to use literal values (instead of ASTNode.Bit18/Bit21)

## Project Renaming

- Bundle: org.eclipse.jdt.core.dom → org.jboss.tools.jdt.core.dom
- Target platform: rsp-target → javacls-target
- Package names: unchanged (remain org.eclipse.jdt.core.*)

All changes enable compilation without JDT internal compiler classes.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

echo -e "${GREEN}✓ Commit 2 created with modifications${NC}"

# Step 7: Cherry-pick build scripts
echo -e "${YELLOW}Step 7: Cherry-picking build scripts commit${NC}"
git cherry-pick 95df270

echo -e "${GREEN}✓ All commits recreated successfully!${NC}"
echo ""
echo "Final commit structure:"
git log --oneline -5
echo ""
echo -e "${YELLOW}Verify the fix:${NC}"
echo "  git show --stat HEAD~2  # Should show IProblem, CategorizedProblem, etc."
echo "  git show --stat HEAD~1  # Should show only our modifications"
