package org.jboss.tools.javac.ls.search.pattern;

import shaded.org.eclipse.jdt.core.dom.ASTNode;
import shaded.org.eclipse.jdt.core.dom.ClassInstanceCreation;
import shaded.org.eclipse.jdt.core.dom.ConstructorInvocation;
import shaded.org.eclipse.jdt.core.dom.IMethodBinding;
import shaded.org.eclipse.jdt.core.dom.MethodDeclaration;
import shaded.org.eclipse.jdt.core.dom.SuperConstructorInvocation;

/**
 * Search pattern for finding constructor references or declarations.
 */
public class ConstructorPattern extends SearchPattern {

    public enum SearchFor {
        REFERENCES,
        DECLARATIONS,
        ALL_OCCURRENCES
    }

    private final SearchFor searchFor;

    public ConstructorPattern(String typeName, MatchRule matchRule, SearchFor searchFor) {
        super(typeName, matchRule);
        this.searchFor = searchFor != null ? searchFor : SearchFor.ALL_OCCURRENCES;
    }

    public ConstructorPattern(String typeName) {
        this(typeName, MatchRule.EXACT_MATCH, SearchFor.ALL_OCCURRENCES);
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
        if (node instanceof ClassInstanceCreation) {
            ClassInstanceCreation creation = (ClassInstanceCreation) node;
            IMethodBinding binding = creation.resolveConstructorBinding();

            if (binding != null && binding.getDeclaringClass() != null) {
                String typeName = binding.getDeclaringClass().getName();
                return matchesName(typeName);
            }

            if (creation.getType() != null) {
                String typeName = creation.getType().toString();
                return matchesName(typeName);
            }
        }

        if (node instanceof ConstructorInvocation) {
            ConstructorInvocation invocation = (ConstructorInvocation) node;
            IMethodBinding binding = invocation.resolveConstructorBinding();

            if (binding != null && binding.getDeclaringClass() != null) {
                String typeName = binding.getDeclaringClass().getName();
                return matchesName(typeName);
            }
        }

        if (node instanceof SuperConstructorInvocation) {
            SuperConstructorInvocation invocation = (SuperConstructorInvocation) node;
            IMethodBinding binding = invocation.resolveConstructorBinding();

            if (binding != null && binding.getDeclaringClass() != null) {
                String typeName = binding.getDeclaringClass().getName();
                return matchesName(typeName);
            }
        }

        return false;
    }

    private boolean matchesDeclaration(ASTNode node) {
        if (node instanceof MethodDeclaration) {
            MethodDeclaration methodDecl = (MethodDeclaration) node;

            if (methodDecl.isConstructor()) {
                return matchesName(methodDecl.getName().getIdentifier());
            }
        }

        return false;
    }

    public SearchFor getSearchFor() {
        return searchFor;
    }

    @Override
    public String toString() {
        return String.format("ConstructorPattern[searchString=%s, matchRule=%s, searchFor=%s]",
                searchString, matchRule, searchFor);
    }
}
