#!/bin/bash
# Helper script to split the JDT bundle commit into original + modifications

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMP_DIR="/tmp/jdt-original-files"
JDT_SOURCE="/home/rob/code/work/ide/eclipse/jdt/eclipse.jdt.core/org.eclipse.jdt.core"

echo -e "${GREEN}JDT Commit Splitter${NC}"
echo "This will help split commit c91557a into two commits:"
echo "1. Original JDT files (unmodified)"
echo "2. Our modifications"
echo ""

# Step 1: Prepare original files
if [ "$1" == "prepare" ]; then
    echo -e "${YELLOW}Step 1: Preparing original JDT files...${NC}"

    rm -rf "$TEMP_DIR"
    mkdir -p "$TEMP_DIR"

    # Copy original DOM files
    echo "Copying original JDT DOM files..."
    cp -r "$JDT_SOURCE/dom/org" "$TEMP_DIR/"

    # Copy original compiler files we need
    echo "Copying original compiler support files..."
    mkdir -p "$TEMP_DIR/org/eclipse/jdt/core/compiler"
    mkdir -p "$TEMP_DIR/org/eclipse/jdt/internal/compiler/classfmt"
    mkdir -p "$TEMP_DIR/org/eclipse/jdt/internal/compiler/problem"
    mkdir -p "$TEMP_DIR/org/eclipse/jdt/internal/compiler/util"

    cp "$JDT_SOURCE/compiler/org/eclipse/jdt/core/compiler/IProblem.java" \
       "$TEMP_DIR/org/eclipse/jdt/core/compiler/" 2>/dev/null || true
    cp "$JDT_SOURCE/compiler/org/eclipse/jdt/core/compiler/CategorizedProblem.java" \
       "$TEMP_DIR/org/eclipse/jdt/core/compiler/" 2>/dev/null || true

    # Copy ClassFileConstants
    find /home/rob/code/work/ide/eclipse/jdt -name "ClassFileConstants.java" -path "*/compiler/classfmt/*" -exec cp {} "$TEMP_DIR/org/eclipse/jdt/internal/compiler/classfmt/" \; 2>/dev/null || true

    # Copy problem infrastructure
    find /home/rob/code/work/ide/eclipse/jdt -name "DefaultProblem.java" -path "*/compiler/problem/*" -exec cp {} "$TEMP_DIR/org/eclipse/jdt/internal/compiler/problem/" \; 2>/dev/null || true

    echo -e "${GREEN}Original files prepared in: $TEMP_DIR${NC}"
    echo ""
    echo -e "${YELLOW}Next steps:${NC}"
    echo "1. Run: git rebase -i 8a2583c"
    echo "2. Change 'pick' to 'edit' for commit c91557a"
    echo "3. Save and close the editor"
    echo "4. When rebase stops, run: ./split-commit-helper.sh apply"
    exit 0
fi

# Step 2: Apply the split
if [ "$1" == "apply" ]; then
    echo -e "${YELLOW}Step 2: Applying the split...${NC}"

    if [ ! -d "$TEMP_DIR" ]; then
        echo -e "${RED}Error: Original files not found. Run './split-commit-helper.sh prepare' first${NC}"
        exit 1
    fi

    echo "Resetting current commit..."
    git reset HEAD~1

    # Commit 1: Original files
    echo -e "${YELLOW}Creating commit 1: Original JDT files...${NC}"

    # Copy original files
    mkdir -p framework/bundles/org.jboss.tools.jdt.core.dom/src/main/java
    cp -r "$TEMP_DIR/org" framework/bundles/org.jboss.tools.jdt.core.dom/src/main/java/

    # Create basic MANIFEST.MF and pom.xml (these need to exist)
    mkdir -p framework/bundles/org.jboss.tools.jdt.core.dom/META-INF
    cat > framework/bundles/org.jboss.tools.jdt.core.dom/META-INF/MANIFEST.MF << 'EOFMANIFEST'
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: Eclipse JDT Core DOM (unmodified)
Bundle-SymbolicName: org.eclipse.jdt.core.dom
Automatic-Module-Name: org.eclipse.jdt.core.dom
Bundle-Version: 0.0.1.qualifier
Bundle-RequiredExecutionEnvironment: JavaSE-17
Export-Package: org.eclipse.jdt.core.dom
EOFMANIFEST

    cat > framework/bundles/org.jboss.tools.jdt.core.dom/pom.xml << 'EOFPOM'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.jboss.tools.javac.ls.framework</groupId>
		<artifactId>bundles</artifactId>
		<version>0.0.1-SNAPSHOT</version>
	</parent>
	<groupId>org.jboss.tools.javac.ls.framework.bundles</groupId>
	<artifactId>org.eclipse.jdt.core.dom</artifactId>
	<packaging>eclipse-plugin</packaging>

	<name>Eclipse JDT DOM</name>
