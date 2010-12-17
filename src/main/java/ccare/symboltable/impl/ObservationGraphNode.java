package ccare.symboltable.impl;

import java.util.Collection;

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolReference;
import ccare.symboltable.SymbolTable;

abstract class ObservationGraphNode {
	private static final SymbolImpl[] EMPTY_SYMBOL_ARRAY = {};	

    private SymbolImpl[] backPointers = EMPTY_SYMBOL_ARRAY;
	
	void buildGraph(Symbol sym, Collection<SymbolReference> obs, SymbolTableImpl t) {
		int i = 0;
    	final SymbolImpl[] newReferences = new SymbolImpl[obs.size()];
		for (SymbolReference ref : obs) {
            SymbolImpl s = t.get(ref);
            newReferences[i] = s;
            register(sym, s);
            i++;
        }
		backPointers = newReferences;
	}

	void unregister(Symbol sym) {
		System.out.println("unregistering " + backPointers.length);
		for (SymbolImpl s : backPointers) {
           unregister(sym, s);
        }		
	}

	abstract void register(Symbol sym, SymbolImpl s);

	abstract void unregister(Symbol sym, SymbolImpl s);
}
