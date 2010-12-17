package ccare.symboltable.impl;

import ccare.symboltable.Symbol;


class TriggerGraphNode extends ObservationGraphNode {
	
	@Override
	void register(Symbol sym, SymbolImpl s) {
		s.registerTrigger(sym);
	}

	@Override
	void unregister(Symbol sym, SymbolImpl s) {
		s.unRegisterTrigger(sym);
	}

}
