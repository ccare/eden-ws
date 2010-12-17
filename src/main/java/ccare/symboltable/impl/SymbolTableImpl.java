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
import ccare.symboltable.SymbolReference;
import ccare.symboltable.SymbolTable;
import ccare.symboltable.exceptions.CannotDefineException;
import ccare.symboltable.exceptions.SymbolTableException;
import ccare.symboltable.impl.javascript.Definition;
import ccare.symboltable.maintainers.DependencyMaintainer;
import ccare.symboltable.maintainers.MarkOutOfDateMaintainer;
import ccare.symboltable.maintainers.TriggeredProcScheduler;

import org.mozilla.javascript.Undefined;

import java.lang.management.ManagementFactory;
import java.util.*;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

public class SymbolTableImpl extends NotificationBroadcasterSupport implements SymbolTable {

    private String name;
    private final UUID id = UUID.randomUUID();
    private Map<SymbolReference, SymbolImpl> symbols = new HashMap<SymbolReference, SymbolImpl>();
	private DependencyMaintainer syncMaintainer = new MarkOutOfDateMaintainer();
	private DependencyMaintainer asyncMaintainer = new TriggeredProcScheduler();

    public SymbolTableImpl() {
		registerAsMXBean();
	}
    
    private void registerAsMXBean() {
    	MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    	ObjectName beanName;
		try {
			beanName = new ObjectName("ccare.symboltable:type=SymbolTable,name="+id.toString());
	        mbs.registerMBean(this, beanName);
		} catch (MalformedObjectNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstanceAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@Override
    public UUID getId() {
        return id;
    }

    public void add(SymbolImpl sym) {
        symbols.put(sym.getReference(), sym);
    }

    public SymbolImpl get(SymbolReference reference) {
        if (!symbols.containsKey(reference)) {
            add(new SymbolImpl(reference));
        }
        return symbols.get(reference);
    }

    @Override
    public Set<SymbolReference> getSymbols() {
        return Collections.unmodifiableSet(symbols.keySet());
    }

    public void fireTriggers(Set<Symbol> set) {
        for (Symbol s : set) {
            this.execute(s.getReference());
        }
    }

    @Override
    public void define(SymbolReference ref, String defn) {
        if (ref == null || defn == null) {
            throw new CannotDefineException();
        }
        if (defn.trim().startsWith("function")) {
            defineFunction(ref, defn);
        } else {
            final Definition d = new Definition(defn);
            doRedefine(ref, d);
        }
    }

	private void doRedefine(SymbolReference ref, final Definition d) {
		SymbolImpl s = get(ref);
		asyncMaintainer.beforeRedefinition(this, s, d);
		syncMaintainer.beforeRedefinition(this, s, d);
		s.redefine(d, this);
		syncMaintainer.afterRedefinition(this, s);
		asyncMaintainer.afterRedefinition(this, s);
	}
	
	

    @Override
    public Object getValue(SymbolReference ref) {
        SymbolImpl s = get(ref);
        try {
            return s.getValue(this);
        } catch (SymbolTableException e) {
            e.printStackTrace();
            // TODO: Log these...
        }
        return Undefined.instance;
    }

    @Override
    public void defineFunction(SymbolReference ref, String defn) {
        final Definition d = new Definition(defn, Definition.ExprType.FUNCTION);
        doRedefine(ref, d);
    }

    @Override
    public void defineTriggeredProc(SymbolReference ref, String defn, String... triggers) {
        final Definition d = new Definition(defn, Definition.ExprType.FUNCTION, triggers);
        doRedefine(ref, d);
    }

    @Override
    public Object execute(SymbolReference a) {
        final SymbolReference r = new SymbolReference("____DUMMY_REF");
        define(r, "#{" + a.getName() + "}()");
        return getValue(r);
    }

    @Override
    public Object execute(SymbolReference a, Object... params) {
        final SymbolReference r = new SymbolReference("____DUMMY_REF");
        define(r, "#{" + a.getName() + "}(" + encodeParams(params) + ")");
        return getValue(r);
    }

    private String encodeParams(Object... params) {
        if (params.length == 0) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(params[0]);
            for (int i = 1; i < params.length; i++) {
                sb.append(",");
                sb.append(params[i]);
            }
            return sb.toString();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Object evaluate(String s) {
        final SymbolReference r = new SymbolReference("____DUMMY_REF");
        define(r, s);
        return getValue(r);
    }

	@Override
	public int getSymbolCount() {
		return this.getSymbols().size();
	}

	@Override
	public String evaluateString(String expression) {
		Object o = evaluate(expression);
		return (o == null) ? "null" : o.toString();
	}
}