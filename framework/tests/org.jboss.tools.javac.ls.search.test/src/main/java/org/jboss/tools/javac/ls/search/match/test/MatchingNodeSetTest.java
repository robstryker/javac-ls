package org.jboss.tools.javac.ls.search.match.test;

import static org.junit.Assert.*;

import java.util.List;

import org.jboss.tools.javac.ls.search.match.MatchingNodeSet;
import org.jboss.tools.javac.ls.search.match.SearchMatch.MatchKind;
import org.junit.Before;
import org.junit.Test;

import shaded.org.eclipse.jdt.core.dom.AST;
import shaded.org.eclipse.jdt.core.dom.ASTNode;
import shaded.org.eclipse.jdt.core.dom.SimpleName;

public class MatchingNodeSetTest {

    private MatchingNodeSet matchingNodeSet;
    private AST ast;

    @Before
    public void setUp() {
        matchingNodeSet = new MatchingNodeSet();
        ast = AST.newAST(AST.getJLSLatest(), false);
    }

    @Test
    public void testEmptySet() {
        assertFalse(matchingNodeSet.hasMatches());
        assertEquals(0, matchingNodeSet.size());
        assertTrue(matchingNodeSet.getMatchingNodes().isEmpty());
    }

    @Test
    public void testAddSingleMatch() {
        SimpleName node = ast.newSimpleName("test");
        matchingNodeSet.addMatch(node, MatchKind.TYPE_REFERENCE);

        assertTrue(matchingNodeSet.hasMatches());
        assertEquals(1, matchingNodeSet.size());
        assertEquals(MatchKind.TYPE_REFERENCE, matchingNodeSet.getMatchKind(node));
    }

    @Test
    public void testAddMultipleMatches() {
        SimpleName node1 = ast.newSimpleName("test1");
        SimpleName node2 = ast.newSimpleName("test2");
        SimpleName node3 = ast.newSimpleName("test3");

        matchingNodeSet.addMatch(node1, MatchKind.TYPE_REFERENCE);
        matchingNodeSet.addMatch(node2, MatchKind.METHOD_DECLARATION);
        matchingNodeSet.addMatch(node3, MatchKind.FIELD_REFERENCE);

        assertEquals(3, matchingNodeSet.size());
        assertEquals(MatchKind.TYPE_REFERENCE, matchingNodeSet.getMatchKind(node1));
        assertEquals(MatchKind.METHOD_DECLARATION, matchingNodeSet.getMatchKind(node2));
        assertEquals(MatchKind.FIELD_REFERENCE, matchingNodeSet.getMatchKind(node3));
    }

    @Test
    public void testGetMatchingNodes() {
        SimpleName node1 = ast.newSimpleName("test1");
        SimpleName node2 = ast.newSimpleName("test2");

        matchingNodeSet.addMatch(node1, MatchKind.TYPE_REFERENCE);
        matchingNodeSet.addMatch(node2, MatchKind.METHOD_DECLARATION);

        List<ASTNode> nodes = matchingNodeSet.getMatchingNodes();
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(node1));
        assertTrue(nodes.contains(node2));
    }

    @Test
    public void testGetMatchingNodesReturnsUnmodifiableList() {
        SimpleName node = ast.newSimpleName("test");
        matchingNodeSet.addMatch(node, MatchKind.TYPE_REFERENCE);

        List<ASTNode> nodes = matchingNodeSet.getMatchingNodes();
        try {
            nodes.add(ast.newSimpleName("should_fail"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testGetMatchKindForNonExistentNode() {
        SimpleName node = ast.newSimpleName("test");
        assertNull(matchingNodeSet.getMatchKind(node));
    }

    @Test
    public void testClear() {
        SimpleName node1 = ast.newSimpleName("test1");
        SimpleName node2 = ast.newSimpleName("test2");

        matchingNodeSet.addMatch(node1, MatchKind.TYPE_REFERENCE);
        matchingNodeSet.addMatch(node2, MatchKind.METHOD_DECLARATION);

        assertEquals(2, matchingNodeSet.size());
        matchingNodeSet.clear();

        assertFalse(matchingNodeSet.hasMatches());
        assertEquals(0, matchingNodeSet.size());
        assertTrue(matchingNodeSet.getMatchingNodes().isEmpty());
    }

    @Test
    public void testAddNullNode() {
        matchingNodeSet.addMatch(null, MatchKind.TYPE_REFERENCE);
        assertFalse(matchingNodeSet.hasMatches());
        assertEquals(0, matchingNodeSet.size());
    }

    @Test
    public void testAddNullKind() {
        SimpleName node = ast.newSimpleName("test");
        matchingNodeSet.addMatch(node, null);
        assertFalse(matchingNodeSet.hasMatches());
        assertEquals(0, matchingNodeSet.size());
    }

    @Test
    public void testToString() {
        SimpleName node1 = ast.newSimpleName("test1");
        SimpleName node2 = ast.newSimpleName("test2");

        matchingNodeSet.addMatch(node1, MatchKind.TYPE_REFERENCE);
        matchingNodeSet.addMatch(node2, MatchKind.METHOD_DECLARATION);

        String str = matchingNodeSet.toString();
        assertTrue(str.contains("MatchingNodeSet"));
        assertTrue(str.contains("2"));
    }

    @Test
    public void testAddSameNodeTwiceWithDifferentKinds() {
        SimpleName node = ast.newSimpleName("test");

        matchingNodeSet.addMatch(node, MatchKind.TYPE_REFERENCE);
        matchingNodeSet.addMatch(node, MatchKind.TYPE_DECLARATION);

        assertEquals(1, matchingNodeSet.size());
        assertEquals(MatchKind.TYPE_DECLARATION, matchingNodeSet.getMatchKind(node));
    }
}
