package ccare.domain;

/**
 * User: carecx
 * Date: 14-Oct-2010
 * Time: 09:45:59
 */
public interface Symbol {

    SymbolReference getReference();

    void redefine(SymbolDefinition d, SymbolTable t);

    void forget() throws CannotForgetException;

    void expireValue();

    Object getValue(SymbolTable t);

    void registerDependent(Symbol s);

    void unRegisterDependent(Symbol s);

    void registerTrigger(Symbol s);

    boolean isTriggeredBy(Symbol symbol);

    void unRegisterTrigger(Symbol symbol);

    boolean isDependentOn(Symbol symbol);

    boolean isUpToDate();
}
