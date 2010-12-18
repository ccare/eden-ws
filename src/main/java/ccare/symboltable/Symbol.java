package ccare.symboltable;

import java.util.Set;

public interface Symbol {

	public abstract void expireValue();

	public abstract SymbolDefinition getDefinition();

	public abstract Set<Symbol> getDependents();

	public abstract SymbolReference getReference();

	public abstract Set<Symbol> getTriggers();

}