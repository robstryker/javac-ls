package org.jboss.tools.javac.ls.search.match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shaded.org.eclipse.jdt.core.dom.ASTNode;

/**
 * A collection of AST nodes that match a search pattern.
 *
 * During AST traversal, the search visitor adds nodes to this set when they match
 * the search pattern. After traversal is complete, these nodes are converted to
 * SearchMatch objects with proper location information.
 *
 * This class is not thread-safe and is designed for single-threaded use during
 * a single search operation.
 */
public class MatchingNodeSet {

    /**
     * Maps an ASTNode to its match kind.
     * We store the kind here so the visitor can specify what type of match it found.
     */
    private final Map<ASTNode, SearchMatch.MatchKind> matches;

    public MatchingNodeSet() {
        this.matches = new HashMap<>();
    }

    /**
     * Adds a matching node to this set.
     *
     * @param node the AST node that matched the search pattern
     * @param kind the kind of match (e.g., TYPE_REFERENCE, METHOD_DECLARATION)
     */
    public void addMatch(ASTNode node, SearchMatch.MatchKind kind) {
        if (node != null && kind != null) {
            matches.put(node, kind);
        }
    }

    /**
     * Returns all matching nodes that have been added to this set.
     *
     * @return an unmodifiable list of matching AST nodes
     */
    public List<ASTNode> getMatchingNodes() {
        return Collections.unmodifiableList(new ArrayList<>(matches.keySet()));
    }

    /**
     * Returns the match kind for a given node.
     *
     * @param node the AST node
     * @return the match kind, or null if the node is not in this set
     */
    public SearchMatch.MatchKind getMatchKind(ASTNode node) {
        return matches.get(node);
    }

    /**
     * Checks if this set contains any matches.
     *
     * @return true if at least one match has been added, false otherwise
     */
    public boolean hasMatches() {
        return !matches.isEmpty();
    }

    /**
     * Returns the number of matches in this set.
     *
     * @return the number of matching nodes
     */
    public int size() {
        return matches.size();
    }

    /**
     * Removes all matches from this set.
     */
    public void clear() {
        matches.clear();
    }

    @Override
    public String toString() {
        return String.format("MatchingNodeSet[%d matches]", matches.size());
    }
}
