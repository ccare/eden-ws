package ccare.symboltable;

import java.util.Set;

public interface Symbol {

	public abstract Set<Symbol> getDependents();

	public abstract Set<Symbol> getTriggers();

	public abstract SymbolDefinition getDefinition();

	public abstract void expireValue();

	public abstract SymbolReference getReference();


}