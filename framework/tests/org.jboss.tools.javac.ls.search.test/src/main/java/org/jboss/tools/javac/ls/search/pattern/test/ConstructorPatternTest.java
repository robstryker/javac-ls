package org.jboss.tools.javac.ls.search.pattern.test;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.tools.javac.ls.parser.bindings.JavacDOMParser;
import org.jboss.tools.javac.ls.search.pattern.ConstructorPattern;
import org.jboss.tools.javac.ls.search.pattern.ConstructorPattern.SearchFor;
import org.jboss.tools.javac.ls.search.pattern.SearchPattern.MatchRule;
import org.junit.Before;
import org.junit.Test;

import shaded.org.eclipse.jdt.core.dom.AST;
import shaded.org.eclipse.jdt.core.dom.CompilationUnit;
import shaded.org.eclipse.jdt.core.dom.MethodDeclaration;
import shaded.org.eclipse.jdt.core.dom.TypeDeclaration;

public class ConstructorPatternTest {

    private JavacDOMParser parser;
    private Path testFile;

    @Before
    public void setUp() {
        parser = new JavacDOMParser();
        testFile = Paths.get("/test/Example.java");
    }

    @Test
    public void testConstructorDeclarationMatch() {
        String source = "public class MyClass { public MyClass() { } }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);
        MethodDeclaration constructor = typeDecl.getMethods()[0];

        ConstructorPattern pattern = new ConstructorPattern("MyClass", MatchRule.EXACT_MATCH, SearchFor.DECLARATIONS);
        assertTrue(pattern.matches(constructor));

        ConstructorPattern wrongPattern = new ConstructorPattern("OtherClass", MatchRule.EXACT_MATCH, SearchFor.DECLARATIONS);
        assertFalse(wrongPattern.matches(constructor));
    }

    @Test
    public void testPrefixMatch() {
        String source = "public class MyClass { public MyClass() { } }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);
        MethodDeclaration constructor = typeDecl.getMethods()[0];

        ConstructorPattern pattern = new ConstructorPattern("My", MatchRule.PREFIX_MATCH, SearchFor.DECLARATIONS);
        assertTrue(pattern.matches(constructor));
    }

    @Test
    public void testNonConstructorMethodDoesNotMatch() {
        String source = "public class MyClass { public void myMethod() { } }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);
        MethodDeclaration method = typeDecl.getMethods()[0];

        ConstructorPattern pattern = new ConstructorPattern("MyClass", MatchRule.EXACT_MATCH, SearchFor.DECLARATIONS);
        assertFalse(pattern.matches(method));
    }

    @Test
    public void testSearchForDeclarationsOnly() {
        String source = "public class MyClass { public MyClass() { } }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);
        MethodDeclaration constructor = typeDecl.getMethods()[0];

        ConstructorPattern declPattern = new ConstructorPattern("MyClass", MatchRule.EXACT_MATCH, SearchFor.DECLARATIONS);
        assertTrue(declPattern.matches(constructor));

        ConstructorPattern refPattern = new ConstructorPattern("MyClass", MatchRule.EXACT_MATCH, SearchFor.REFERENCES);
        assertFalse(refPattern.matches(constructor));
    }

    @Test
    public void testDefaultConstructor() {
        ConstructorPattern pattern = new ConstructorPattern("MyClass");
        assertEquals("MyClass", pattern.getSearchString());
        assertEquals(MatchRule.EXACT_MATCH, pattern.getMatchRule());
        assertEquals(SearchFor.ALL_OCCURRENCES, pattern.getSearchFor());
    }

    @Test
    public void testToString() {
        ConstructorPattern pattern = new ConstructorPattern("MyClass", MatchRule.EXACT_MATCH, SearchFor.REFERENCES);
        String str = pattern.toString();
        assertTrue(str.contains("ConstructorPattern"));
        assertTrue(str.contains("MyClass"));
        assertTrue(str.contains("EXACT_MATCH"));
        assertTrue(str.contains("REFERENCES"));
    }

    @Test
    public void testNullNode() {
        ConstructorPattern pattern = new ConstructorPattern("MyClass");
        assertFalse(pattern.matches(null));
    }
}
