package ccare.symboltable.impl;

import ccare.symboltable.Symbol;

class DependencyGraphNodeRecord extends SymbolGraphNodeRecord {

	@Override
	void register(Symbol sym, SymbolImpl s) {
		s.registerDependent(sym);
	}

	@Override
	void unregister(Symbol sym, SymbolImpl s) {
		s.unRegisterDependent(sym);
	}

}
