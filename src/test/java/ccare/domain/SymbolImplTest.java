package ccare.domain;

import ccare.service.SymbolTableBean;
import ccare.service.SymbolTableService;
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
    SymbolTableService symbolTable;

    @Before
    public void setup() {
        defn = createMock(SymbolDefinition.class);
        symbolTable = createMock(SymbolTableService.class);
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
        final SymbolTableService table = new SymbolTableBean();
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
        final SymbolTableService table = new SymbolTableBean();
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
        final SymbolTableService table = new SymbolTableBean();
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


    private void defineAsNumber(final SymbolTableService table, final Symbol b, final Integer val) {
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


    private void defineAsIncrement(SymbolTableService table, final SymbolReference refB, Symbol a) {
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
