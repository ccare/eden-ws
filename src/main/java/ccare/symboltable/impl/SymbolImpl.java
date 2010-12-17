/*
 * Copyright (c) 2010, Charles Care
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ccare.symboltable.impl;

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolReference;
import ccare.symboltable.SymbolTable;
import ccare.symboltable.exceptions.CannotForgetException;

import org.mozilla.javascript.Undefined;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.management.NotificationBroadcasterSupport;

/**
 * User: carecx Date: 13-Oct-2010 Time: 23:36:43
 */
class SymbolImpl implements Symbol {

	private final SymbolReference ref;
	private SymbolDefinition definition;
	private SoftReference<Object> cachedValue;
	// private Object value;
	private boolean upToDate;
	//private Set<Symbol> dependents = new HashSet<Symbol>();
	//private Set<Symbol> triggers = new HashSet<Symbol>();

	private SymbolGraphNodeRecord dependsOn = new DependencyGraphNodeRecord();
	private SymbolGraphNodeRecord tb = new TriggerGraphNodeRecord();

	public SymbolImpl(SymbolReference ref) {
		this.ref = ref;
	}

	/* (non-Javadoc)
	 * @see ccare.symboltable.impl.ISymbol#getDependents()
	 */
	@Override
	public Set<Symbol> getDependents() {
		return dependsOn.listeners;
	}

	/* (non-Javadoc)
	 * @see ccare.symboltable.impl.ISymbol#getTriggers()
	 */
	@Override
	public Set<Symbol> getTriggers() {
		return tb.listeners;
	}

	@Override
	public SymbolReference getReference() {
		return ref;
	}

	public void redefine(SymbolDefinition d, SymbolTableImpl t) {
		upToDate = false;
		clearDefinitions();
		definition = d;
		buildDefinitions(t);
		expireValue();
	}

	public void forget() throws CannotForgetException {
		System.out.println("Dependencies are " + dependsOn.listeners.size());
		System.out.println("Triggers are " + tb.listeners.size());
		if (dependsOn.listeners.isEmpty() && tb.listeners.isEmpty()) {
			clearDefinitions();
		} else {
			throw new CannotForgetException(
					"Cannot forget a symbol inside a dependency graph");
		}
		System.out.println("After Dependencies are " + dependsOn.listeners.size());
		System.out.println("After Triggers are " + tb.listeners.size());
	}

	@Override
	public void expireValue() {
		upToDate = false;
	}

	public Object getValue(SymbolTable t) {
		if (definition == null) {
			return Undefined.instance;
		}
		if (!upToDate || cachedValue == null) {
			cachedValue = new SoftReference(definition.evaluate(t));
			upToDate = true;
		}
		return cachedValue.get();
	}

	public void registerDependent(Symbol s) {
		dependsOn.listeners.add((Symbol) s);
	}

	public void unRegisterDependent(Symbol s) {
		dependsOn.listeners.remove(s);
	}

	public boolean isUpToDate() {
		return upToDate;
	}

	public void registerTrigger(Symbol s) {
		tb.listeners.add(s);
	}

	public void unRegisterTrigger(Symbol symbol) {
		tb.listeners.remove(symbol);
	}

	private void clearDefinitions() {
		cachedValue = null;
		// value = null;
		definition = null;
		dependsOn.unregister(this);
		tb.unregister(this);
	}

	private void buildDefinitions(SymbolTableImpl t) {
		dependsOn.buildGraph(this, definition.getDependencies(), t);
		tb.buildGraph(this, definition.getTriggers(), t);
	}

	/* (non-Javadoc)
	 * @see ccare.symboltable.impl.ISymbol#getDefinition()
	 */
	@Override
	public SymbolDefinition getDefinition() {
		return definition;
	}
}
