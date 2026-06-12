package org.jboss.tools.javac.ls.search.pattern.test;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.tools.javac.ls.parser.bindings.JavacDOMParser;
import org.jboss.tools.javac.ls.search.pattern.MethodPattern;
import org.jboss.tools.javac.ls.search.pattern.MethodPattern.SearchFor;
import org.jboss.tools.javac.ls.search.pattern.SearchPattern.MatchRule;
import org.junit.Before;
import org.junit.Test;

import shaded.org.eclipse.jdt.core.dom.AST;
import shaded.org.eclipse.jdt.core.dom.CompilationUnit;
import shaded.org.eclipse.jdt.core.dom.MethodDeclaration;
import shaded.org.eclipse.jdt.core.dom.TypeDeclaration;

public class MethodPatternTest {

    private JavacDOMParser parser;
    private Path testFile;

    @Before
    public void setUp() {
        parser = new JavacDOMParser();
        testFile = Paths.get("/test/Example.java");
    }

    @Test
    public void testMethodDeclarationMatch() {
        String source = "public class MyClass { public void myMethod() { } }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);
        MethodDeclaration methodDecl = typeDecl.getMethods()[0];

        MethodPattern pattern = new MethodPattern("myMethod", null, MatchRule.EXACT_MATCH, SearchFor.DECLARATIONS);
        assertTrue(pattern.matches(methodDecl));

        MethodPattern wrongPattern = new MethodPattern("otherMethod", null, MatchRule.EXACT_MATCH, SearchFor.DECLARATIONS);
        assertFalse(wrongPattern.matches(methodDecl));
    }

    @Test
    public void testPrefixMatch() {
        String source = "public class MyClass { public void myMethod() { } }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);
        MethodDeclaration methodDecl = typeDecl.getMethods()[0];

        MethodPattern pattern = new MethodPattern("my", null, MatchRule.PREFIX_MATCH, SearchFor.DECLARATIONS);
        assertTrue(pattern.matches(methodDecl));
    }

    @Test
    public void testSearchForDeclarationsOnly() {
        String source = "public class MyClass { public void myMethod() { } }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);
        MethodDeclaration methodDecl = typeDecl.getMethods()[0];

        MethodPattern declPattern = new MethodPattern("myMethod", null, MatchRule.EXACT_MATCH, SearchFor.DECLARATIONS);
        assertTrue(declPattern.matches(methodDecl));

        MethodPattern refPattern = new MethodPattern("myMethod", null, MatchRule.EXACT_MATCH, SearchFor.REFERENCES);
        assertFalse(refPattern.matches(methodDecl));
    }

    @Test
    public void testDefaultConstructor() {
        MethodPattern pattern = new MethodPattern("myMethod");
        assertEquals("myMethod", pattern.getSearchString());
        assertEquals(MatchRule.EXACT_MATCH, pattern.getMatchRule());
        assertEquals(SearchFor.ALL_OCCURRENCES, pattern.getSearchFor());
        assertNull(pattern.getDeclaringType());
    }

    @Test
    public void testWithDeclaringType() {
        MethodPattern pattern = new MethodPattern("myMethod", "MyClass", MatchRule.EXACT_MATCH, SearchFor.ALL_OCCURRENCES);
        assertEquals("MyClass", pattern.getDeclaringType());
    }

    @Test
    public void testToString() {
        MethodPattern pattern = new MethodPattern("myMethod", "MyClass", MatchRule.EXACT_MATCH, SearchFor.REFERENCES);
        String str = pattern.toString();
        assertTrue(str.contains("MethodPattern"));
        assertTrue(str.contains("myMethod"));
        assertTrue(str.contains("MyClass"));
        assertTrue(str.contains("EXACT_MATCH"));
        assertTrue(str.contains("REFERENCES"));
    }

    @Test
    public void testNullNode() {
        MethodPattern pattern = new MethodPattern("myMethod");
        assertFalse(pattern.matches(null));
    }
}
