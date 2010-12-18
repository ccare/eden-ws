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

import static ccare.symboltable.impl.javascript.TranslationUtils.SPECIALNAME_PATTERN;
import static ccare.symboltable.impl.javascript.TranslationUtils.encodeObservation;
import static ccare.symboltable.impl.javascript.TranslationUtils.extractExpr;
import static ccare.symboltable.impl.javascript.TranslationUtils.extractSpecialSymbols;
import static ccare.symboltable.impl.javascript.TranslationUtils.findEndOfExpr;
import static ccare.symboltable.impl.javascript.TranslationUtils.findExprRange;
import static ccare.symboltable.impl.javascript.TranslationUtils.pullOutRegions;
import static ccare.symboltable.impl.javascript.TranslationUtils.translateE4XObservation;
import static ccare.symboltable.impl.javascript.TranslationUtils.translateExpression;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import ccare.symboltable.impl.javascript.TranslationUtils.DefnFragment;

/**
 * User: carecx Date: 30-Oct-2010 Time: 16:19:41
 */
public class TranslationUtilsTest {

	@Test
	public void testEncodeObservation_simple() {
		assertEquals("a", encodeObservation("a"));
		assertEquals("a + b", encodeObservation("a + b"));
		assertEquals("function () {a + b}",
				encodeObservation("function () {a + b}"));
	}

	@Test
	public void testEncodeObservation_withObservable() {
		assertEquals("$eden_observe('a')", encodeObservation("#a"));
		assertEquals("a + $eden_observe('b')", encodeObservation("a + #b"));
		assertEquals("function () {a + $eden_observe('b')}",
				encodeObservation("function () {a + #b}"));
	}

	@Test
	public void testEncodeObservation_withObservableNameInString() {
		assertEquals("'#a'", encodeObservation("'#a'"));
	}

	@Test
	public void testEncodeObservation_withStrings() {
		assertEquals("'a'", encodeObservation("'a'"));
		assertEquals("a + 'b'", encodeObservation("a + 'b'"));
		assertEquals("function () {a + 'b'}",
				encodeObservation("function () {a + 'b'}"));
	}

	@Test
	public void testExtractExprContainingBraces() {
		assertEquals("{}", extractExpr("{}; a = 2", 0));
		assertEquals("{{{}}}", extractExpr("{{{}}}; a = 2", 0));
	}

	@Test
	public void testExtractExprContainingDblQuotString() {
		assertEquals("\";\" + a", extractExpr("\";\" + a", 0));
		assertEquals("\"a string\" + a",
				extractExpr("\"a string\" + a; a = 2", 0));
	}

	@Test
	public void testExtractExprContainingSingleDblStringWithEscapedQuotes() {
		assertEquals(
				"\"a \\\"string\\\" containing \\\"\" + a",
				extractExpr("\"a \\\"string\\\" containing \\\"\" + a; a = 2",
						0));
	}

	@Test
	public void testExtractExprContainingSingleQuotString() {
		assertEquals("';' + a", extractExpr("';' + a", 0));
		assertEquals("'a string' + a", extractExpr("'a string' + a; a = 2", 0));
	}

	@Test
	public void testExtractExprContainingSingleQuotStringWithEscapedQuotes() {
		assertEquals("'a \\'string\\' containing \\'' + a",
				extractExpr("'a \\'string\\' containing \\'' + a; a = 2", 0));
	}

	@Test
	public void testExtractExpressionContainingSingleQuoteStrings() {
		final String expr = "#a is '....'";
		assertEquals("$eden_define('a','\\'....\\'')",
				translateExpression(expr));
		assertEquals("$eden_define('a','\\'..\\\\a..\\'')",
				translateExpression("#a is '..\\a..'"));
	}

	@Test
	public void testExtractExprForSimpleInput() {
		assertEquals("", extractExpr("", 0));
		assertEquals("", extractExpr("a", 1));
		assertEquals("a", extractExpr("a", 0));
		assertEquals("a b c", extractExpr("a b c", 0));
	}

	@Test
	public void testExtractExprFromWithinFunction() {
		assertEquals("b + c", extractExpr("function() {a is b + c; ...}", 17));
		assertEquals("b + c", extractExpr("function() {a is b + c}", 17));
	}

	@Test
	public void testExtractExprIgnoresClosingBracesInDoubleString() {
		assertEquals("{ + \"}\";...", extractExpr("{ + \"}\";...", 0));
	}

	@Test
	public void testExtractExprIgnoresClosingBracesInSingleString() {
		assertEquals("{ + '}';...", extractExpr("{ + '}';...", 0));
	}

