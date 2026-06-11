package org.jboss.tools.javac.ls.search.pattern.test;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.tools.javac.ls.parser.bindings.JavacDOMParser;
import org.jboss.tools.javac.ls.search.pattern.FieldPattern;
import org.jboss.tools.javac.ls.search.pattern.FieldPattern.SearchFor;
import org.jboss.tools.javac.ls.search.pattern.SearchPattern.MatchRule;
import org.junit.Before;
import org.junit.Test;

import shaded.org.eclipse.jdt.core.dom.AST;
import shaded.org.eclipse.jdt.core.dom.CompilationUnit;
import shaded.org.eclipse.jdt.core.dom.FieldDeclaration;
import shaded.org.eclipse.jdt.core.dom.TypeDeclaration;
import shaded.org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class FieldPatternTest {

    private JavacDOMParser parser;
    private Path testFile;

    @Before
    public void setUp() {
        parser = new JavacDOMParser();
        testFile = Paths.get("/test/Example.java");
    }

    @Test
    public void testFieldDeclarationMatch() {
        String source = "public class MyClass { private int myField; }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);
        FieldDeclaration fieldDecl = typeDecl.getFields()[0];
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);

        FieldPattern pattern = new FieldPattern("myField", null, MatchRule.EXACT_MATCH, SearchFor.DECLARATIONS);
        assertTrue(pattern.matches(fragment));

        FieldPattern wrongPattern = new FieldPattern("otherField", null, MatchRule.EXACT_MATCH, SearchFor.DECLARATIONS);
        assertFalse(wrongPattern.matches(fragment));
    }

    @Test
    public void testPrefixMatch() {
        String source = "public class MyClass { private int myField; }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);
        FieldDeclaration fieldDecl = typeDecl.getFields()[0];
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);

        FieldPattern pattern = new FieldPattern("my", null, MatchRule.PREFIX_MATCH, SearchFor.DECLARATIONS);
        assertTrue(pattern.matches(fragment));
    }

    @Test
    public void testSearchForDeclarationsOnly() {
        String source = "public class MyClass { private int myField; }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);
        FieldDeclaration fieldDecl = typeDecl.getFields()[0];
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);

        FieldPattern declPattern = new FieldPattern("myField", null, MatchRule.EXACT_MATCH, SearchFor.DECLARATIONS);
        assertTrue(declPattern.matches(fragment));

        FieldPattern refPattern = new FieldPattern("myField", null, MatchRule.EXACT_MATCH, SearchFor.REFERENCES);
        assertFalse(refPattern.matches(fragment));
    }

    @Test
    public void testDefaultConstructor() {
        FieldPattern pattern = new FieldPattern("myField");
        assertEquals("myField", pattern.getSearchString());
        assertEquals(MatchRule.EXACT_MATCH, pattern.getMatchRule());
        assertEquals(SearchFor.ALL_OCCURRENCES, pattern.getSearchFor());
        assertNull(pattern.getDeclaringType());
    }

    @Test
    public void testWithDeclaringType() {
        FieldPattern pattern = new FieldPattern("myField", "MyClass", MatchRule.EXACT_MATCH, SearchFor.ALL_OCCURRENCES);
        assertEquals("MyClass", pattern.getDeclaringType());
    }

    @Test
    public void testToString() {
        FieldPattern pattern = new FieldPattern("myField", "MyClass", MatchRule.EXACT_MATCH, SearchFor.REFERENCES);
        String str = pattern.toString();
        assertTrue(str.contains("FieldPattern"));
        assertTrue(str.contains("myField"));
        assertTrue(str.contains("MyClass"));
        assertTrue(str.contains("EXACT_MATCH"));
        assertTrue(str.contains("REFERENCES"));
    }

    @Test
    public void testNullNode() {
        FieldPattern pattern = new FieldPattern("myField");
        assertFalse(pattern.matches(null));
    }
}
