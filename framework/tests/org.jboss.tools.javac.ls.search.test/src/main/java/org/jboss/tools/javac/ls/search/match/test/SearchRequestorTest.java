package org.jboss.tools.javac.ls.search.match.test;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.javac.ls.search.match.SearchMatch;
import org.jboss.tools.javac.ls.search.match.SearchMatch.MatchKind;
import org.jboss.tools.javac.ls.search.match.SearchRequestor;
import org.junit.Test;

public class SearchRequestorTest {

    @Test
    public void testSearchRequestorCallback() {
        List<SearchMatch> collectedMatches = new ArrayList<>();
        SearchRequestor requestor = collectedMatches::add;

        Path file = Paths.get("/tmp/Test.java");
        SearchMatch match1 = new SearchMatch(file, 100, 10, MatchKind.TYPE_REFERENCE, "MyClass");
        SearchMatch match2 = new SearchMatch(file, 200, 15, MatchKind.METHOD_DECLARATION, "myMethod");

        requestor.acceptMatch(match1);
        requestor.acceptMatch(match2);

        assertEquals(2, collectedMatches.size());
        assertTrue(collectedMatches.contains(match1));
        assertTrue(collectedMatches.contains(match2));
    }

    @Test
    public void testSearchRequestorLambda() {
        List<SearchMatch> collectedMatches = new ArrayList<>();
        SearchRequestor requestor = match -> {
            if (match.getKind() == MatchKind.TYPE_REFERENCE) {
                collectedMatches.add(match);
            }
        };

        Path file = Paths.get("/tmp/Test.java");
        SearchMatch typeRef = new SearchMatch(file, 100, 10, MatchKind.TYPE_REFERENCE, "MyClass");
        SearchMatch methodRef = new SearchMatch(file, 200, 15, MatchKind.METHOD_REFERENCE, "myMethod");

        requestor.acceptMatch(typeRef);
        requestor.acceptMatch(methodRef);

        assertEquals(1, collectedMatches.size());
        assertTrue(collectedMatches.contains(typeRef));
        assertFalse(collectedMatches.contains(methodRef));
    }

    @Test
    public void testSearchRequestorAnonymousClass() {
        final int[] count = {0};

        SearchRequestor requestor = new SearchRequestor() {
            @Override
            public void acceptMatch(SearchMatch match) {
                count[0]++;
            }
        };

        Path file = Paths.get("/tmp/Test.java");
        SearchMatch match = new SearchMatch(file, 100, 10, MatchKind.TYPE_REFERENCE, "MyClass");

        requestor.acceptMatch(match);
        requestor.acceptMatch(match);
        requestor.acceptMatch(match);

        assertEquals(3, count[0]);
    }
}