	@Test
	public void testExtractExprIgnoresNewLineInBraces() {
		assertEquals("{a;\n" + "a}", extractExpr("{a;\na}; a = 2", 0));
		assertEquals("{a;\n}", extractExpr("{a;\n}\na}; a = 2", 0));
	}

	@Test
	public void testExtractExprIgnoresOpeningBracesInDoubleString() {
		assertEquals("\"{\" + a", extractExpr("\"{\" + a;}", 0));
	}

	@Test
	public void testExtractExprIgnoresOpeningBracesInSingleString() {
		assertEquals("'{' + a", extractExpr("'{' + a;}", 0));
	}

	@Test
	public void testExtractExprIgnoresSemiColonInBraces() {
		assertEquals("{a;a}", extractExpr("{a;a}; a = 2", 0));
		assertEquals("{{{{{{{{{a;a};};};};};};};};}",
				extractExpr("{{{{{{{{{a;a};};};};};};};};}; a = 2", 0));
	}

	@Test
	public void testExtractExprProcessesE4XString() {
		assertEquals("a + <xml>d</xml>", extractExpr("a + <xml>d</xml>", 0));
	}

	@Test
	public void testExtractExprProcessesE4XStringContainingSemi() {
		assertEquals("a + <xml>;</xml>", extractExpr("a + <xml>;</xml>;", 0));
	}

	@Test
	public void testExtractExprProcessesE4XStringTerminatedBySemi() {
		assertEquals("a + <xml>d</xml>", extractExpr("a + <xml>d</xml>;", 0));
	}

	@Test
	public void testExtractExprTerminatedBySemiColonAndNewline() {
		assertEquals("a b", extractExpr("a b; c", 0));
		assertEquals("a b", extractExpr("a b\n c", 0));
		assertEquals("a b c", extractExpr("a b c;\n a v l;;;;", 0));
		assertEquals(" b + c", extractExpr("a is b + c; ...", 4));
	}

	@Test
	public void testExtractSpecialSymbols() {
		assertThat(extractSpecialSymbols("1+2").size(), is(equalTo(0)));
		assertThat(extractSpecialSymbols("1 + 2*34 + 111").size(),
				is(equalTo(0)));
		assertThat(extractSpecialSymbols("#a + 1").size(), is(equalTo(1)));
		assertThat(extractSpecialSymbols("#a + 1"), hasItem("a"));
		assertThat(extractSpecialSymbols("#b + #a"), hasItem("a"));
		assertThat(extractSpecialSymbols("#b + #a"), hasItem("b"));
		assertThat(extractSpecialSymbols("#b * #a"), hasItem("a"));
		assertThat(extractSpecialSymbols("#b * #a"), hasItem("b"));
	}

	@Test
	public void testExtractSpecialSymbolsMatchesNamesWithHash() {
		testForFragment("a101#a");
	}

	@Test
	public void testExtractSpecialSymbolsMatchesNamesWithNumbers() {
		testForFragment("a101");
	}

	@Test
	public void testExtractSpecialSymbolsMatchesNamesWithOtherCharsWhenEscaped() {
		testForEscapedFragment("a");
		testForEscapedFragment("a101");
		testForEscapedFragment("a101#2");
		testForEscapedFragment("http://host/foo/bar_1:10");
		testForEscapedFragment("http://host/foo/bar-baz,a,a,a,1(1234)#1+23+1");
	}

	@Test
	public void testExtractSpecialSymbolsMatchesNamesWithRelUrl() {
		testForFragment("/foo/bar");
	}

	@Test
	public void testExtractSpecialSymbolsMatchesNamesWithUrl() {
		testForFragment("http://host/foo/bar");
	}

	@Test
	public void testExtractSpecialSymbolsMatchesNamesWithUrlAndPort() {
		testForFragment("http://host:8080/foo/bar");
	}

	@Test
	public void testExtractSpecialSymbolsMatchesNamesWithUrlChars() {
		testForFragment("http://host/foo/bar_1");
		testForFragment("http://host/foo/bar_1:10");
	}

	@Test
	public void testExtractSpecialSymbolsMatchesNamesWithUrlPortAndAnchor() {
		testForFragment("http://host:8080/foo/bar#101");
	}

	@Test
	public void testExtractSpecialSymbolsSkipsDblQuoteStrings() {
		final Set<String> symbols = extractSpecialSymbols("\"#b\" + #a");
		assertThat(symbols, hasItem("a"));
		assertThat(symbols.size(), is(equalTo(1)));
	}

	@Test
	public void testExtractSpecialSymbolsSkipsSingleQuoteStrings() {
		final Set<String> symbols = extractSpecialSymbols("'#b' + #a + '#c'");
		assertThat(symbols, hasItem("a"));
		assertThat(symbols.size(), is(equalTo(1)));
	}

