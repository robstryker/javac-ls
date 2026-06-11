package org.jboss.tools.javac.ls.search.match.test;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.tools.javac.ls.search.match.SearchMatch;
import org.jboss.tools.javac.ls.search.match.SearchMatch.MatchKind;
import org.junit.Test;

public class SearchMatchTest {

    @Test
    public void testSearchMatchCreation() {
        Path file = Paths.get("/tmp/Test.java");
        SearchMatch match = new SearchMatch(file, 100, 10, MatchKind.TYPE_REFERENCE, "MyClass");

        assertEquals(file, match.getFile());
        assertEquals(100, match.getOffset());
        assertEquals(10, match.getLength());
        assertEquals(MatchKind.TYPE_REFERENCE, match.getKind());
        assertEquals("MyClass", match.getElementName());
    }

    @Test
    public void testSearchMatchEquality() {
        Path file = Paths.get("/tmp/Test.java");
        SearchMatch match1 = new SearchMatch(file, 100, 10, MatchKind.TYPE_REFERENCE, "MyClass");
        SearchMatch match2 = new SearchMatch(file, 100, 10, MatchKind.TYPE_REFERENCE, "MyClass");
        SearchMatch match3 = new SearchMatch(file, 200, 10, MatchKind.TYPE_REFERENCE, "MyClass");

        assertEquals(match1, match2);
        assertNotEquals(match1, match3);
        assertEquals(match1.hashCode(), match2.hashCode());
    }

    @Test
    public void testSearchMatchToString() {
        Path file = Paths.get("/tmp/Test.java");
        SearchMatch match = new SearchMatch(file, 100, 10, MatchKind.METHOD_DECLARATION, "myMethod");

        String str = match.toString();
        assertTrue(str.contains("Test.java"));
        assertTrue(str.contains("100"));
        assertTrue(str.contains("110"));
        assertTrue(str.contains("METHOD_DECLARATION"));
        assertTrue(str.contains("myMethod"));
    }

    @Test(expected = NullPointerException.class)
    public void testSearchMatchNullFile() {
        new SearchMatch(null, 100, 10, MatchKind.TYPE_REFERENCE, "MyClass");
    }

    @Test(expected = NullPointerException.class)
    public void testSearchMatchNullKind() {
        Path file = Paths.get("/tmp/Test.java");
        new SearchMatch(file, 100, 10, null, "MyClass");
    }

    @Test
    public void testSearchMatchAllMatchKinds() {
        Path file = Paths.get("/tmp/Test.java");

        for (MatchKind kind : MatchKind.values()) {
            SearchMatch match = new SearchMatch(file, 0, 5, kind, "element");
            assertEquals(kind, match.getKind());
        }
    }
}
