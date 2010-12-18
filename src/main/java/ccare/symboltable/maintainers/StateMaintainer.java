package ccare.symboltable.maintainers;

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolTable;

public interface StateMaintainer {

	void afterRedefinition(SymbolTable table, Symbol s);

	void beforeRedefinition(SymbolTable table, Symbol s, SymbolDefinition d);

}
