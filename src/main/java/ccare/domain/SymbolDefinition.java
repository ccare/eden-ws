package ccare.domain;

import java.util.Collection;

/**
 * User: carecx
 * Date: 13-Oct-2010
 * Time: 22:25:05
 */
public interface SymbolDefinition {
    public Collection<SymbolReference> getDependencies();

    public Collection<SymbolReference> getTriggers();

    public Object evaluate(SymbolTable t);

    boolean isExecutable();
}
