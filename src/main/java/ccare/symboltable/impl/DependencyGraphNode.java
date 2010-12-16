package ccare.symboltable.impl;


public class DependencyGraphNode extends ObservationGraphNode {
	
	@Override
	void register(Symbol sym, Symbol s) {
		s.registerDependent(sym);
	}

	@Override
	void unregister(Symbol sym, Symbol s) {
		s.unRegisterDependent(sym);
	}

}
