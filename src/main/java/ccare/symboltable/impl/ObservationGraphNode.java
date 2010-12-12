package ccare.symboltable.impl;

import java.util.Collection;

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolReference;
import ccare.symboltable.SymbolTable;

public class ObservationGraphNode {
	private static final Symbol[] EMPTY_SYMBOL_ARRAY = {};	

    private Symbol[] dependsOn = EMPTY_SYMBOL_ARRAY;
	
	void buildDependencyGraph(Symbol sym, SymbolDefinition defn, SymbolTable t) {
		int i = 0;
        final Collection<SymbolReference> dependencies = defn.getDependencies();
    	final Symbol[] newDependsOn = new Symbol[dependencies.size()];
		for (SymbolReference ref : dependencies) {
            Symbol s = t.get(ref);
            newDependsOn[i] = s;
            s.registerDependent(sym);
            i++;
        }
		dependsOn = newDependsOn;
	}

	void unregister(Symbol sym) {
		for (Symbol s : dependsOn) {
            s.unRegisterDependent(sym);
        }		
	}
}
