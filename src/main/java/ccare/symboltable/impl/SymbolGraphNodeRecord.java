package ccare.symboltable.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolReference;
import ccare.symboltable.SymbolTable;

abstract class SymbolGraphNodeRecord {
	private static final SymbolImpl[] EMPTY_SYMBOL_ARRAY = {};
	
	private final Set<Symbol> listeners = new HashSet<Symbol>();

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

	public void removeListener(Symbol symbol) {
		listeners.remove(symbol);		
	}

	public void addListener(Symbol symbol) {
		listeners.add(symbol);
	}

	public boolean hasListeners() {
		return !listeners.isEmpty();
	}

	public Set<Symbol> getListeners() {
		return Collections.unmodifiableSet(listeners);
	}
}
