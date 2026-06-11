package org.jboss.tools.javac.ls.search.pattern;

import shaded.org.eclipse.jdt.core.dom.ASTNode;
import shaded.org.eclipse.jdt.core.dom.IMethodBinding;
import shaded.org.eclipse.jdt.core.dom.MethodDeclaration;
import shaded.org.eclipse.jdt.core.dom.MethodInvocation;
import shaded.org.eclipse.jdt.core.dom.SimpleName;

/**
 * Search pattern for finding method references or declarations.
 */
public class MethodPattern extends SearchPattern {

    public enum SearchFor {
        REFERENCES,
        DECLARATIONS,
        ALL_OCCURRENCES
    }

    private final SearchFor searchFor;
    private final String declaringType;

    public MethodPattern(String methodName, String declaringType, MatchRule matchRule, SearchFor searchFor) {
        super(methodName, matchRule);
        this.declaringType = declaringType;
        this.searchFor = searchFor != null ? searchFor : SearchFor.ALL_OCCURRENCES;
    }

    public MethodPattern(String methodName) {
        this(methodName, null, MatchRule.EXACT_MATCH, SearchFor.ALL_OCCURRENCES);
    }

    @Override
    public boolean matches(ASTNode node) {
        if (node == null) {
            return false;
        }

        switch (searchFor) {
            case REFERENCES:
                return matchesReference(node);
            case DECLARATIONS:
                return matchesDeclaration(node);
            case ALL_OCCURRENCES:
                return matchesReference(node) || matchesDeclaration(node);
            default:
                return false;
        }
    }

    private boolean matchesReference(ASTNode node) {
        if (node instanceof MethodInvocation) {
            MethodInvocation invocation = (MethodInvocation) node;
            SimpleName methodName = invocation.getName();

            if (!matchesName(methodName.getIdentifier())) {
                return false;
            }

            if (declaringType != null) {
                IMethodBinding binding = invocation.resolveMethodBinding();
                if (binding != null && binding.getDeclaringClass() != null) {
                    String actualDeclaringType = binding.getDeclaringClass().getName();
                    return declaringType.equals(actualDeclaringType);
                }
            }

            return true;
        }

        return false;
    }

    private boolean matchesDeclaration(ASTNode node) {
        if (node instanceof MethodDeclaration) {
            MethodDeclaration methodDecl = (MethodDeclaration) node;
            return matchesName(methodDecl.getName().getIdentifier());
        }

        return false;
    }

    public SearchFor getSearchFor() {
        return searchFor;
    }

    public String getDeclaringType() {
        return declaringType;
    }

    @Override
    public String toString() {
        return String.format("MethodPattern[searchString=%s, declaringType=%s, matchRule=%s, searchFor=%s]",
                searchString, declaringType, matchRule, searchFor);
    }
}
