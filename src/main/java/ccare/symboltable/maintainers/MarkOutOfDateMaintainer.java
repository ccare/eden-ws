package ccare.symboltable.maintainers;

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolReference;
import ccare.symboltable.impl.javascript.Definition;

public class MarkOutOfDateMaintainer implements DependencyMaintainer {

	@Override
	public void beforeRedefinition(Symbol s, Definition d) {
	}

	@Override
	public void afterRedefinition(Symbol s) {
		doRecursivelyExpireValue(s);
	}
	
	private void doRecursivelyExpireValue(Symbol s) {
		s.expireValue();
		for (Symbol dependent : s.getDependents()) {
			doRecursivelyExpireValue(dependent);
		}
	}

}
