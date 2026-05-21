#!/bin/bash
# Simple script to split the JDT commit into original + modifications

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

BUNDLE_DIR="framework/bundles/org.jboss.tools.jdt.core.dom"
TEMP_BACKUP="/tmp/jdt-modified-backup"
JDT_REPO="/home/rob/code/work/ide/eclipse/jdt/eclipse.jdt.core/org.eclipse.jdt.core"

echo -e "${GREEN}Simple JDT Commit Splitter${NC}"
echo ""

# Step 1: Reset to the commit
echo -e "${YELLOW}Step 1: Resetting to commit c91557a${NC}"
git reset --hard c91557a

# Step 2: Soft reset to unstage
echo -e "${YELLOW}Step 2: Soft reset to unstage files${NC}"
git reset --soft HEAD~1

# Step 3: Backup the modified bundle
echo -e "${YELLOW}Step 3: Backing up modified bundle to $TEMP_BACKUP${NC}"
rm -rf "$TEMP_BACKUP"
cp -r "$BUNDLE_DIR" "$TEMP_BACKUP"

# Step 4: Replace with original JDT files
echo -e "${YELLOW}Step 4: Replacing with original JDT files${NC}"

# Find all Java files in our bundle and replace with originals from JDT
find "$BUNDLE_DIR/src/main/java" -name "*.java" -type f | while read -r file; do
    # Get the relative path from src/main/java
    rel_path="${file#$BUNDLE_DIR/src/main/java/}"

    # Try to find in JDT DOM first
    jdt_file="$JDT_REPO/dom/$rel_path"
    if [ -f "$jdt_file" ]; then
        echo "  Replacing: $rel_path (from dom)"
        cp "$jdt_file" "$file"
        continue
    fi

    # Try in compiler
    jdt_file="$JDT_REPO/compiler/$rel_path"
    if [ -f "$jdt_file" ]; then
        echo "  Replacing: $rel_path (from compiler)"
        cp "$jdt_file" "$file"
        continue
    fi

    # Try in model (for internal/core classes)
    jdt_file="$JDT_REPO/model/$rel_path"
    if [ -f "$jdt_file" ]; then
        echo "  Replacing: $rel_path (from model)"
        cp "$jdt_file" "$file"
        continue
    fi

    # File not found in JDT - it's one of our created files, remove it for now
    echo "  Removing (not in JDT): $rel_path"
    rm "$file"
done

# Clean up empty directories
find "$BUNDLE_DIR/src/main/java" -type d -empty -delete

# Step 5: Commit original files
echo -e "${YELLOW}Step 5: Committing original JDT files${NC}"
git add -A

git commit -m "Copy original Eclipse JDT DOM files (unmodified)

Copied unmodified Eclipse JDT DOM API files from Eclipse JDT Core.
This commit contains the original files before any modifications.

Files copied from:
- Eclipse JDT Core (org.eclipse.jdt.core)
- DOM API classes from org.eclipse.jdt.core.dom package
- Supporting compiler interfaces and classes

The next commit will apply our modifications to remove compiler
dependencies and make this bundle standalone.

Source: Eclipse JDT Core
Repository: /home/rob/code/work/ide/eclipse/jdt/eclipse.jdt.core

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

echo -e "${GREEN}✓ Commit 1 created: Original JDT files${NC}"

# Step 6: Restore modified files
echo -e "${YELLOW}Step 6: Restoring modified files${NC}"
rm -rf "$BUNDLE_DIR"
cp -r "$TEMP_BACKUP" "$BUNDLE_DIR"

# Step 7: Commit modifications
echo -e "${YELLOW}Step 7: Committing modifications${NC}"
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
- Problem infrastructure: Minimal stubs (Messages, Util, ProblemSeverities)
- Modified CategorizedProblem to use DOMConstants.NO_STRINGS

## Project Renaming

- Bundle: org.eclipse.jdt.core.dom → org.jboss.tools.jdt.core.dom
- Target platform: rsp-target → javacls-target
- Package names: unchanged (remain org.eclipse.jdt.core.*)

All changes enable compilation without JDT internal compiler classes.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

echo -e "${GREEN}✓ Commit 2 created: Modifications applied${NC}"

# Step 8: Cherry-pick the build scripts commit
echo -e "${YELLOW}Step 8: Cherry-picking build scripts commit${NC}"
git cherry-pick cc4d2ab

echo -e "${GREEN}✓ Commit 3 cherry-picked: Build scripts${NC}"
echo ""
echo -e "${GREEN}Done! The commit has been split.${NC}"
echo ""
echo "New commit structure:"
git log --oneline -4

echo ""
echo "Temp backup preserved at: $TEMP_BACKUP"
