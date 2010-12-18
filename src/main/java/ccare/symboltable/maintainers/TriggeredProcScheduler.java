package ccare.symboltable.maintainers;

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolTable;

public class TriggeredProcScheduler implements StateMaintainer {

	@Override
	public void beforeRedefinition(SymbolTable table, Symbol s,
			SymbolDefinition d) {
	}

	@Override
	public void afterRedefinition(SymbolTable table, Symbol s) {
		for (Symbol ss : s.getTriggers()) {
			table.execute(ss.getReference());
		}
	}

}
