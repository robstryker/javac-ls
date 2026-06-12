package org.jboss.tools.javac.ls.search.visitor.test;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.tools.javac.ls.parser.bindings.JavacDOMParser;
import org.jboss.tools.javac.ls.search.match.MatchingNodeSet;
import org.jboss.tools.javac.ls.search.match.SearchMatch.MatchKind;
import org.jboss.tools.javac.ls.search.pattern.ConstructorPattern;
import org.jboss.tools.javac.ls.search.pattern.ConstructorPattern.SearchFor;
import org.jboss.tools.javac.ls.search.pattern.FieldPattern;
import org.jboss.tools.javac.ls.search.pattern.MethodPattern;
import org.jboss.tools.javac.ls.search.pattern.TypePattern;
import org.jboss.tools.javac.ls.search.visitor.DOMSearchVisitor;
import org.junit.Before;
import org.junit.Test;

import shaded.org.eclipse.jdt.core.dom.AST;
import shaded.org.eclipse.jdt.core.dom.ASTNode;
import shaded.org.eclipse.jdt.core.dom.CompilationUnit;

public class DOMSearchVisitorTest {

    private JavacDOMParser parser;
    private Path testFile;

    @Before
    public void setUp() {
        parser = new JavacDOMParser();
        testFile = Paths.get("/test/Example.java");
    }

    @Test
    public void testFindTypeDeclaration() {
        String source = "public class MyClass { }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);

        TypePattern pattern = new TypePattern("MyClass", null, TypePattern.SearchFor.DECLARATIONS);
        MatchingNodeSet matchingNodes = new MatchingNodeSet();
        DOMSearchVisitor visitor = new DOMSearchVisitor(pattern, matchingNodes);

        cu.accept(visitor);

        assertTrue(matchingNodes.hasMatches());
        assertEquals(1, matchingNodes.size());
        ASTNode match = matchingNodes.getMatchingNodes().get(0);
        assertEquals(MatchKind.TYPE_DECLARATION, matchingNodes.getMatchKind(match));
    }

    @Test
    public void testFindTypeReference() {
        String source = "public class MyClass { private String field; }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);

        TypePattern pattern = new TypePattern("String", null, TypePattern.SearchFor.REFERENCES);
        MatchingNodeSet matchingNodes = new MatchingNodeSet();
        DOMSearchVisitor visitor = new DOMSearchVisitor(pattern, matchingNodes);

        cu.accept(visitor);

        assertTrue(matchingNodes.hasMatches());
        assertEquals(1, matchingNodes.size());
        ASTNode match = matchingNodes.getMatchingNodes().get(0);
        assertEquals(MatchKind.TYPE_REFERENCE, matchingNodes.getMatchKind(match));
    }

    @Test
    public void testFindMethodDeclaration() {
        String source = "public class MyClass { public void myMethod() { } }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);

        MethodPattern pattern = new MethodPattern("myMethod", null, null, MethodPattern.SearchFor.DECLARATIONS);
        MatchingNodeSet matchingNodes = new MatchingNodeSet();
        DOMSearchVisitor visitor = new DOMSearchVisitor(pattern, matchingNodes);

        cu.accept(visitor);

        assertTrue(matchingNodes.hasMatches());
        assertEquals(1, matchingNodes.size());
        ASTNode match = matchingNodes.getMatchingNodes().get(0);
        assertEquals(MatchKind.METHOD_DECLARATION, matchingNodes.getMatchKind(match));
    }

    @Test
    public void testFindMethodInvocation() {
        String source = "public class MyClass { public void myMethod() { } public void caller() { myMethod(); } }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);

        MethodPattern pattern = new MethodPattern("myMethod", null, null, MethodPattern.SearchFor.REFERENCES);
        MatchingNodeSet matchingNodes = new MatchingNodeSet();
        DOMSearchVisitor visitor = new DOMSearchVisitor(pattern, matchingNodes);

        cu.accept(visitor);

        assertTrue(matchingNodes.hasMatches());
        assertEquals(1, matchingNodes.size());
        ASTNode match = matchingNodes.getMatchingNodes().get(0);
        assertEquals(MatchKind.METHOD_REFERENCE, matchingNodes.getMatchKind(match));
    }

    @Test
    public void testFindFieldDeclaration() {
        String source = "public class MyClass { private int myField; }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);

        FieldPattern pattern = new FieldPattern("myField", null, null, FieldPattern.SearchFor.DECLARATIONS);
        MatchingNodeSet matchingNodes = new MatchingNodeSet();
        DOMSearchVisitor visitor = new DOMSearchVisitor(pattern, matchingNodes);

        cu.accept(visitor);

        assertTrue(matchingNodes.hasMatches());
        assertEquals(1, matchingNodes.size());
        ASTNode match = matchingNodes.getMatchingNodes().get(0);
        assertEquals(MatchKind.FIELD_DECLARATION, matchingNodes.getMatchKind(match));
    }

