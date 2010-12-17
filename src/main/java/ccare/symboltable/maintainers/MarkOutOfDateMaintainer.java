package ccare.symboltable.maintainers;

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolReference;
import ccare.symboltable.SymbolTable;
import ccare.symboltable.impl.javascript.Definition;

public class MarkOutOfDateMaintainer implements StateMaintainer {

	@Override
	public void beforeRedefinition(SymbolTable table, Symbol s, Definition d) {
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
