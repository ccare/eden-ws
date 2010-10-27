package ccare.domain;

import java.util.UUID;

/**
 * User: carecx
 * Date: 13-Oct-2010
 * Time: 22:24:49
 */
public class SymbolReference {
    private final String name;

    public SymbolReference() {
        final UUID id = UUID.randomUUID();
        name = id.toString();
    }

    public SymbolReference(final String symbolName) {
        name = symbolName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SymbolReference that = (SymbolReference) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public String toString() {
        return "Ref<" + getName() + ">";
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String getName() {
        return name;
    }
}
