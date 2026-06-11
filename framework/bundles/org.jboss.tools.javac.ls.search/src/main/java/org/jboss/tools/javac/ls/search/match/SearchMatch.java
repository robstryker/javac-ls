package org.jboss.tools.javac.ls.search.match;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a single match found during a search operation.
 * Contains location information (file, offset, length) and metadata about the matched element.
 */
public class SearchMatch {

    public enum MatchKind {
        TYPE_REFERENCE,
        TYPE_DECLARATION,
        METHOD_REFERENCE,
        METHOD_DECLARATION,
        FIELD_REFERENCE,
        FIELD_DECLARATION,
        CONSTRUCTOR_REFERENCE,
        CONSTRUCTOR_DECLARATION
    }

    private final Path file;
    private final int offset;
    private final int length;
    private final MatchKind kind;
    private final String elementName;

    public SearchMatch(Path file, int offset, int length, MatchKind kind, String elementName) {
        this.file = Objects.requireNonNull(file, "file cannot be null");
        this.offset = offset;
        this.length = length;
        this.kind = Objects.requireNonNull(kind, "kind cannot be null");
        this.elementName = elementName;
    }

    public Path getFile() {
        return file;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public MatchKind getKind() {
        return kind;
    }

    public String getElementName() {
        return elementName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchMatch that = (SearchMatch) o;
        return offset == that.offset &&
               length == that.length &&
               Objects.equals(file, that.file) &&
               kind == that.kind &&
               Objects.equals(elementName, that.elementName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, offset, length, kind, elementName);
    }

    @Override
    public String toString() {
        return String.format("SearchMatch[%s:%d-%d, kind=%s, element=%s]",
                file.getFileName(),
                offset,
                offset + length,
                kind,
                elementName);
    }
}
