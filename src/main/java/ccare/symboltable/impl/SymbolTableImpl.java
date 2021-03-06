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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.mozilla.javascript.Undefined;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccare.monitoring.AbstractMonitoringBean;
import ccare.service.SymbolTableBean;
import ccare.symboltable.LanguageExecutor;
import ccare.symboltable.LanugageSupport;
import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolReference;
import ccare.symboltable.SymbolTable;
import ccare.symboltable.exceptions.CannotDefineException;
import ccare.symboltable.exceptions.SymbolTableException;
import ccare.symboltable.impl.javascript.JavaScriptLanguageExecutor;
import ccare.symboltable.impl.javascript.JavaScriptLanguageSupport;
import ccare.symboltable.maintainers.MarkOutOfDateMaintainer;
import ccare.symboltable.maintainers.StateMaintainer;
import ccare.symboltable.maintainers.TriggeredProcScheduler;

public class SymbolTableImpl extends AbstractMonitoringBean implements
		SymbolTable {
	
	private static final Logger logger = LoggerFactory.getLogger(SymbolTableImpl.class);

	private StateMaintainer asyncMaintainer = new TriggeredProcScheduler();
	private LanguageExecutor executor = new JavaScriptLanguageExecutor(this);
	private final UUID id = UUID.randomUUID();
	private LanugageSupport languageSupport = new JavaScriptLanguageSupport();
	private String name;
	private Map<SymbolReference, SymbolImpl> symbols = new HashMap<SymbolReference, SymbolImpl>();
	private StateMaintainer syncMaintainer = new MarkOutOfDateMaintainer();
		
	private boolean showDetailedLogging = false;
	
	public SymbolTableImpl() {
		register("ccare.symboltable", "SymbolTable", id.toString());
	}

	public void add(SymbolImpl sym) {
		symbols.put(sym.getReference(), sym);
	}

	@Override
	public void define(SymbolReference ref, String defn) {
		if (ref == null || defn == null) {
			throw new CannotDefineException();
		}
		doRedefine(ref, languageSupport.createDefinition(defn));
	}

	@Override
	public void defineFunction(SymbolReference ref, String defn) {
		final SymbolDefinition d = languageSupport.defineFunction(defn);
		doRedefine(ref, d);
	}

	@Override
	public void defineTriggeredProc(SymbolReference ref, String defn,
			String... triggers) {
		final SymbolDefinition d = languageSupport.defineTriggeredProc(defn,
				triggers);
		doRedefine(ref, d);
	}

	private void doRedefine(SymbolReference ref, final SymbolDefinition d) {
		SymbolImpl s = get(ref);
		asyncMaintainer.beforeRedefinition(this, s, d);
		syncMaintainer.beforeRedefinition(this, s, d);
		s.redefine(d, this);
		syncMaintainer.afterRedefinition(this, s);
		asyncMaintainer.afterRedefinition(this, s);
	}

	private Object eval(final SymbolDefinition defn) {
		try {
			return defn.evaluate(this.executor);
		} catch (Exception e) {
			e.printStackTrace();
			return Undefined.instance;
		}
	}

	@Override
	public Object evaluate(String expression) {
		logger.debug("Evaluating: " + expression);
		return eval(languageSupport.createDefinition(expression));
	}

	@Override
	public String evaluateString(String expression) {
		Object o = evaluate(expression);
		return (o == null) ? "null" : o.toString();
	}

	@Override
	public Object execute(SymbolReference a) {
		logger.debug("Executing " + a);
		return eval(languageSupport.createMethodCall(a.getName()));
	}

	@Override
	public Object execute(SymbolReference a, Object... params) {
		logger.debug("Executing " + a + " with " + params.length + " params");
		return eval(languageSupport.createMethodCall(a.getName(), params));
	}

	public void fireTriggers(Set<Symbol> set) {
		logger.debug("Fire triggers");
		for (Symbol s : set) {
			this.execute(s.getReference());
		}
	}

	public SymbolImpl get(SymbolReference reference) {
		logger.debug("Get symbol " + reference);
		if (!symbols.containsKey(reference)) {
			logger.debug("Introducing symbol to symboltable" + reference);
			add(new SymbolImpl(reference));
		}
		return symbols.get(reference);
	}

	@Override
	public LanguageExecutor getExecutor() {
		return executor;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getSymbolCount() {
		return this.getSymbols().size();
	}

	@Override
	public Set<SymbolReference> getSymbols() {
		return Collections.unmodifiableSet(symbols.keySet());
	}

	@Override
	public Object getValue(SymbolReference ref) {
		logger.debug("Get value for " + ref);
		SymbolImpl s = get(ref);
		try {
			return s.getValue(this.executor);
		} catch (SymbolTableException e) {
			logger.info("Exception getting value for " + ref);
			e.printStackTrace();
		}
		logger.info("Returning UNDEFINED for " + ref);
		return Undefined.instance;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}
	
}