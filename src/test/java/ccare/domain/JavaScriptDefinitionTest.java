package ccare.domain;

import ccare.service.SymbolTableBean;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

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