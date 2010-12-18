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

import java.lang.ref.SoftReference;
import java.util.Set;

import org.mozilla.javascript.Undefined;

import ccare.symboltable.LanguageExecutor;
import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolReference;
import ccare.symboltable.exceptions.CannotForgetException;

/**
 * User: carecx Date: 13-Oct-2010 Time: 23:36:43
 */
class SymbolImpl implements Symbol {

	private SoftReference<Object> cachedValue;
	private SymbolDefinition definition;
	private SymbolGraphNodeRecord dependsOn = new DependencyGraphNodeRecord();
	private final SymbolReference ref;

	private SymbolGraphNodeRecord tb = new TriggerGraphNodeRecord();
	private boolean upToDate;

	public SymbolImpl(SymbolReference ref) {
		this.ref = ref;
	}

	private void buildDefinitions(SymbolTableImpl t) {
		dependsOn.buildGraph(this, definition.getDependencies(), t);
		tb.buildGraph(this, definition.getTriggers(), t);
	}

	private void clearDefinitions() {
		cachedValue = null;
		// value = null;
		definition = null;
		dependsOn.unregister(this);
		tb.unregister(this);
	}

	@Override
	public void expireValue() {
		upToDate = false;
	}

	void forget() throws CannotForgetException {
		if (dependsOn.hasListeners() || tb.hasListeners()) {
			throw new CannotForgetException(
					"Cannot forget a symbol inside a dependency graph");
		} else {
			clearDefinitions();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ccare.symboltable.impl.ISymbol#getDefinition()
	 */
	@Override
	public SymbolDefinition getDefinition() {
		return definition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ccare.symboltable.impl.ISymbol#getDependents()
	 */
	@Override
	public Set<Symbol> getDependents() {
		return dependsOn.getListeners();
	}

	@Override
	public SymbolReference getReference() {
		return ref;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ccare.symboltable.impl.ISymbol#getTriggers()
	 */
	@Override
	public Set<Symbol> getTriggers() {
		return tb.getListeners();
	}

	Object getValue(LanguageExecutor exec) {
		if (definition == null) {
			return Undefined.instance;
		}
		if (!upToDate || cachedValue == null) {
			cachedValue = new SoftReference<Object>(definition.evaluate(exec));
			upToDate = true;
		}
		return cachedValue.get();
	}

	boolean isUpToDate() {
		return upToDate;
	}

	void redefine(SymbolDefinition d, SymbolTableImpl t) {
		upToDate = false;
		clearDefinitions();
		definition = d;
		buildDefinitions(t);
		expireValue();
	}

	void registerDependent(Symbol s) {
		dependsOn.addListener(s);
	}

	void registerTrigger(Symbol symbol) {
		tb.addListener(symbol);
	}

	void unRegisterDependent(Symbol s) {
		dependsOn.removeListener(s);
	}

	void unRegisterTrigger(Symbol symbol) {
		tb.removeListener(symbol);
	}
}
