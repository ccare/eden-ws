package ccare.symboltable.maintainers;

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolTable;
import ccare.symboltable.impl.javascript.Definition;

public interface DependencyMaintainer {

	void beforeRedefinition(SymbolTable table, Symbol s, Definition d);

	void afterRedefinition(SymbolTable table, Symbol s);

}
