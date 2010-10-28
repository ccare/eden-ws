package ccare.domain;

import ccare.service.SymbolTableBean;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static ccare.domain.JavaScriptDefinition.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: carecx
 * Date: 25-Oct-2010
 * Time: 17:07:10
 * To change this template use File | Settings | File Templates.
 */
public class JavaScriptDefinitionTest {

    @Test
    public void testCreate() {
        new JavaScriptDefinition("a+b");
    }

    @Test
    public void testExpressionTranslationBaseCase() {
        validateExprUnchangedFor("a+b");
        validateExprUnchangedFor("1 * 123 + 3 % b");
        validateExprUnchangedFor("\"aa\" + 'b' + c");
    }

    @Test
    public void testExpressionTranslationForExpressionWithSimpleObservables() {
        validateExpr("a+$eden_observe('c')", "a+#c");
        validateExpr("a % $eden_observe('c_c_d')", "a % #c_c_d");
    }


    @Test
    public void testExpressionTranslationForExpressionWithEscapedObservables() {
        validateExpr("$eden_observe('abc')", "#{abc}");
        validateExpr("$eden_observe('http://foo/bar/') * $eden_observe('b')", "#{http://foo/bar/} * #b");
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
        validateExpr("function() {$eden_define('a','b+c')}", "function() {#a is b+c}");
        validateExpr("function() {$eden_define('a','b+c');}", "function() {#a is b+c;}");
        validateExpr("function() {$eden_define('a','[1,2,\"3\"]');}", "function() {#a is [1,2,\"3\"];}");
        validateExpr("function() {$eden_define('a','12')}", "function() {#a is 12}");
    }

    @Test
    public void testExpressionTranslationForDefinitionsInFunctionsSplitsOnSemiColonAndNewline() {
        validateExpr("function() {$eden_define('a','12'); $eden_define('b','5'); return $eden_observe('b')}", "function() {#a is 12; #b is 5; return #b}");
        validateExpr("function() {" +
                "$eden_define('a','12');\n" +
                "$eden_define('b','5');\n" +
                "return $eden_observe('b')\n" +
                "}",
                "function() {" +
                "#a is 12;\n" +
                "#b is 5;\n" +
                "return #b\n}");
        validateExpr("function() {" +
                "$eden_define('a','12')\n" +
                "$eden_define('b','5')\n" +
                "return $eden_observe('b')\n" +
                "}",
                "function() {" +
                "#a is 12\n" +
                "#b is 5\n" +
                "return #b\n}");
    }

    @Test
    public void testExpressionTranslationForDoubleQuoteStrings() {
        validateExpr("function() {$eden_define('a','\"b+c\"');}", "function() {#a is \"b+c\";}");
    }


    @Test
    public void testExpressionTranslationForSingleQuoteStrings() {
        validateExpr("$eden_define('a','\\'...\\'')", "#a is '...'");
    }
    
    @Test
    public void testExpressionTranslationForSingleQuoteStringsInFunctions() {
        validateExpr("function() {$eden_define('a','\\'b+c\\'')}", "function() {#a is 'b+c'}");
        validateExpr("function() {$eden_define('a','\\'b+c\\'');}", "function() {#a is 'b+c';}");
    }


    // TODO: IMPLEMENT THIS
    //@Test(expected = NotImplementedException.class)
    @Test
    public void testExpressionTranslationForObjects() {
        validateExpr("$eden_define('a','({ a: 1})')", "#a is ({ a: 1})");
    }

    // TODO: IMPLEMENT THIS
    //@Test(expected = NotImplementedException.class)
    @Test
    public void testExpressionTranslationForObjectsInFunctions() {
        validateExpr("$eden_define('a','({ a: 1})')", "function() { #a is ({ a: 1}); }");
    }

