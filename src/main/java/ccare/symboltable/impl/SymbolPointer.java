package ccare.symboltable.impl;

import java.lang.ref.WeakReference;

import ccare.symboltable.Symbol;

public class SymbolPointer extends WeakReference<Symbol> {

	public SymbolPointer(Symbol referent) {
		super(referent);
	}
}