</project>
EOFPOM

    # Add to parent pom
    git add framework/bundles/org.jboss.tools.jdt.core.dom/
    git add framework/bundles/pom.xml 2>/dev/null || true
    git add pom.xml 2>/dev/null || true
    git add targetplatform/ 2>/dev/null || true

    git commit -m "Copy original Eclipse JDT DOM files

Copied unmodified Eclipse JDT DOM API files from Eclipse JDT Core.
This commit contains the original files before any modifications.

Files copied from: Eclipse JDT Core (org.eclipse.jdt.core)
- All DOM API classes from org.eclipse.jdt.core.dom
- Supporting compiler interfaces: IProblem, CategorizedProblem
- ClassFileConstants, DefaultProblem
- Basic problem infrastructure

Next commit will apply modifications to remove compiler dependencies.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

    echo -e "${GREEN}Commit 1 created: Original JDT files${NC}"

    # Commit 2: Restore all our modifications
    echo -e "${YELLOW}Creating commit 2: Apply modifications...${NC}"

    # Stage all remaining changes (these are our modifications)
    git add -A

    git commit -m "Remove compiler dependencies and refactor JDT DOM bundle

Applied modifications to make JDT DOM bundle standalone without Eclipse
JDT compiler dependencies.

## Major Changes

### Removed Scanner Dependencies

**DefaultCommentMapper.java**:
- Replaced Scanner with direct char[] source scanning
- Added helper methods: isOnlyWhitespace(), countNewlines(), etc.
- Implemented custom getLineNumber() with binary search
- Changed initialize() signature from Scanner to char[] source

**SimpleName.java**:
- Replaced Scanner-based validation with Character.isJavaIdentifierStart/Part
- Simplified setIdentifier() from ~30 lines to 4 lines

**Javadoc.java, Statement.java, SimpleType.java**:
- Removed Scanner/InvalidInputException/TerminalToken imports
- Use DOMConstants helper methods instead

### Created Constants/Helper Classes

**JavaCoreConstants.java**:
- Java version constants (VERSION_1_1 through VERSION_26)
- Compiler option constants
- Replaces JavaCore dependency

**DOMConstants.java**:
- Validation methods: isValidJavaIdentifier(), isValidComment(), etc.
- String/character literal parsing
- TypeConstants arrays

**CompilationUnitResolverConstants.java**:
- Bit flag constants from CompilationUnitResolver
- IntArrayList helper class

### Stubbed/Minimal Implementations

**ASTParser.java**:
- Entire implementation commented out
- newParser() throws UnsupportedOperationException
- New entry point will be created later

**Problem Infrastructure**:
- Created minimal stubs: Messages.java, Util.java, ProblemSeverities.java
- Modified CategorizedProblem to use DOMConstants.NO_STRINGS

### Project Renaming

- Bundle: org.eclipse.jdt.core.dom → org.jboss.tools.jdt.core.dom
- Target: rsp-target → javacls-target
- Package names unchanged (remain org.eclipse.jdt.core.*)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

    echo -e "${GREEN}Commit 2 created: Modifications applied${NC}"
    echo ""
    echo -e "${YELLOW}Next step:${NC}"
    echo "Run: git rebase --continue"
    echo ""
    echo "After rebase completes, you may want to:"
    echo "  git log --oneline -5"
    echo "to verify the split commits look correct."

    exit 0
fi

# Default: show usage
echo "Usage:"
echo "  ./split-commit-helper.sh prepare   - Prepare original files"
echo "  ./split-commit-helper.sh apply     - Apply the split during rebase"
echo ""
echo "Full workflow:"
echo "1. ./split-commit-helper.sh prepare"
echo "2. git rebase -i 8a2583c"
echo "3. Change 'pick' to 'edit' for commit c91557a, save and exit"
echo "4. ./split-commit-helper.sh apply"
echo "5. git rebase --continue"
