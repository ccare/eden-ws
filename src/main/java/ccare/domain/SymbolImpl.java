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

package ccare.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * User: carecx
 * Date: 13-Oct-2010
 * Time: 23:36:43
 */
public class SymbolImpl implements Symbol {
    private final SymbolReference ref;
    private SymbolDefinition definition;
    private Object value;
    private boolean upToDate;
    private Set<Symbol> dependents = new HashSet<Symbol>();
    private Set<Symbol> dependsOn = new HashSet<Symbol>();
    private Set<Symbol> triggeredBy = new HashSet<Symbol>();
    private Set<Symbol> triggers = new HashSet<Symbol>();

    public SymbolImpl(SymbolReference ref) {
        this.ref = ref;
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
        t.fireTriggers(triggers);
        expireValue();
    }

    @Override
    public void forget() throws CannotForgetException {
        if (dependents.isEmpty() && triggers.isEmpty()) {
            clearDefinitions();
        } else {
            throw new CannotForgetException("Cannot forget a symbol inside a dependency graph");
        }
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
            return null;
        }
        if (!upToDate || value == null) {
            value = definition.evaluate(t);
            upToDate = true;
        }
        return value;
    }

    @Override
    public void registerDependent(Symbol s) {
        if (!s.isDependentOn(this)) {
            throw new IllegalArgumentException("Not a valid dependent");
        }
        dependents.add(s);
    }

    @Override
    public void unRegisterDependent(Symbol s) {
        dependents.remove(s);
    }

    @Override
    public boolean isDependentOn(Symbol symbol) {
        return dependsOn.contains(symbol);
    }

    @Override
    public boolean isUpToDate() {
        return upToDate;
    }

    @Override
    public void registerTrigger(Symbol s) {
        if (!s.isTriggeredBy(this)) {
            throw new IllegalArgumentException("Not a valid trigger");
        }
        triggers.add(s);
    }

    @Override
    public boolean isTriggeredBy(Symbol symbol) {
        return triggeredBy.contains(symbol);
    }

    @Override
    public void unRegisterTrigger(Symbol symbol) {
        triggers.remove(symbol);
    }

    private void clearDefinitions() {
        value = null;
        definition = null;
        for (Symbol s : dependsOn) {
            s.unRegisterDependent(this);
        }
        dependsOn.clear();
        for (Symbol s : triggeredBy) {
            s.unRegisterTrigger(this);
        }
        triggeredBy.clear();
    }

    private void buildDefinitions(SymbolTable t) {
        for (SymbolReference ref : definition.getDependencies()) {
            Symbol s = t.get(ref);
            dependsOn.add(s);
            s.registerDependent(this);
        }
        for (SymbolReference ref : definition.getTriggers()) {
            Symbol s = t.get(ref);
            triggeredBy.add(s);
            s.registerTrigger(this);
        }
    }
}
