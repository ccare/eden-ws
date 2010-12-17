package ccare.symboltable.maintainers;

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolTable;
import ccare.symboltable.impl.javascript.Definition;

public class TriggeredProcScheduler implements StateMaintainer {

	@Override
	public void beforeRedefinition(SymbolTable table, Symbol s, Definition d) {
	}

	@Override
	public void afterRedefinition(SymbolTable table, Symbol s) {
		for (Symbol ss : s.getTriggers()) {
			table.execute(ss.getReference());
		}
	}

}
