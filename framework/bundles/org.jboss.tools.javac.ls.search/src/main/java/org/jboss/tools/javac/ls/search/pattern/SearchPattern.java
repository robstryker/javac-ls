package org.jboss.tools.javac.ls.search.pattern;

import shaded.org.eclipse.jdt.core.dom.ASTNode;

/**
 * Abstract base class for all search patterns.
 *
 * A search pattern describes what to search for in the code (e.g., references to a type,
 * declarations of a method, etc.). Patterns are matched against AST nodes during search.
 */
public abstract class SearchPattern {

    public enum MatchRule {
        EXACT_MATCH,
        PREFIX_MATCH,
        PATTERN_MATCH,
        CAMELCASE_MATCH
    }

    protected final String searchString;
    protected final MatchRule matchRule;

    protected SearchPattern(String searchString, MatchRule matchRule) {
        this.searchString = searchString;
        this.matchRule = matchRule != null ? matchRule : MatchRule.EXACT_MATCH;
    }

    /**
     * Tests whether the given AST node matches this search pattern.
     *
     * @param node the AST node to test
     * @return true if the node matches this pattern, false otherwise
     */
    public abstract boolean matches(ASTNode node);

    /**
     * Returns the search string for this pattern.
     *
     * @return the search string
     */
    public String getSearchString() {
        return searchString;
    }

    /**
     * Returns the match rule for this pattern.
     *
     * @return the match rule
     */
    public MatchRule getMatchRule() {
        return matchRule;
    }

    /**
     * Matches a name against the search string using this pattern's match rule.
     *
     * @param name the name to match
     * @return true if the name matches, false otherwise
     */
    protected boolean matchesName(String name) {
        if (name == null || searchString == null) {
            return false;
        }

        switch (matchRule) {
            case EXACT_MATCH:
                return searchString.equals(name);
            case PREFIX_MATCH:
                return name.startsWith(searchString);
            case PATTERN_MATCH:
                return matchesPattern(name, searchString);
            case CAMELCASE_MATCH:
                return matchesCamelCase(name, searchString);
            default:
                return false;
        }
    }

    private boolean matchesPattern(String name, String pattern) {
        String regex = pattern.replace("*", ".*").replace("?", ".");
        return name.matches(regex);
    }

    private boolean matchesCamelCase(String name, String pattern) {
        if (pattern.isEmpty()) {
            return name.isEmpty();
        }

        int nameIndex = 0;
        int patternIndex = 0;

        while (nameIndex < name.length() && patternIndex < pattern.length()) {
            char nameChar = name.charAt(nameIndex);
            char patternChar = pattern.charAt(patternIndex);

            if (Character.isUpperCase(nameChar)) {
                if (Character.toLowerCase(nameChar) == Character.toLowerCase(patternChar)) {
                    patternIndex++;
                }
            }
            nameIndex++;
        }

        return patternIndex == pattern.length();
    }

    @Override
    public String toString() {
        return String.format("%s[searchString=%s, matchRule=%s]",
                getClass().getSimpleName(),
                searchString,
                matchRule);
    }
}
