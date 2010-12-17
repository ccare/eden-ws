package ccare.symboltable.impl;

import ccare.symboltable.Symbol;


class DependencyGraphNode extends ObservationGraphNode {
	
	@Override
	void register(Symbol sym, SymbolImpl s) {
		s.registerDependent(sym);
	}

	@Override
	void unregister(Symbol sym, SymbolImpl s) {
		s.unRegisterDependent(sym);
	}

}