    @Test
    public void testFindStartOfDefinitionForEmpty() {
        final List<DefnFragment> fragments = findExprRange("''");
        assertEquals(0, fragments.size());
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
    public void testExtractExpressionContainingSingleQuoteStrings() {
        final String expr = "#a is '....'";
        assertEquals("$eden_define('a','\\'....\\'')", translateExpression(expr));
        assertEquals("$eden_define('a','\\'..\\\\a..\\'')", translateExpression("#a is '..\\a..'"));
    }




    @Test
    public void testFindStarts() {
        assertEquals(0, findStarts("").size());
        assertEquals(1, findStarts("#a is b;").size());
        assertEquals(2, findStarts("#a is b; #c is d;").size());

        assertEquals(0, (Object) findStarts("#a is b;").get(0)[0]);
        assertEquals(0, (Object) findStarts("#a is b; #c is d;").get(0)[0]);
        assertEquals(9, (Object) findStarts("#a is b; #c is d;").get(1)[0]);  
    }



    @Test
    public void testFindEndOfExprForSimpleInput() {
        assertEquals(0, JavaScriptDefinition.findEndOfExpr("",0));
        assertEquals(1, JavaScriptDefinition.findEndOfExpr("a",1));
        assertEquals(1, JavaScriptDefinition.findEndOfExpr("a",0));
    }

    @Test
    public void testExtractExprForSimpleInput() {
        assertEquals("", extractExpr("",0));
        assertEquals("", extractExpr("a",1));
        assertEquals("a", extractExpr("a",0));
        assertEquals("a b c", extractExpr("a b c",0));
    }

    @Test
    public void testExtractExprTerminatedBySemiColonAndNewline() {
        assertEquals("a b", extractExpr("a b; c",0));
        assertEquals("a b", extractExpr("a b\n c",0));
        assertEquals("a b c", extractExpr("a b c;\n a v l;;;;",0));
        assertEquals(" b + c", extractExpr("a is b + c; ...",4));
    }

    @Test
    public void testExtractExprContainingSingleQuotString() {
        assertEquals("';' + a", extractExpr("';' + a",0));
        assertEquals("'a string' + a", extractExpr("'a string' + a; a = 2",0));
    }

    @Test
    public void testExtractExprFromWithinFunction() {
        assertEquals("b + c", extractExpr("function() {a is b + c; ...}",17));
        assertEquals("b + c", extractExpr("function() {a is b + c}",17));
    }



    @Test
    public void testExtractExprContainingSingleQuotStringWithEscapedQuotes() {
        assertEquals("'a \\'string\\' containing \\'' + a", extractExpr("'a \\'string\\' containing \\'' + a; a = 2",0));
    }

    @Test
    public void testExtractExprContainingDblQuotString() {
        assertEquals("\";\" + a", extractExpr("\";\" + a",0));
        assertEquals("\"a string\" + a", extractExpr("\"a string\" + a; a = 2",0));
    }

    @Test
    public void testExtractExprContainingSingleDblStringWithEscapedQuotes() {
        assertEquals("\"a \\\"string\\\" containing \\\"\" + a", extractExpr("\"a \\\"string\\\" containing \\\"\" + a; a = 2",0));
    }

    @Test
    public void testExtractExprContainingBraces() {
        assertEquals("{}", extractExpr("{}; a = 2",0));
        assertEquals("{{{}}}", extractExpr("{{{}}}; a = 2",0));
    }

    @Test
    public void testExtractExprIgnoresSemiColonInBraces() {
        assertEquals("{a;a}", extractExpr("{a;a}; a = 2",0));
        assertEquals("{{{{{{{{{a;a};};};};};};};};}", extractExpr("{{{{{{{{{a;a};};};};};};};};}; a = 2",0));
    }

    @Test
    public void testExtractExprIgnoresNewLineInBraces() {
        assertEquals("{a;\n" +
                "a}", extractExpr("{a;\na}; a = 2",0));
        assertEquals("{a;\n}", extractExpr("{a;\n}\na}; a = 2",0));
    }

    @Test
    public void testExtractExprIgnoresOpeningBracesInSingleString() {
        assertEquals("'{' + a", extractExpr("'{' + a;}",0));
    }

    @Test
    public void testExtractExprIgnoresOpeningBracesInDoubleString() {
        assertEquals("\"{\" + a", extractExpr("\"{\" + a;}",0));
    }

    @Test
    public void testExtractExprIgnoresClosingBracesInSingleString() {
        assertEquals("{ + '}';...", extractExpr("{ + '}';...",0));
    }

    @Test
    public void testExtractExprIgnoresClosingBracesInDoubleString() {
        assertEquals("{ + \"}\";...", extractExpr("{ + \"}\";...",0));
    }


    private void validateExprUnchangedFor(final String expr) {
        validateExpr(expr, expr);
    }

    private void validateExpr(final String target, final String expr) {
        final JavaScriptDefinition defn = new JavaScriptDefinition(expr);
        assertEquals(target, defn.getExpr());
    }
    
    @Test
    public void testGetDependenciesAndTriggersForSimpleExpr() throws Exception {
        SymbolDefinition d = new JavaScriptDefinition("1 + 2");
        assertEquals(0, d.getDependencies().size());
        assertEquals(0, d.getTriggers().size());
    }

    @Test
    public void testGetDependenciesForExpressionWithDependency() throws Exception {
        SymbolDefinition d = new JavaScriptDefinition("1 + #x");
        final Collection<SymbolReference> dependencies = d.getDependencies();
        assertEquals(1, dependencies.size());
        assertEquals(0, d.getTriggers().size());
    }

    @Test
    public void testEvaluate() throws Exception {
        SymbolDefinition d = new JavaScriptDefinition("1+2");
        assertEquals(3, d.evaluate(null));
    }

    @Test
    public void testEvaluateE4X() throws Exception {
        SymbolDefinition d = new JavaScriptDefinition("<xml><foo>bar</foo></xml>.foo.toString()");
        assertEquals("bar", d.evaluate(null));
    }

    @Test
    public void testPrintln() throws Exception {
        SymbolDefinition d = new JavaScriptDefinition("java.lang.System.out.println(3)");
        d.evaluate(null);
    }

    @Test
    public void testCallMagicObserveFunctionDelegatesCorrectly() throws Exception {
        final SymbolTable table = new SymbolTableBean();
        final Symbol s = new SymbolImpl(new SymbolReference());
        s.redefine(new JavaScriptDefinition("'abc'"), table);
        final SymbolDefinition d = new JavaScriptDefinition("$eden_observe('a')");
        final SymbolTable t = stubSymbolTable(s);
        assertEquals("abc", d.evaluate(t));
    }

    @Test
    public void testEvaluateDependency() throws Exception {
        final SymbolTable table = new SymbolTableBean();
        final Symbol s = new SymbolImpl(new SymbolReference());
        s.redefine(new JavaScriptDefinition("'abc'"), table);
        final SymbolDefinition d = new JavaScriptDefinition("#a");
        final SymbolTable t = stubSymbolTable(s);
        assertEquals("abc", d.evaluate(t));
    }

    @Test
    public void testEvaluateDependencyExpression() throws Exception {
        final SymbolTable table = new SymbolTableBean();
        final Symbol s = new SymbolImpl(new SymbolReference());
        s.redefine(new JavaScriptDefinition("'abc'"), table);
        final SymbolDefinition d1 = new JavaScriptDefinition("#a + 'def'");
        final SymbolDefinition d2 = new JavaScriptDefinition("#{a} + 'def'");
        final SymbolTable t = stubSymbolTable(s);
        final String target = "abcdef";
        assertEquals(target, d1.evaluate(t));
        assertEquals(target, d2.evaluate(t));
    }


    private SymbolTable stubSymbolTable(final Symbol s) {
        return new SymbolTable() {

            @Override
            public Set<SymbolReference> listSymbols() {
                return null;
            }

            @Override
            public Symbol get(SymbolReference reference) {
                return s;
            }

            @Override
            public void fireTriggers(Set<Symbol> triggers) {

            }

            @Override
            public void define(SymbolReference aRef, String s) {

            }

            @Override
            public Object getValue(SymbolReference bRef) {
                return null;
            }

            @Override
            public void defineFunction(SymbolReference a, String s) {

            }

            @Override
            public void defineTriggeredProc(SymbolReference a, String s, String... triggers) {

            }

            @Override
            public void execute(SymbolReference a) {

            }
        };
    }

    @Test
    public void testExtractSpecialSymbols() {
        assertThat(JavaScriptDefinition.extractSpecialSymbols("1+2").size(), is(equalTo(0)));
        assertThat(JavaScriptDefinition.extractSpecialSymbols("1 + 2*34 + 111").size(), is(equalTo(0)));
        assertThat(JavaScriptDefinition.extractSpecialSymbols("#a + 1").size(), is(equalTo(1)));
        assertThat(JavaScriptDefinition.extractSpecialSymbols("#a + 1"), hasItem("a"));
        assertThat(JavaScriptDefinition.extractSpecialSymbols("#b + #a"), hasItem("a"));
        assertThat(JavaScriptDefinition.extractSpecialSymbols("#b + #a"), hasItem("b"));
        assertThat(JavaScriptDefinition.extractSpecialSymbols("#b * #a"), hasItem("a"));
        assertThat(JavaScriptDefinition.extractSpecialSymbols("#b * #a"), hasItem("b"));
    }

    @Test
    public void testExtractSpecialSymbolsSkipsDblQuoteStrings() {
        final Set<String> symbols = JavaScriptDefinition.extractSpecialSymbols("\"#b\" + #a");
        assertThat(symbols, hasItem("a"));
        assertThat(symbols.size(), is(equalTo(1)));
    }

    @Test
    public void testExtractSpecialSymbolsSkipsSingleQuoteStrings() {
        final Set<String> symbols = JavaScriptDefinition.extractSpecialSymbols("'#b' + #a + '#c'");
        assertThat(symbols, hasItem("a"));
        assertThat(symbols.size(), is(equalTo(1)));
    }

    @Test
    public void testExtractSpecialSymbolsMatchesNamesWithNumbers() {
        testForFragment("a101");
    }

    @Test
    public void testExtractSpecialSymbolsMatchesNamesWithHash() {
        testForFragment("a101#a");
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
    public void testExtractSpecialSymbolsMatchesNamesWithUrlPortAndAnchor() {
        testForFragment("http://host:8080/foo/bar#101");
    }

    @Test
    public void testExtractSpecialSymbolsMatchesNamesWithUrlChars() {
        testForFragment("http://host/foo/bar_1");
        testForFragment("http://host/foo/bar_1:10");
    }

    @Test
    public void testExtractSpecialSymbolsMatchesNamesWithOtherCharsWhenEscaped() {
        testForEscapedFragment("a");
        testForEscapedFragment("a101");
        testForEscapedFragment("a101#2");
        testForEscapedFragment("http://host/foo/bar_1:10");
        testForEscapedFragment("http://host/foo/bar-baz,a,a,a,1(1234)#1+23+1");
    }

    private void testForFragment(String fragment) {
        final Set<String> symbols = JavaScriptDefinition.extractSpecialSymbols("1 + #" + fragment + " + 101");
        assertThat(symbols, hasItem(fragment));
        assertThat(symbols.size(), is(equalTo(1)));
    }

    private void testForEscapedFragment(String fragment) {
        final Set<String> symbols = JavaScriptDefinition.extractSpecialSymbols("1 + #{" + fragment + "} + 101");
        assertThat(symbols, hasItem(fragment));
        assertThat(symbols.size(), is(equalTo(1)));
    }


}
