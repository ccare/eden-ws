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

package ccare.symboltable;

import ccare.domain.CannotForgetException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * User: carecx
 * Date: 15-Oct-2010
 * Time: 20:10:42
 */
public class SymbolImplTest {

    SymbolDefinition defn;
    SymbolTable symbolTable;

    @Before
    public void setup() {
        defn = createMock(SymbolDefinition.class);
        symbolTable = createMock(SymbolTable.class);
    }

    @Test
    public void testIsUpToDateInitialisesCorrectly() throws Exception {
        Symbol s = new SymbolImpl(new SymbolReference());
        assertFalse(s.isUpToDate());
    }

    @Test
    public void testExpireCachedValueResetsFlag() throws Exception {
        expect(defn.evaluate(symbolTable)).andReturn(new Object());
        expect(defn.getDependencies()).andReturn(Collections.<SymbolReference>emptyList());
        expect(defn.getTriggers()).andReturn(Collections.<SymbolReference>emptyList());
        symbolTable.fireTriggers(anyObject(Set.class));

        replay(defn);
        replay(symbolTable);

        Symbol s = new SymbolImpl(new SymbolReference());
        s.redefine(defn, symbolTable);
        assertFalse(s.isUpToDate());
        s.getValue(symbolTable);
        assertTrue(s.isUpToDate());
        s.expireValue();
        assertFalse(s.isUpToDate());

        verify(defn);
        verify(symbolTable);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDependentWhenNotADependent() throws Exception {
        final Symbol dependant = createMock(Symbol.class);
        expect(dependant.isDependentOn(anyObject(Symbol.class))).andReturn(false);
        replay(dependant);
        Symbol s = new SymbolImpl(new SymbolReference());
        s.registerDependent(dependant);
        verify(dependant);
    }

    @Test
    public void testRegisterDependent() throws Exception {
        final Symbol dependant = createMock(Symbol.class);
        expect(dependant.isDependentOn(anyObject(Symbol.class))).andReturn(true);
        replay(dependant);
        Symbol s = new SymbolImpl(new SymbolReference());
        s.registerDependent(dependant);
        verify(dependant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterTriggerWhenNotATrigger() throws Exception {
        final Symbol other = createMock(Symbol.class);
        expect(other.isTriggeredBy(anyObject(Symbol.class))).andReturn(false);
        replay(other);
        Symbol s = new SymbolImpl(new SymbolReference());
        s.registerTrigger(other);
        verify(other);
    }

    @Test
    public void testRegisterTrigger() throws Exception {
        final Symbol other = createMock(Symbol.class);
        expect(other.isTriggeredBy(anyObject(Symbol.class))).andReturn(true);
        replay(other);
        Symbol s = new SymbolImpl(new SymbolReference());
        s.registerTrigger(other);
        verify(other);
    }

    @Test
    public void testGetValueDoesntReEvaluateButCaches() throws Exception {
        expect(defn.evaluate(symbolTable)).andReturn(new Object());
        expect(defn.getDependencies()).andReturn(Collections.<SymbolReference>emptyList());
        expect(defn.getTriggers()).andReturn(Collections.<SymbolReference>emptyList());
        symbolTable.fireTriggers(anyObject(Set.class));

        replay(defn);
        replay(symbolTable);

        Symbol s = new SymbolImpl(new SymbolReference());
        s.redefine(defn, symbolTable);
        s.getValue(symbolTable);
        s.getValue(symbolTable);
        s.getValue(symbolTable);

        verify(defn);
        verify(symbolTable);
    }

    @Test
    public void testGetValueReEvaluatesWhenSymbolValueExpires() throws Exception {
        expect(defn.evaluate(symbolTable)).andReturn(new Object()).times(2);
        expect(defn.getDependencies()).andReturn(Collections.<SymbolReference>emptyList());
        expect(defn.getTriggers()).andReturn(Collections.<SymbolReference>emptyList());
        symbolTable.fireTriggers(anyObject(Set.class));

        replay(defn);
        replay(symbolTable);

        Symbol s = new SymbolImpl(new SymbolReference());
        s.redefine(defn, symbolTable);
        s.getValue(symbolTable);
        s.expireValue();
        s.getValue(symbolTable);
        s.getValue(symbolTable);

        verify(defn);
        verify(symbolTable);
    }

    @Test
    public void testDependentValuesAreReEvaluated() {
        final SymbolTableImpl table = new SymbolTableImpl();
        final SymbolReference refA = new SymbolReference();
        final SymbolReference refB = new SymbolReference();

        final Symbol a = new SymbolImpl(refA);
        final Symbol b = new SymbolImpl(refB);

        table.add(a);
        table.add(b);

        defineAsNumber(table, b, 2);

        assertNotNull(b.getValue(table));
        assertEquals(2, b.getValue(table));

        defineAsIncrement(table, refB, a);    // a is increment of b

        assertNotNull(a.getValue(table));
        assertEquals(3, a.getValue(table));

        defineAsNumber(table, b, 3);
        assertEquals(4, a.getValue(table));

        defineAsNumber(table, b, -1);
        assertEquals(0, a.getValue(table));
    }

    @Test
    public void testDeepDependentValuesAreReEvaluated() {
        final SymbolTableImpl table = new SymbolTableImpl();
        final SymbolReference refA = new SymbolReference();
        final SymbolReference refB = new SymbolReference();
        final SymbolReference refC = new SymbolReference();

        final Symbol a = new SymbolImpl(refA);
        final Symbol b = new SymbolImpl(refB);
        final Symbol c = new SymbolImpl(refC);

        table.add(a);
        table.add(b);
        table.add(c);

        defineAsNumber(table, a, 2);       // a = 2
        defineAsIncrement(table, refA, b); // b = a + 1
        defineAsIncrement(table, refB, c); // c = b + 1

        assertNotNull(a.getValue(table));
        assertNotNull(b.getValue(table));
        assertNotNull(c.getValue(table));

        assertEquals(2, a.getValue(table));
        assertEquals(3, b.getValue(table));
        assertEquals(4, c.getValue(table));

        defineAsNumber(table, a, 3);       // a = 2
        assertEquals(3, a.getValue(table));
        assertEquals(4, b.getValue(table));
        assertEquals(5, c.getValue(table));

        defineAsNumber(table, a, 4);       // a = 2
        assertEquals(6, c.getValue(table));
    }

    @Test
    public void testForgetSymbolThrowsExceptionIfThereAreDependentValues() {
        final SymbolTableImpl table = new SymbolTableImpl();
        final SymbolReference refA = new SymbolReference();
        final SymbolReference refB = new SymbolReference();
        final SymbolReference refC = new SymbolReference();

        final Symbol a = new SymbolImpl(refA);
        final Symbol b = new SymbolImpl(refB);
        final Symbol c = new SymbolImpl(refC);

        table.add(a);
        table.add(b);
        table.add(c);

        defineAsNumber(table, a, 2);       // a = 2
        defineAsIncrement(table, refA, b); // b = a + 1
        defineAsIncrement(table, refB, c); // c = b + 1

        // Forgetting either A or B errors as there are dependent definitions
        try {
            a.forget();
            fail();
        } catch (CannotForgetException e) {
        }
        try {
            b.forget();
            fail();
        } catch (CannotForgetException e) {
        }

        // Forgetting in reverse order works
        try {
            c.forget();
            b.forget();
            a.forget();
        } catch (CannotForgetException e) {
            fail();
        }
        assertNull(a.getValue(table));
        assertNull(b.getValue(table));
        assertNull(c.getValue(table));
    }


    private void defineAsNumber(final SymbolTable table, final Symbol b, final Integer val) {
        b.redefine(new SymbolDefinition() {
            @Override
            public Collection<SymbolReference> getDependencies() {
                return Collections.<SymbolReference>emptyList();
            }

            @Override
            public Collection<SymbolReference> getTriggers() {
                return Collections.<SymbolReference>emptyList();
            }

            @Override
            public Object evaluate(SymbolTable t) {
                return val;

            }

            @Override
            public boolean isExecutable() {
                return false;
            }
        }, table);
    }


    private void defineAsIncrement(SymbolTable table, final SymbolReference refB, Symbol a) {
        a.redefine(new SymbolDefinition() {
            @Override
            public Collection<SymbolReference> getDependencies() {
                return Arrays.asList(new SymbolReference[]{refB});
            }

            @Override
            public Collection<SymbolReference> getTriggers() {
                return Collections.<SymbolReference>emptyList();
            }

            @Override
            public Object evaluate(SymbolTable t) {
                Symbol b = t.get(refB);
                Object value = b.getValue(t);
                return 1 + (Integer) value;

            }

            @Override
            public boolean isExecutable() {
                return false;
            }
        }, table);
    }

}
