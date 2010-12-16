package ccare.symboltable.impl;


public class TriggerGraphNode extends ObservationGraphNode {
	
	@Override
	void register(Symbol sym, Symbol s) {
		s.registerTrigger(sym);
	}

	@Override
	void unregister(Symbol sym, Symbol s) {
		s.unRegisterTrigger(sym);
	}

}