    @Test
    public void testFindFieldReference() {
        String source = "public class MyClass { private int myField; public int getField() { return this.myField; } }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);

        FieldPattern pattern = new FieldPattern("myField", null, null, FieldPattern.SearchFor.REFERENCES);
        MatchingNodeSet matchingNodes = new MatchingNodeSet();
        DOMSearchVisitor visitor = new DOMSearchVisitor(pattern, matchingNodes);

        cu.accept(visitor);

        assertTrue(matchingNodes.hasMatches());
        assertEquals(1, matchingNodes.size());
        ASTNode match = matchingNodes.getMatchingNodes().get(0);
        assertEquals(MatchKind.FIELD_REFERENCE, matchingNodes.getMatchKind(match));
    }

    @Test
    public void testFindConstructorDeclaration() {
        String source = "public class MyClass { public MyClass() { } }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);

        ConstructorPattern pattern = new ConstructorPattern("MyClass", null, SearchFor.DECLARATIONS);
        MatchingNodeSet matchingNodes = new MatchingNodeSet();
        DOMSearchVisitor visitor = new DOMSearchVisitor(pattern, matchingNodes);

        cu.accept(visitor);

        assertTrue(matchingNodes.hasMatches());
        assertEquals(1, matchingNodes.size());
        ASTNode match = matchingNodes.getMatchingNodes().get(0);
        assertEquals(MatchKind.CONSTRUCTOR_DECLARATION, matchingNodes.getMatchKind(match));
    }

    @Test
    public void testFindConstructorReference() {
        String source = "public class MyClass { public MyClass() { } public static MyClass create() { return new MyClass(); } }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);

        ConstructorPattern pattern = new ConstructorPattern("MyClass", null, SearchFor.REFERENCES);
        MatchingNodeSet matchingNodes = new MatchingNodeSet();
        DOMSearchVisitor visitor = new DOMSearchVisitor(pattern, matchingNodes);

        cu.accept(visitor);

        assertTrue(matchingNodes.hasMatches());
        assertEquals(1, matchingNodes.size());
        ASTNode match = matchingNodes.getMatchingNodes().get(0);
        assertEquals(MatchKind.CONSTRUCTOR_REFERENCE, matchingNodes.getMatchKind(match));
    }

    @Test
    public void testFindAllOccurrences() {
        String source = "public class MyClass { public void myMethod() { } public void caller() { myMethod(); } }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);

        MethodPattern pattern = new MethodPattern("myMethod", null, null, MethodPattern.SearchFor.ALL_OCCURRENCES);
        MatchingNodeSet matchingNodes = new MatchingNodeSet();
        DOMSearchVisitor visitor = new DOMSearchVisitor(pattern, matchingNodes);

        cu.accept(visitor);

        assertTrue(matchingNodes.hasMatches());
        assertEquals(2, matchingNodes.size());
    }

    @Test
    public void testNoMatches() {
        String source = "public class MyClass { }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);

        TypePattern pattern = new TypePattern("OtherClass", null, TypePattern.SearchFor.DECLARATIONS);
        MatchingNodeSet matchingNodes = new MatchingNodeSet();
        DOMSearchVisitor visitor = new DOMSearchVisitor(pattern, matchingNodes);

        cu.accept(visitor);

        assertFalse(matchingNodes.hasMatches());
        assertEquals(0, matchingNodes.size());
    }

    @Test
    public void testMultipleFieldsInSameDeclaration() {
        String source = "public class MyClass { private int field1, field2, field3; }";
        CompilationUnit cu = parser.parse(source, testFile.toString(), null, AST.JLS21, null, false);

        FieldPattern pattern = new FieldPattern("field2", null, null, FieldPattern.SearchFor.DECLARATIONS);
        MatchingNodeSet matchingNodes = new MatchingNodeSet();
        DOMSearchVisitor visitor = new DOMSearchVisitor(pattern, matchingNodes);

        cu.accept(visitor);

        assertTrue(matchingNodes.hasMatches());
        assertEquals(1, matchingNodes.size());
        ASTNode match = matchingNodes.getMatchingNodes().get(0);
        assertEquals(MatchKind.FIELD_DECLARATION, matchingNodes.getMatchKind(match));
    }
}
