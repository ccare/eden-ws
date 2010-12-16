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
import org.mozilla.javascript.Undefined;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.management.NotificationBroadcasterSupport;

/**
 * User: carecx
 * Date: 13-Oct-2010
 * Time: 23:36:43
 */
public class SymbolImpl implements Symbol {
	
	
    private final SymbolReference ref;
    private SymbolDefinition definition;
    private SoftReference<Object> cachedValue;
    //private Object value;
    private boolean upToDate;
    private Set<Symbol> dependents = new HashSet<Symbol>();
	private Set<Symbol> triggers = new HashSet<Symbol>();
    
    private ObservationGraphNode dependsOn = new DependencyGraphNode();
    private ObservationGraphNode tb = new TriggerGraphNode();

    public SymbolImpl(SymbolReference ref) {
        this.ref = ref;
    }
    
    public Set<Symbol> getDependents() {
		return dependents;
	}

	public Set<Symbol> getTriggers() {
		return triggers;
	}

    @Override
    public SymbolReference getReference() {
        return ref;
    }

    @Override
    public void redefine(SymbolDefinition d, SymbolTable t) {
        upToDate = false;
        clearDefinitions();
        definition = d;
        buildDefinitions(t);
        // TODO change to fireTriggers(this)
        t.fireTriggers(triggers);
        expireValue();
    }

    @Override
    public void forget() throws CannotForgetException {
    	System.out.println("Dependencies are " + dependents.size());
    	System.out.println("Triggers are " + triggers.size());
        if (dependents.isEmpty() && triggers.isEmpty()) {
            clearDefinitions();
        } else {
            throw new CannotForgetException("Cannot forget a symbol inside a dependency graph");
        }
    	System.out.println("After Dependencies are " + dependents.size());
    	System.out.println("After Triggers are " + triggers.size());
    }

    @Override
    public void expireValue() {
        upToDate = false;
        for (Symbol s : dependents) {
            s.expireValue();
        }
    }

    @Override
    public Object getValue(SymbolTable t) {
        if (definition == null) {
            return Undefined.instance;
        }
        if (!upToDate || cachedValue == null) {
            cachedValue = new SoftReference(definition.evaluate(t)); 
            upToDate = true;
        }
        return cachedValue.get();
    }

    @Override
    public void registerDependent(Symbol s) {
        dependents.add(s);
    }

    @Override
    public void unRegisterDependent(Symbol s) {
        dependents.remove(s);
    }


    @Override
    public boolean isUpToDate() {
        return upToDate;
    }

    @Override
    public void registerTrigger(Symbol s) {
        triggers.add(s);
    }


    @Override
    public void unRegisterTrigger(Symbol symbol) {
        triggers.remove(symbol);
    }

    private void clearDefinitions() {
        cachedValue = null;
    	//value = null;
        definition = null;
        dependsOn.unregister(this);
        tb.unregister(this);
    }

    private void buildDefinitions(SymbolTable t) {
    	dependsOn.buildGraph(this, 
    			definition.getDependencies(), 
    			t);
    	tb.buildGraph(this, 
    			definition.getTriggers(), 
    			t);
    }

	

    @Override
    public SymbolDefinition getDefinition() {
        return definition;
    }
}
