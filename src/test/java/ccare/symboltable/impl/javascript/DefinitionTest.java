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

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import ccare.symboltable.LanguageExecutor;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolReference;
import ccare.symboltable.SymbolTable;
import ccare.symboltable.SymbolTableMXBean;
import ccare.symboltable.impl.SymbolTableImpl;

/**
 * Created by IntelliJ IDEA. User: carecx Date: 25-Oct-2010 Time: 17:07:10 To
 * change this template use File | Settings | File Templates.
 */
public class DefinitionTest {

	public LanguageExecutor getExecutor() {
		return null;
	}

	private SymbolTable stubSymbolTable(final Object value) {
		return new SymbolTable() {
			@Override
			public void define(SymbolReference aRef, String s) {

			}

			@Override
			public void defineFunction(SymbolReference a, String s) {

			}

			@Override
			public void defineTriggeredProc(SymbolReference a, String s,
					String... triggers) {

			}

			@Override
			public Object evaluate(String s) {
				return null; // To change body of implemented methods use File |
								// Settings | File Templates.
			}

			@Override
			public String evaluateString(String expression) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object execute(SymbolReference a) {
				return null;
			}

			@Override
			public Object execute(SymbolReference a, Object... params) {
				return null;
			}

			@Override
			public LanguageExecutor getExecutor() {
				return new JavaScriptLanguageExecutor(this);
			}

			@Override
			public UUID getId() {
				return null;
			}

			@Override
			public String getName() {
				return null; // To change body of implemented methods use File |
								// Settings | File Templates.
			}

			@Override
			public int getSymbolCount() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Set<SymbolReference> getSymbols() {
				return null;
			}

			@Override
			public Object getValue(SymbolReference bRef) {
				return value;
			}

			@Override
			public void setName(String name) {
				// To change body of implemented methods use File | Settings |
				// File Templates.
			}
		};
	}

	@Test
	public void testCallMagicObserveFunctionDelegatesCorrectly()
			throws Exception {
		final SymbolDefinition d = new Definition("$eden_observe('a')");
		final SymbolTable t = stubSymbolTable("abc");
		assertEquals("abc", d.evaluate(t.getExecutor()));
	}

	@Test
	public void testCreate() {
		new Definition("a+b");
	}

	@Test
	public void testEvaluate() throws Exception {
		SymbolDefinition d = new Definition("1+2");
		assertEquals(3, d.evaluate(new JavaScriptLanguageExecutor(null)));
	}

	@Test
	public void testEvaluateDependency() throws Exception {
		final SymbolTableMXBean table = new SymbolTableImpl();
		final SymbolDefinition d = new Definition("#a");
		final SymbolTable t = stubSymbolTable("abc");
		assertEquals("abc", d.evaluate(t.getExecutor()));
	}

	@Test
	public void testEvaluateDependencyExpression() throws Exception {
		final SymbolDefinition d1 = new Definition("#a + 'def'");
		final SymbolDefinition d2 = new Definition("#{a} + 'def'");
		final SymbolTable t = stubSymbolTable("abc");
		final String target = "abcdef";
		assertEquals(target, d1.evaluate(t.getExecutor()));
		assertEquals(target, d2.evaluate(t.getExecutor()));
	}

	@Test
	public void testEvaluateE4X() throws Exception {
		SymbolDefinition d = new Definition(
				"<xml><foo>bar</foo></xml>.foo.toString()");
		assertEquals("bar", d.evaluate(new JavaScriptLanguageExecutor(null)));
	}

	@Test
	public void testExpressionTranslationBaseCase() {
		validateExprUnchangedFor("a+b");
		validateExprUnchangedFor("1 * 123 + 3 % b");
		validateExprUnchangedFor("\"aa\" + 'b' + c");
	}

	@Test
	public void testExpressionTranslationForCalculatedExpresions() {
		validateExpr("$eden_observe('abc')", "#{abc}");
	}

	@Test
	public void testExpressionTranslationForDefinition() {
		validateExpr("$eden_define('a','b+c')", "#a is b+c");
	}

	@Test
	public void testExpressionTranslationForDefinitionsInFunctions() {
		validateExpr("function() {$eden_define('a','b+c')}",
				"function() {#a is b+c}");
		validateExpr("function() {$eden_define('a','b+c');}",
				"function() {#a is b+c;}");
		validateExpr("function() {$eden_define('a','[1,2,\"3\"]');}",
				"function() {#a is [1,2,\"3\"];}");
		validateExpr("function() {$eden_define('a','12')}",
				"function() {#a is 12}");
	}

