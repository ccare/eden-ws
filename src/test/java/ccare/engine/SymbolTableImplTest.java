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

package ccare.engine;

import org.junit.Test;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Undefined;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * User: carecx
 * Date: 31-Oct-2010
 * Time: 12:31:46
 */
public class SymbolTableImplTest {


    final SymbolReference a = new SymbolReference("a");
    final SymbolReference b = new SymbolReference("b");
    final SymbolReference c = new SymbolReference("c");
    final SymbolReference d = new SymbolReference("d");
    final SymbolReference e = new SymbolReference("e");
    final SymbolReference f = new SymbolReference("f");

    @Test
    public void testDependencyManagementUsingLowLevel() {
        SymbolTableImpl table = new SymbolTableImpl();

        Symbol sA = new SymbolImpl(a);
        Symbol sB = new SymbolImpl(b);
        Symbol sC = new SymbolImpl(c);

        table.add(sA);
        table.add(sB);
        table.add(sC);

        table.get(a).redefine(new JavaScriptDefinition("#b + #c"), table);
        table.get(b).redefine(new JavaScriptDefinition("1"), table);
        table.get(c).redefine(new JavaScriptDefinition("2"), table);

        assertEquals(1.0, table.get(b).getValue(table));
        assertEquals(2, table.get(c).getValue(table));
        assertEquals(3.0, table.get(a).getValue(table));

        table.get(b).redefine(new JavaScriptDefinition("2"), table);
        assertEquals(4.0, table.get(a).getValue(table));

        table.get(b).redefine(new JavaScriptDefinition("3"), table);
        assertEquals(5.0, table.get(a).getValue(table));
    }

    @Test
    public void testScript() {
        SymbolTable table = new SymbolTableImpl();

        table.define(a, "#b + #c");
        table.define(b, "1");
        table.define(c, "2");

        assertEquals(3.0, table.getValue(a));
        assertEquals(1.0, table.getValue(b));
        assertEquals(2, table.getValue(c));

        table.define(b, "2");
        assertEquals(4.0, table.getValue(a));

        table.define(b, "3");
        assertEquals(5.0, table.getValue(a));
    }

    @Test
    public void testDeepChaining() {
        SymbolTable table = new SymbolTableImpl();

        table.define(a, "1");
        table.define(b, "#a + 1");
        table.define(c, "#b + 1");
        table.define(d, "#c + 1");
        table.define(e, "#d + 1");
        table.define(f, "#e + 1");

        assertEquals(6.0, table.getValue(f));

        table.define(a, "2");
        assertEquals(7.0, table.getValue(f));
    }


    @Test
    public void testStringConcat() {
        SymbolTable table = new SymbolTableImpl();

        table.define(a, "1");
        table.define(b, "#a + '1'");

        assertEquals("11", table.getValue(b));

        table.define(a, "2");
        assertEquals("21", table.getValue(b));
    }

    @Test
    public void testStringsContainingMagicChar() {
        SymbolTable table = new SymbolTableImpl();

        table.define(b, "'#a'");

        assertEquals("#a", table.getValue(b));
    }

    @Test
    public void testStringsContainingMagicCharInsideProc() {
        SymbolTable table = new SymbolTableImpl();

        table.defineFunction(b, "function() { #a is '#c' }");
        table.execute(b);

        assertEquals("#c", table.getValue(a));
    }

    @Test
    public void testStringsContainingMagicCharInsideProc_doubleQuotes() {
        SymbolTable table = new SymbolTableImpl();

        table.defineFunction(b, "function() { #a is \"#c\" }");
        table.execute(b);

        assertEquals("#c", table.getValue(a));
    }

    @Test
    public void testStringsContainingMagicCharInsideProcAndEndedBySemiColon() {
        SymbolTable table = new SymbolTableImpl();

        table.defineFunction(b, "function() { #a is '#c'; }");
        table.execute(b);

        assertEquals("#c", table.getValue(a));
    }

    @Test
    public void testStringsContainingMagicCharInsideProc_doubleQuotesAndEndedBySemiColon() {
        SymbolTable table = new SymbolTableImpl();

        table.defineFunction(b, "function() { #a is \"#c\"; }");
        table.execute(b);

        assertEquals("#c", table.getValue(a));
    }

