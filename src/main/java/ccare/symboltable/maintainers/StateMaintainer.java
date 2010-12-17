package ccare.symboltable.maintainers;

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolTable;

public interface StateMaintainer {

	void beforeRedefinition(SymbolTable table, Symbol s, SymbolDefinition d);

	void afterRedefinition(SymbolTable table, Symbol s);

}
