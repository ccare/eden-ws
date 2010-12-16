package ccare.symboltable.impl;

import java.util.Collection;

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolReference;
import ccare.symboltable.SymbolTable;

public abstract class ObservationGraphNode {
	private static final Symbol[] EMPTY_SYMBOL_ARRAY = {};	

    private Symbol[] backPointers = EMPTY_SYMBOL_ARRAY;
	
	void buildGraph(Symbol sym, Collection<SymbolReference> obs, SymbolTable t) {
		System.out.println("Building graph for refs");
		for (SymbolReference r : obs) {
			System.out.println(r.getName());
		}
		int i = 0;
        //final Collection<SymbolReference> dependencies = defn.getDependencies();
    	final Symbol[] newReferences = new Symbol[obs.size()];
		for (SymbolReference ref : obs) {
            Symbol s = t.get(ref);
            newReferences[i] = s;
            register(sym, s);
            i++;
        }
		backPointers = newReferences;
	}

	void unregister(Symbol sym) {
		System.out.println("unregistering " + backPointers.length);
		for (Symbol s : backPointers) {
           unregister(sym, s);
        }		
	}

	abstract void register(Symbol sym, Symbol s);

	abstract void unregister(Symbol sym, Symbol s);
}
