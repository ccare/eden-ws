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

package ccare.symboltable.impl.javascript;

import static ccare.symboltable.impl.javascript.TranslationUtils.extractSpecialSymbols;
import static ccare.symboltable.impl.javascript.TranslationUtils.translateExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ccare.symboltable.LanguageExecutor;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolReference;

/**
 * Created by IntelliJ IDEA. User: carecx Date: 25-Oct-2010 Time: 17:06:58 To
 * change this template use File | Settings | File Templates.
 */
class Definition implements SymbolDefinition {

	public enum ExprType {
		EXPRESSION, FUNCTION
	}

	private final String expr;
	private List<SymbolReference> triggers;

	private final ExprType type;

	public Definition(String expr) {
		this(expr, ExprType.EXPRESSION, null);
	}

	public Definition(String expr, ExprType type, String... triggers) {
		this.expr = expr;
		this.type = type;
		if (triggers == null || triggers.length == 0) {
			this.triggers = Collections.<SymbolReference> emptyList();
		} else {
			this.triggers = new ArrayList(triggers.length);
			for (String trigger : triggers) {
				this.triggers.add(new SymbolReference(trigger.replaceAll("^#",
						"")));
			}
		}
	}

	@Override
	public Object evaluate(final LanguageExecutor context) {
		return context.evaluate(this);
	}

	@Override
	public Collection<SymbolReference> getDependencies() {
		final Set<String> dependentNames = extractSpecialSymbols(expr);
		Set<SymbolReference> refs = new HashSet<SymbolReference>(
				dependentNames.size());
		for (String symbolName : dependentNames) {
			refs.add(new SymbolReference(symbolName));
		}
		return refs;
	}

	@Override
	public String getExpr() {
		return translateExpression(expr);
	}

	@Override
	public Collection<SymbolReference> getTriggers() {
		return triggers;
	}

	@Override
	public boolean isExecutable() {
		return type == ExprType.FUNCTION;
	}

	@Override
	public String toString() {
		return expr;
	}
}
