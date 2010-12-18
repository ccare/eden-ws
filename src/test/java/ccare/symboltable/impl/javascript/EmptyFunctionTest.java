package ccare.symboltable.impl.javascript;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class EmptyFunctionTest {
	
	EmptyFunction f = new EmptyFunction() {
		
		@Override
		public Object call(Context arg0, Scriptable arg1, Scriptable arg2,
				Object[] arg3) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	@Test
	public void testConstruct() {
		assertNull(f.construct(createMock(Context.class), createMock(Scriptable.class), new Object[]{}));
	}

	@Test
	public void testGetClassName() {
		assertNull(f.getClassName());
	}

	@Test
	public void testGetScriptable() {
		Scriptable s = createMock(Scriptable.class);
		assertNull(f.get(1, s));
		assertNull(f.get('a', s));
	}

	@Test
	public void testHasScriptable() {
		Scriptable s = createMock(Scriptable.class);
		assertFalse(f.has(1, s));
		assertFalse(f.has('a', s));
	}

	@Test
	public void testPutScriptableObjectDoesntFail() {
		Scriptable s = createMock(Scriptable.class);
		f.put(1, s, new Object());
		f.put('a', s, new Object());
	}

	@Test
	public void testDeleteDoesntThrowException() {
		f.delete(1);
		f.delete(2);
		f.delete(3);
		f.delete('a');
		f.delete('b');
		f.delete('c');
	}

	@Test
	public void testGetPrototype() {
		assertNull(f.getPrototype());
	}

	@Test
	public void testGetParentScope() {
		assertNull(f.getParentScope());
		f.setParentScope(null);
		assertNull(f.getParentScope());
		f.setParentScope(createMock(Scriptable.class));
		assertNull(f.getParentScope());
	}

	@Test
	public void testGetIds() {
		assertNotNull(f.getIds());
		assertEquals(0, f.getIds().length);
	}

	@Test
	public void testGetDefaultValue() {
		assertNull(f.getDefaultValue(null));
	}

	@Test
	public void testHasInstance() {
		assertFalse(f.hasInstance(null));
	}

}
