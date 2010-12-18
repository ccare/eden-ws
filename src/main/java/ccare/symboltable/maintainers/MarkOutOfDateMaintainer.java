package ccare.symboltable.maintainers;

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolTable;

public class MarkOutOfDateMaintainer implements StateMaintainer {

	@Override
	public void beforeRedefinition(SymbolTable table, Symbol s,
			SymbolDefinition d) {
	}

	@Override
	public void afterRedefinition(SymbolTable table, Symbol s) {
		doRecursivelyExpireValue(s);
	}

	private void doRecursivelyExpireValue(Symbol s) {
		s.expireValue();
		for (Symbol dependent : s.getDependents()) {
			doRecursivelyExpireValue(dependent);
		}
	}

}
