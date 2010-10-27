package ccare.domain;

import java.util.Set;

/**
 * User: carecx
 * Date: 26-Oct-2010
 * Time: 15:24:19
 */
public interface SymbolTable {
    Set<SymbolReference> listSymbols();

    Symbol get(SymbolReference reference);

    void fireTriggers(Set<Symbol> triggers);

    void define(SymbolReference aRef, String s);

    Object getValue(SymbolReference bRef);

    void defineFunction(SymbolReference a, String s);

    void defineTriggeredProc(SymbolReference a, String s, String... triggers);

    void execute(SymbolReference a);
}
