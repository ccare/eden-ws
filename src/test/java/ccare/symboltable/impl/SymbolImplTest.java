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

import ccare.symboltable.Symbol;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolReference;
import ccare.symboltable.SymbolTable;
import ccare.symboltable.exceptions.CannotForgetException;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Undefined;

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
    SymbolTableImpl symbolTable;

    @Before
    public void setup() {
        defn = createMock(SymbolDefinition.class);
        symbolTable = createMock(SymbolTableImpl.class);
    }

    @Test
    public void testIsUpToDateInitialisesCorrectly() throws Exception {
        SymbolImpl s = new SymbolImpl(new SymbolReference());
        assertFalse(s.isUpToDate());
    }

    @Test
    public void testExpireCachedValueResetsFlag() throws Exception {
        expect(defn.evaluate(symbolTable)).andReturn(new Object());
        expect(defn.getDependencies()).andReturn(Collections.<SymbolReference>emptyList());
        expect(defn.getTriggers()).andReturn(Collections.<SymbolReference>emptyList());
  
        replay(defn);
        replay(symbolTable);

        SymbolImpl s = new SymbolImpl(new SymbolReference());
        s.redefine(defn, symbolTable);
        assertFalse(s.isUpToDate());
        s.getValue(symbolTable);
        assertTrue(s.isUpToDate());
        s.expireValue();
        assertFalse(s.isUpToDate());

        verify(defn);
        verify(symbolTable);
    }

 
    @Test
    public void testRegisterDependent() throws Exception {
        final Symbol dependant = createMock(SymbolImpl.class);
        replay(dependant);
        SymbolImpl s = new SymbolImpl(new SymbolReference());
        s.registerDependent(dependant);
        verify(dependant);
    }

    @Test
    public void testRegisterTrigger() throws Exception {
        final Symbol other = createMock(SymbolImpl.class);
        replay(other);
        SymbolImpl s = new SymbolImpl(new SymbolReference());
        s.registerTrigger(other);
        verify(other);
    }

    @Test
    public void testGetValueDoesntReEvaluateButCaches() throws Exception {
        expect(defn.evaluate(symbolTable)).andReturn(new Object());
        expect(defn.getDependencies()).andReturn(Collections.<SymbolReference>emptyList());
        expect(defn.getTriggers()).andReturn(Collections.<SymbolReference>emptyList());
  
        replay(defn);
        replay(symbolTable);

        SymbolImpl s = new SymbolImpl(new SymbolReference());
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

        replay(defn);
        replay(symbolTable);

        SymbolImpl s = new SymbolImpl(new SymbolReference());
        s.redefine(defn, symbolTable);
        s.getValue(symbolTable);
        s.expireValue();
        s.getValue(symbolTable);
        s.getValue(symbolTable);

        verify(defn);
        verify(symbolTable);
    }


   
    @Test
    public void testForgetSymbolThrowsExceptionIfThereAreDependentValues() {
        final SymbolTableImpl table = new SymbolTableImpl();
        final SymbolReference refA = new SymbolReference("a");
        final SymbolReference refB = new SymbolReference("b");
        final SymbolReference refC = new SymbolReference("c");

        final SymbolImpl a = new SymbolImpl(refA);
        final SymbolImpl b = new SymbolImpl(refB);
        final SymbolImpl c = new SymbolImpl(refC);

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
        assertEquals(Undefined.instance, a.getValue(table));
        assertEquals(Undefined.instance, b.getValue(table));
        assertEquals(Undefined.instance, c.getValue(table));
    }


    private void defineAsNumber(final SymbolTableImpl table, final SymbolImpl b, final Integer val) {
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


    private void defineAsIncrement(SymbolTableImpl table, final SymbolReference refB, SymbolImpl a) {
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
            public boolean isExecutable() {
                return false;
            }

			@Override
			public Object evaluate(SymbolTable t) {
				// TODO Auto-generated method stub
				return null;
			}
        }, table);
    }

}
