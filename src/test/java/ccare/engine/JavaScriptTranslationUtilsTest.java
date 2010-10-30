package ccare.engine;

import ccare.domain.JavaScriptDefinition;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static ccare.engine.JavaScriptTranslationUtils.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

/**
 * User: carecx
 * Date: 30-Oct-2010
 * Time: 16:19:41
 */
public class JavaScriptTranslationUtilsTest {


    @Test
    public void testPattern() {
       assertEquals("b+c", SPECIALNAME_PATTERN.matcher("b+c").replaceAll("foo"));
       assertEquals(" foo+c", SPECIALNAME_PATTERN.matcher(" #b+c").replaceAll("foo"));
       assertEquals("foo+c", SPECIALNAME_PATTERN.matcher("#b+c").replaceAll("foo"));
       assertEquals("#foo+c", SPECIALNAME_PATTERN.matcher("##b+c").replaceAll("foo"));
       assertEquals("#{b+c", SPECIALNAME_PATTERN.matcher("#{b+c").replaceAll("foo"));
       assertEquals("b+foo", SPECIALNAME_PATTERN.matcher("b+#c").replaceAll("foo"));
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
       public void testFindStartsWhenIsExistsInString() {
           final List<Integer[]> normal = findStarts("#a is b; #c is d;");
           assertEquals(2, normal.size());

           final List<Integer[]> withSingleQuotes = findStarts("#a is b; a = '#c is d'");
           assertEquals(1, withSingleQuotes.size());
           assertEquals(0, (Object) withSingleQuotes.get(0)[0]);
           assertEquals(6, (Object) withSingleQuotes.get(0)[1]);

           final List<Integer[]> withDoubleQuotes = findStarts("#a is b; a = \"#c is d\"");
           assertEquals(1, withDoubleQuotes.size());
           assertEquals(0, (Object) withDoubleQuotes.get(0)[0]);
           assertEquals(6, (Object) withDoubleQuotes.get(0)[1]);

           final List<Integer[]> withMultiple = findStarts("#a is b; #b is 2;");
           assertEquals(2, withMultiple.size());
           assertEquals(0, (Object) withMultiple.get(0)[0]);
           assertEquals(6, (Object) withMultiple.get(0)[1]);
           assertEquals(9, (Object) withMultiple.get(1)[0]);
           assertEquals(15, (Object) withMultiple.get(1)[1]);

           //final List<Integer[]> withMultiple = findStarts("#a is b; a = \"#c is d\"; a = ({ a : '#a is'}); #b is 2; '#d is 5'");

       }





       @Test
       public void testFindEndOfExprForSimpleInput() {
           assertEquals(0, findEndOfExpr("",0));
           assertEquals(1, findEndOfExpr("a",1));
           assertEquals(1, findEndOfExpr("a",0));
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



     @Test
    public void testExtractSpecialSymbols() {
        assertThat(extractSpecialSymbols("1+2").size(), is(equalTo(0)));
        assertThat(extractSpecialSymbols("1 + 2*34 + 111").size(), is(equalTo(0)));
        assertThat(extractSpecialSymbols("#a + 1").size(), is(equalTo(1)));
        assertThat(extractSpecialSymbols("#a + 1"), hasItem("a"));
        assertThat(extractSpecialSymbols("#b + #a"), hasItem("a"));
        assertThat(extractSpecialSymbols("#b + #a"), hasItem("b"));
        assertThat(extractSpecialSymbols("#b * #a"), hasItem("a"));
        assertThat(extractSpecialSymbols("#b * #a"), hasItem("b"));
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
        final Set<String> symbols = extractSpecialSymbols("1 + #" + fragment + " + 101");
        assertThat(symbols, hasItem(fragment));
        assertThat(symbols.size(), is(equalTo(1)));
    }

    private void testForEscapedFragment(String fragment) {
        final Set<String> symbols = extractSpecialSymbols("1 + #{" + fragment + "} + 101");
        assertThat(symbols, hasItem(fragment));
        assertThat(symbols.size(), is(equalTo(1)));
    }


    @Test
    public void testEncodeObservation_simple() {
        assertEquals("a", encodeObservation("a"));
        assertEquals("a + b", encodeObservation("a + b"));
        assertEquals("function () {a + b}", encodeObservation("function () {a + b}"));
    }

    @Test
    public void testEncodeObservation_withStrings() {
        assertEquals("'a'", encodeObservation("'a'"));
        assertEquals("a + 'b'", encodeObservation("a + 'b'"));
        assertEquals("function () {a + 'b'}", encodeObservation("function () {a + 'b'}"));
    }

    @Test
    public void testEncodeObservation_withObservable() {
        assertEquals("$eden_observe('a')", encodeObservation("#a"));
        assertEquals("a + $eden_observe('b')", encodeObservation("a + #b"));
        assertEquals("function () {a + $eden_observe('b')}", encodeObservation("function () {a + #b}"));
    }

    @Test
    public void testEncodeObservation_withObservableNameInString() {
        assertEquals("'#a'", encodeObservation("'#a'"));
    }

    @Test
    public void testPullOutRegions_baseCase() {
        final List<String> stringList = pullOutRegions("");
        assertEquals(1, stringList.size());
        assertEquals("", stringList.get(0));
    }

    @Test
    public void testPullOutRegions_simple() {
        final List<String> stringList = pullOutRegions("abcdefg");
        assertEquals(1, stringList.size());
        assertEquals("abcdefg", stringList.get(0));
    }

    @Test
    public void testPullOutRegions_simpleWithSpaces() {
        final List<String> stringList = pullOutRegions("abc defg");
        assertEquals(1, stringList.size());
        assertEquals("abc defg", stringList.get(0));
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
    public void testPullOutRegions_singleQuotesInsideDoubleQuotes() {
        final List<String> stringList = pullOutRegions("a \"b 'b' b\" c");
        assertEquals(3, stringList.size());
        assertEquals("a ", stringList.get(0));
        assertEquals("\"b 'b' b\"", stringList.get(1));
        assertEquals(" c", stringList.get(2));
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


}
