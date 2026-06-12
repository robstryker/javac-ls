package org.jboss.tools.javac.ls.search.pattern;

import shaded.org.eclipse.jdt.core.dom.ASTNode;
import shaded.org.eclipse.jdt.core.dom.FieldAccess;
import shaded.org.eclipse.jdt.core.dom.FieldDeclaration;
import shaded.org.eclipse.jdt.core.dom.IVariableBinding;
import shaded.org.eclipse.jdt.core.dom.SimpleName;
import shaded.org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Search pattern for finding field references or declarations.
 */
public class FieldPattern extends SearchPattern {

    public enum SearchFor {
        REFERENCES,
        DECLARATIONS,
        ALL_OCCURRENCES
    }

    private final SearchFor searchFor;
    private final String declaringType;

    public FieldPattern(String fieldName, String declaringType, MatchRule matchRule, SearchFor searchFor) {
        super(fieldName, matchRule);
        this.declaringType = declaringType;
        this.searchFor = searchFor != null ? searchFor : SearchFor.ALL_OCCURRENCES;
    }

    public FieldPattern(String fieldName) {
        this(fieldName, null, MatchRule.EXACT_MATCH, SearchFor.ALL_OCCURRENCES);
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
        if (node instanceof FieldAccess) {
            FieldAccess fieldAccess = (FieldAccess) node;
            SimpleName fieldName = fieldAccess.getName();

            if (!matchesName(fieldName.getIdentifier())) {
                return false;
            }

            if (declaringType != null) {
                IVariableBinding binding = fieldAccess.resolveFieldBinding();
                if (binding != null && binding.getDeclaringClass() != null) {
                    String actualDeclaringType = binding.getDeclaringClass().getName();
                    return declaringType.equals(actualDeclaringType);
                }
            }

            return true;
        }

        if (node instanceof SimpleName) {
            SimpleName name = (SimpleName) node;
            IVariableBinding binding = resolveFieldBinding(name);

            if (binding != null && binding.isField()) {
                if (!matchesName(name.getIdentifier())) {
                    return false;
                }

                if (declaringType != null && binding.getDeclaringClass() != null) {
                    String actualDeclaringType = binding.getDeclaringClass().getName();
                    return declaringType.equals(actualDeclaringType);
                }

                return true;
            }
        }

        return false;
    }

    private boolean matchesDeclaration(ASTNode node) {
        if (node instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;

            if (fragment.getParent() instanceof FieldDeclaration) {
                return matchesName(fragment.getName().getIdentifier());
            }
        }

        return false;
    }

    private IVariableBinding resolveFieldBinding(SimpleName name) {
        if (name.resolveBinding() instanceof IVariableBinding) {
            return (IVariableBinding) name.resolveBinding();
        }
        return null;
    }

    public SearchFor getSearchFor() {
        return searchFor;
    }

    public String getDeclaringType() {
        return declaringType;
    }

    @Override
    public String toString() {
        return String.format("FieldPattern[searchString=%s, declaringType=%s, matchRule=%s, searchFor=%s]",
                searchString, declaringType, matchRule, searchFor);
    }
}
