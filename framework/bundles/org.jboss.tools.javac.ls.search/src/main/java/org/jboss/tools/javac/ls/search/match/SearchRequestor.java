package org.jboss.tools.javac.ls.search.match;

/**
 * Callback interface for receiving search results.
 *
 * Implementations of this interface are passed to the search engine and
 * receive notifications for each match found during the search.
 *
 * Example usage:
 * <pre>
 * SearchRequestor requestor = new SearchRequestor() {
 *     public void acceptMatch(SearchMatch match) {
 *         System.out.println("Found: " + match);
 *     }
 * };
 * searchEngine.search(pattern, scope, requestor);
 * </pre>
 */
@FunctionalInterface
public interface SearchRequestor {

    /**
     * Notification sent when a search match is found.
     *
     * @param match the search match that was found
     */
    void acceptMatch(SearchMatch match);
}