	@Test
	public void testExpressionTranslationForDefinitionsInFunctionsSplitsOnSemiColonAndNewline() {
		validateExpr(
				"function() {$eden_define('a','12'); $eden_define('b','5'); return $eden_observe('b')}",
				"function() {#a is 12; #b is 5; return #b}");
		validateExpr("function() {" + "$eden_define('a','12');\n"
				+ "$eden_define('b','5');\n" + "return $eden_observe('b')\n"
				+ "}", "function() {" + "#a is 12;\n" + "#b is 5;\n"
				+ "return #b\n}");
		validateExpr("function() {" + "$eden_define('a','12')\n"
				+ "$eden_define('b','5')\n" + "return $eden_observe('b')\n"
				+ "}", "function() {" + "#a is 12\n" + "#b is 5\n"
				+ "return #b\n}");
	}

	@Test
	public void testExpressionTranslationForDefinitionWithDependency() {
		validateExpr("$eden_define('a','#b+c')", "#a is #b+c");
		validateExpr("$eden_define('a','#c')", "#a is #c");
		validateExpr("$eden_define('a','f(#c)')", "#a is f(#c)");
		validateExpr("$eden_define('a','#f(#c)')", "#a is #f(#c)");
	}

	@Test
	public void testExpressionTranslationForDoubleQuoteStrings() {
		validateExpr("function() {$eden_define('a','\"b+c\"');}",
				"function() {#a is \"b+c\";}");
	}

	@Test
	public void testExpressionTranslationForDoubleQuoteStringsContainingDependency() {
		validateExpr("function() {$eden_define('a','\"#b+c\"');}",
				"function() {#a is \"#b+c\";}");
	}

	@Test
	public void testExpressionTranslationForExpressionWithEscapedObservables() {
		validateExpr("$eden_observe('abc')", "#{abc}");
		validateExpr("$eden_observe('http://foo/bar/') * $eden_observe('b')",
				"#{http://foo/bar/} * #b");
	}

	@Test
	public void testExpressionTranslationForExpressionWithSimpleObservables() {
		validateExpr("a+$eden_observe('c')", "a+#c");
		validateExpr("a % $eden_observe('c_c_d')", "a % #c_c_d");
	}

	@Test
	public void testExpressionTranslationForObjects() {
		validateExpr("$eden_define('a','({ a: 1})')", "#a is ({ a: 1})");
	}

	@Test
	public void testExpressionTranslationForObjectsInFunctions() {
		validateExpr("function() { $eden_define('a','({ a: 1})'); }",
				"function() { #a is ({ a: 1}); }");
	}

	@Test
	public void testExpressionTranslationForSingleQuoteStrings() {
		validateExpr("$eden_define('a','\\'...\\'')", "#a is '...'");
	}

	@Test
	public void testExpressionTranslationForSingleQuoteStringsInFunctions() {
		validateExpr("function() {$eden_define('a','\\'b+c\\'')}",
				"function() {#a is 'b+c'}");
		validateExpr("function() {$eden_define('a','\\'b+c\\'');}",
				"function() {#a is 'b+c';}");
	}

	@Test
	public void testGetDependenciesAndTriggersForSimpleExpr() throws Exception {
		SymbolDefinition d = new Definition("1 + 2");
		assertEquals(0, d.getDependencies().size());
		assertEquals(0, d.getTriggers().size());
	}

	@Test
	public void testGetDependenciesForExpressionWithDependency()
			throws Exception {
		SymbolDefinition d = new Definition("1 + #x");
		final Collection<SymbolReference> dependencies = d.getDependencies();
		assertEquals(1, dependencies.size());
		assertEquals(0, d.getTriggers().size());
	}

	@Test
	public void testPrintln() throws Exception {
		SymbolDefinition d = new Definition("java.lang.System.out.println(3)");
		d.evaluate(new JavaScriptLanguageExecutor(null));
	}

	@Test
	public void testToString() {
		final String expr = "a+b";
		assertEquals(expr, new Definition(expr).toString());
	}

	private void validateExpr(final String target, final String expr) {
		final SymbolDefinition defn = new Definition(expr);
		assertEquals(target, defn.getExpr());
	}

	private void validateExprUnchangedFor(final String expr) {
		validateExpr(expr, expr);
	}

}