    @Test
    public void testStringsContainingDependencyDefnInsideProc() {
        SymbolTable table = new SymbolTableImpl();

        table.defineFunction(b, "function() { #a is '1' }");
        table.execute(b);

        assertEquals("1", table.getValue(a));
    }

    @Test
    public void testStringsContainingDependencyDefnAgainst_A_defaultsTo_HashA() {
        SymbolTable table = new SymbolTableImpl();

        table.defineFunction(b, "function() { a is '1' }");
        table.execute(b);
        assertEquals("1", table.getValue(a));
    }

    @Test
    public void test_Is_InsideAString() {
        SymbolTable table = new SymbolTableImpl();
        table.define(a, "'a is b'");
        assertEquals("a is b", table.getValue(a));
    }

    @Test
    public void test_Is_InsideAString2() {
        SymbolTable table = new SymbolTableImpl();
        table.define(a, "'#a is b'");
        assertEquals("#a is b", table.getValue(a));
    }

    @Test
    public void test_Is_InsideAFunction() {
        SymbolTable table = new SymbolTableImpl();
        table.defineFunction(a, "function() { return 'a is b'}");
        table.define(b, "#a()");
        assertEquals("a is b", table.getValue(b));
    }

    @Test
    public void test_Is_InsideAComplexFunction() {
        SymbolTable table = new SymbolTableImpl();

        table.defineFunction(a, "function() { #c is 4; return 'a is b'}");
        table.define(b, "#a()");

        assertEquals("a is b", table.getValue(b));
        assertEquals(4, table.getValue(c));
    }

    @Test
    public void test_Is_InsideAComplexFunction2() {
        SymbolTable table = new SymbolTableImpl();

        table.defineFunction(f, "function() { #a is 'b'; var a = \"#c is d\"; var a = ({ a : '#a is'}); #b is 2; return '#d is 5'}");
        table.define(c, "#f()");

        assertEquals("#d is 5", table.getValue(c));

        assertEquals("b", table.getValue(a));
        assertEquals(2, table.getValue(b));
    }

    @Test
    public void testArrayValues() {
        SymbolTable table = new SymbolTableImpl();
        table.define(a, "1");
        table.define(b, "[#a]");

        NativeArray arr = (NativeArray) table.getValue(b);
        assertEquals(1.0, arr.get(0, arr));

        table.define(a, "2");

        arr = (NativeArray) table.getValue(b);
        assertEquals(2, arr.get(0, arr));
    }

    @Test
    public void testArraysConcat() {
        SymbolTable table = new SymbolTableImpl();
        table.define(a, "[1,2,3]");
        table.define(b, "[4,5,6]");
        table.define(c, "#a.concat(#b)");
        table.define(d, "#c.length");

        assertEquals(6.0, table.getValue(d));

        table.define(a, "[1,2]");

        assertEquals(5.0, table.getValue(d));
    }

    @Test
    public void testSimpleFunctionCall() {
        SymbolTable table = new SymbolTableImpl();
        table.defineFunction(a, "function() { return 2; }");
        table.define(b, "#a()");
        assertEquals(2, table.getValue(b));
    }

    @Test
    public void testDependencyFunctionCall() {
        SymbolTable table = new SymbolTableImpl();
        table.define(a, "15");
        table.defineFunction(b, "function() { return #a*2; }");
        table.define(c, "#b()");
        assertEquals(30.0, table.getValue(c));
        table.define(a, "2");
        assertEquals(4.0, table.getValue(c));
    }

    @Test
    public void testTrinery() {
        SymbolTable table = new SymbolTableImpl();
        table.define(a, "4");
        table.define(b, "(#a == 4) ? 1 : 2");
        assertEquals(1.0, table.getValue(b));
        table.define(a, "5");
        assertEquals(2, table.getValue(b));
    }

    @Test
    public void testFuncWithSideEffect() {
        SymbolTable table = new SymbolTableImpl();
        table.defineFunction(a, "function() { $eden_define('b','3'); }");
        table.define(c, "#a()");
        table.getValue(c);
        assertEquals(3, table.getValue(b));
    }