	@Test
	public void testFindEndOfExprForSimpleInput() {
		assertEquals(0, findEndOfExpr("", 0));
		assertEquals(1, findEndOfExpr("a", 1));
		assertEquals(1, findEndOfExpr("a", 0));
	}

	@Test
	public void testFindStartAndEndOfExpressionContainingSingleQuoteStrings() {
		List<DefnFragment> fragments = findExprRange("#a is '...'");
		assertEquals(6, fragments.get(0).exprStart);
		assertEquals(11, fragments.get(0).exprEnd);

		final String expr = "#a is '....'";
		fragments = findExprRange(expr);
		final int start = fragments.get(0).exprStart;
		assertEquals(6, start);
		final int end = fragments.get(0).exprEnd;
		assertEquals(12, end);
		assertEquals("'....'", expr.substring(start, end));
	}

	@Test
	public void testFindStartOfDefinitionContainingSingleQuoteStrings() {
		List<DefnFragment> fragments = findExprRange("#a is '...'");
		assertEquals(0, fragments.get(0).start);

		fragments = findExprRange(" #a is '...'");
		assertEquals(1, fragments.get(0).start);

		fragments = findExprRange("asd; #a is '...'");
		assertEquals(5, fragments.get(0).start);
	}

	@Test
	public void testFindStartOfDefinitionForEmpty() {
		final List<DefnFragment> fragments = findExprRange("''");
		assertEquals(0, fragments.size());
	}

	@Test
	public void testFindStarts() {
		assertEquals(0, findExprRange("").size());
		assertEquals(1, findExprRange("#a is b;").size());
		assertEquals(2, findExprRange("#a is b; #c is d;").size());

		assertEquals(0, (Object) findExprRange("#a is b;").get(0).start);
		assertEquals(0,
				(Object) findExprRange("#a is b; #c is d;").get(0).start);
		assertEquals(9,
				(Object) findExprRange("#a is b; #c is d;").get(1).start);
	}

	@Test
	public void testFindStartsWhenIsExistsInString() {
		final List<DefnFragment> normal = findExprRange("#a is b; #c is d;");
		assertEquals(2, normal.size());

		final List<DefnFragment> withSingleQuotes = findExprRange("#a is b; a = '#c is d'");
		assertEquals(1, withSingleQuotes.size());
		assertEquals(0, (Object) withSingleQuotes.get(0).start);
		assertEquals(6, (Object) withSingleQuotes.get(0).exprStart);

		final List<DefnFragment> withDoubleQuotes = findExprRange("#a is b; a = \"#c is d\"");
		assertEquals(1, withDoubleQuotes.size());
		assertEquals(0, (Object) withDoubleQuotes.get(0).start);
		assertEquals(6, (Object) withDoubleQuotes.get(0).exprStart);

		final List<DefnFragment> withMultiple = findExprRange("#a is b; #b is 2;");
		assertEquals(2, withMultiple.size());
		assertEquals(0, (Object) withMultiple.get(0).start);
		assertEquals(6, (Object) withMultiple.get(0).exprStart);
		assertEquals(9, (Object) withMultiple.get(1).start);
		assertEquals(15, (Object) withMultiple.get(1).exprStart);

	}

	private void testForEscapedFragment(String fragment) {
		final Set<String> symbols = extractSpecialSymbols("1 + #{" + fragment
				+ "} + 101");
		assertThat(symbols, hasItem(fragment));
		assertThat(symbols.size(), is(equalTo(1)));
	}

	private void testForFragment(String fragment) {
		final Set<String> symbols = extractSpecialSymbols("1 + #" + fragment
				+ " + 101");
		assertThat(symbols, hasItem(fragment));
		assertThat(symbols.size(), is(equalTo(1)));
	}

	@Test
	public void testPattern() {
		assertEquals("b+c", SPECIALNAME_PATTERN.matcher("b+c")
				.replaceAll("foo"));
		assertEquals(" foo+c",
				SPECIALNAME_PATTERN.matcher(" #b+c").replaceAll("foo"));
		assertEquals("foo+c",
				SPECIALNAME_PATTERN.matcher("#b+c").replaceAll("foo"));
		assertEquals("#foo+c",
				SPECIALNAME_PATTERN.matcher("##b+c").replaceAll("foo"));
		assertEquals("#{b+c",
				SPECIALNAME_PATTERN.matcher("#{b+c").replaceAll("foo"));
		assertEquals("b+foo",
				SPECIALNAME_PATTERN.matcher("b+#c").replaceAll("foo"));
	}

