package org.jboss.tools.javac.ls.search.pattern.test;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.tools.javac.ls.parser.bindings.JavacDOMParser;
import org.jboss.tools.javac.ls.search.pattern.SearchPattern.MatchRule;
import org.jboss.tools.javac.ls.search.pattern.TypePattern;
import org.jboss.tools.javac.ls.search.pattern.TypePattern.SearchFor;
import org.junit.Before;
import org.junit.Test;

import shaded.org.eclipse.jdt.core.dom.AST;
import shaded.org.eclipse.jdt.core.dom.CompilationUnit;
import shaded.org.eclipse.jdt.core.dom.TypeDeclaration;

public class TypePatternTest {

    private JavacDOMParser parser;
    private Path testFile;

    @Before
    public void setUp() {
        parser = new JavacDOMParser();
        testFile = Paths.get("/test/Example.java");
    }

    @Test
    public void testExactMatchDeclaration() {
        String source = "public class MyClass { }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);

        TypePattern pattern = new TypePattern("MyClass", MatchRule.EXACT_MATCH, SearchFor.DECLARATIONS);
        assertTrue(pattern.matches(typeDecl));

        TypePattern wrongPattern = new TypePattern("OtherClass", MatchRule.EXACT_MATCH, SearchFor.DECLARATIONS);
        assertFalse(wrongPattern.matches(typeDecl));
    }

    @Test
    public void testPrefixMatch() {
        String source = "public class MyClass { }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);

        TypePattern pattern = new TypePattern("My", MatchRule.PREFIX_MATCH, SearchFor.DECLARATIONS);
        assertTrue(pattern.matches(typeDecl));

        TypePattern noMatchPattern = new TypePattern("Other", MatchRule.PREFIX_MATCH, SearchFor.DECLARATIONS);
        assertFalse(noMatchPattern.matches(typeDecl));
    }

    @Test
    public void testPatternMatch() {
        String source = "public class MyClass { }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);

        TypePattern pattern = new TypePattern("My*", MatchRule.PATTERN_MATCH, SearchFor.DECLARATIONS);
        assertTrue(pattern.matches(typeDecl));

        TypePattern wildcardPattern = new TypePattern("*Class", MatchRule.PATTERN_MATCH, SearchFor.DECLARATIONS);
        assertTrue(wildcardPattern.matches(typeDecl));
    }

    @Test
    public void testCamelCaseMatch() {
        String source = "public class MyLongClassName { }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);

        TypePattern pattern = new TypePattern("MLC", MatchRule.CAMELCASE_MATCH, SearchFor.DECLARATIONS);
        assertTrue(pattern.matches(typeDecl));

        TypePattern mlcnPattern = new TypePattern("MLCN", MatchRule.CAMELCASE_MATCH, SearchFor.DECLARATIONS);
        assertTrue(mlcnPattern.matches(typeDecl));
    }

    @Test
    public void testSearchForDeclarationsOnly() {
        String source = "public class MyClass { }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);

        TypePattern declPattern = new TypePattern("MyClass", MatchRule.EXACT_MATCH, SearchFor.DECLARATIONS);
        assertTrue(declPattern.matches(typeDecl));

        TypePattern refPattern = new TypePattern("MyClass", MatchRule.EXACT_MATCH, SearchFor.REFERENCES);
        assertFalse(refPattern.matches(typeDecl));
    }

    @Test
    public void testDefaultConstructor() {
        TypePattern pattern = new TypePattern("MyClass");
        assertEquals("MyClass", pattern.getSearchString());
        assertEquals(MatchRule.EXACT_MATCH, pattern.getMatchRule());
        assertEquals(SearchFor.ALL_OCCURRENCES, pattern.getSearchFor());
    }

    @Test
    public void testToString() {
        TypePattern pattern = new TypePattern("MyClass", MatchRule.EXACT_MATCH, SearchFor.REFERENCES);
        String str = pattern.toString();
        assertTrue(str.contains("TypePattern"));
        assertTrue(str.contains("MyClass"));
        assertTrue(str.contains("EXACT_MATCH"));
        assertTrue(str.contains("REFERENCES"));
    }

    @Test
    public void testNullNode() {
        TypePattern pattern = new TypePattern("MyClass");
        assertFalse(pattern.matches(null));
    }
}
