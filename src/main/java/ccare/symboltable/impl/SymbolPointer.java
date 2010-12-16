package ccare.symboltable.impl;

import java.lang.ref.WeakReference;


public class SymbolPointer extends WeakReference<Symbol> {

	public SymbolPointer(Symbol referent) {
		super(referent);
	}
}