    @Test
    public void testExecuteFuncWithSideEffect() {
        SymbolTable table = new SymbolTableImpl();
        table.defineFunction(a, "function() { $eden_define('b','3'); }");
        table.execute(a);
        assertEquals(3, table.getValue(b));
    }

    @Test
    public void testExecuteFuncWithInnerDefinition() {
        SymbolTable table = new SymbolTableImpl();
        table.defineFunction(a, "function() { #b is 3; }");
        table.execute(a);
        assertEquals(3, table.getValue(b));
    }

    @Test
    public void testExecuteFuncWithInnerDefinitionCreatingDependency() {
        SymbolTable table = new SymbolTableImpl();
        table.define(c, "45");
        table.defineFunction(a, "function() { #b is #c + 1; }");
        table.execute(a);
        assertEquals(46.0, table.getValue(b));
        table.define(c, "2");
        assertEquals(3.0, table.getValue(b));
    }

    @Test
    public void testTrigger() {
        SymbolTable table = new SymbolTableImpl();
        table.defineTriggeredProc(a, "function() { $eden_define('b','3');}", "#c");
        assertEquals(null, table.getValue(b));
        table.define(c, "101");
        assertEquals(3, table.getValue(b));
        table.define(b, "2");
        assertEquals(2, table.getValue(b));
        table.define(c, "'a'");
        assertEquals(3, table.getValue(b));

        // Remove the trigger and verify that the linkage is broken
        table.defineTriggeredProc(a, "function() { $eden_define('b','3');}");
        table.define(b, "2");
        assertEquals(2, table.getValue(b));
        table.define(c, "'a'");
        assertEquals(2, table.getValue(b));
    }

    @Test
    public void testDefineObject() {
        SymbolTable table = new SymbolTableImpl();
        table.define(a, "({})");
    }

    @Test
    public void testDefineObjectWithProp() {
        SymbolTable table = new SymbolTableImpl();
        table.define(a, "({a: 2})");
        table.define(b, "#a.a");
        assertEquals(2, table.getValue(b));
    }

    @Test
    public void testObjectWithDependentFields() {
        SymbolTable table = new SymbolTableImpl();
        table.define(a, "'hi'");
        table.define(b, "'there'");
        table.define(c, "({a: #a, b: #b})");
        table.define(d, "#c.a");
        table.define(e, "#c.b");

        assertEquals("hi", table.getValue(d));
        assertEquals("there", table.getValue(e));

        table.define(a, "'bye'");
        table.define(b, "'then'");

        assertEquals("bye", table.getValue(d));
        assertEquals("then", table.getValue(e));
    }

    @Test
    public void testRedefinitionOfObjectBreaksDependency() {
        SymbolTable table = new SymbolTableImpl();
        table.define(a, "({a: 2})");
        table.define(b, "#a.a");
        assertEquals(2, table.getValue(b));
        table.define(a, "({})");
        assertEquals(Undefined.instance, table.getValue(b));
        table.define(a, "({b:12, a: [1,2,3]})");
        final Object val = table.getValue(b);
        assertTrue(val instanceof NativeArray);
        final NativeArray arr = (NativeArray) val;
        assertEquals(1.0, arr.get(0, arr));
        assertEquals(2, arr.get(1, arr));
        assertEquals(3, arr.get(2, arr));
    }

    @Test
    public void testE4X() {
        SymbolTable table = new SymbolTableImpl();
        table.define(a, "<xml><foo>bar</foo></xml>");
        table.define(b, "#a.foo.toString()");
        assertEquals("bar", table.getValue(b));
    }

    @Test
    public void testE4XWithEmbeddedDependency() {
        SymbolTable table = new SymbolTableImpl();
        table.define(a, "'baz'");
        table.define(b, "<xml><foo>{#a}</foo><bar>{#{a} + 'zy'}</bar></xml>");
        table.define(c, "#b.foo.toString()");
        table.define(d, "#b.bar.toString()");
        assertEquals("baz", table.getValue(c));
        assertEquals("bazzy", table.getValue(d));
    }

         
}