	@Test
	public void testPullOutRegions_baseCase() {
		final List<String> stringList = pullOutRegions("");
		assertEquals(0, stringList.size());
	}

	@Test
	public void testPullOutRegions_doubleQuotesInsideSingleQuotes() {
		final List<String> stringList = pullOutRegions("a 'b \"b\" b' c");
		assertEquals(3, stringList.size());
		assertEquals("a ", stringList.get(0));
		assertEquals("'b \"b\" b'", stringList.get(1));
		assertEquals(" c", stringList.get(2));
	}

	@Test
	public void testPullOutRegions_e4x() {
		final List<String> stringList = pullOutRegions("a <xml>b</xml> c");
		assertEquals(3, stringList.size());
		assertEquals("a ", stringList.get(0));
		assertEquals("<xml>b</xml>", stringList.get(1));
		assertEquals(" c", stringList.get(2));
	}

	@Test
	public void testPullOutRegions_e4x2() {
		final List<String> stringList = pullOutRegions("a <xml/> c");
		assertEquals(3, stringList.size());
		assertEquals("a ", stringList.get(0));
		assertEquals("<xml/>", stringList.get(1));
		assertEquals(" c", stringList.get(2));
	}

	@Test
	public void testPullOutRegions_e4XWithNestedExpr() {
		final List<String> stringList = pullOutRegions("<xml><foo>{#a}</foo><bar>{#{a} + 'zy'}</bar></xml>");
		assertEquals(1, stringList.size());
	}

	@Test
	public void testPullOutRegions_escapedDoubleQuotes() {
		final List<String> stringList = pullOutRegions("a \"b \\\"b\\\" b\" c");
		assertEquals(3, stringList.size());
		assertEquals("a ", stringList.get(0));
		assertEquals("\"b \\\"b\\\" b\"", stringList.get(1));
		assertEquals(" c", stringList.get(2));
	}

	@Test
	public void testPullOutRegions_escapedSingleQuotes() {
		final List<String> stringList = pullOutRegions("a 'b \\'b\\' b' c");
		assertEquals(3, stringList.size());
		assertEquals("a ", stringList.get(0));
		assertEquals("'b \\'b\\' b'", stringList.get(1));
		assertEquals(" c", stringList.get(2));
	}

	@Test
	public void testPullOutRegions_simple() {
		final List<String> stringList = pullOutRegions("abcdefg");
		assertEquals(1, stringList.size());
		assertEquals("abcdefg", stringList.get(0));
	}

	@Test
	public void testPullOutRegions_simpleWithDblQuotedText() {
		final List<String> stringList = pullOutRegions("abc \"de\"fg");
		assertEquals(3, stringList.size());
		assertEquals("abc ", stringList.get(0));
		assertEquals("\"de\"", stringList.get(1));
		assertEquals("fg", stringList.get(2));
	}

	@Test
	public void testPullOutRegions_simpleWithSingleQuotedText() {
		final List<String> stringList = pullOutRegions("abc 'de'fg");
		assertEquals(3, stringList.size());
		assertEquals("abc ", stringList.get(0));
		assertEquals("'de'", stringList.get(1));
		assertEquals("fg", stringList.get(2));
	}

	@Test
	public void testPullOutRegions_simpleWithSpaces() {
		final List<String> stringList = pullOutRegions("abc defg");
		assertEquals(1, stringList.size());
		assertEquals("abc defg", stringList.get(0));
	}

	@Test
	public void testPullOutRegions_singleQuotesInsideDoubleQuotes() {
		final List<String> stringList = pullOutRegions("a \"b 'b' b\" c");
		assertEquals(3, stringList.size());
		assertEquals("a ", stringList.get(0));
		assertEquals("\"b 'b' b\"", stringList.get(1));
		assertEquals(" c", stringList.get(2));
	}

	@Test
	public void testPullOutRegions_xmlInString() {
		final List<String> stringList = pullOutRegions("a '<xml>b</xml>' c");
		assertEquals(3, stringList.size());
		assertEquals("a ", stringList.get(0));
		assertEquals("'<xml>b</xml>'", stringList.get(1));
		assertEquals(" c", stringList.get(2));
	}

	@Test
	public void testTranslateE4XObservationTranslatesExpressions() {
		assertEquals("{$eden_observe('a') + c}",
				translateE4XObservation("{#a + c}"));
	}

	@Test
	public void testTranslateE4XObservationWhenNoSubs() {
		assertEquals("", translateE4XObservation(""));
		assertEquals("abc", translateE4XObservation("abc"));
		assertEquals("{abc}", translateE4XObservation("{abc}"));
		assertEquals("#a + c", translateE4XObservation("#a + c"));
	}

}
