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
import org.mozilla.javascript.Undefined;

import java.util.*;

public class SymbolTableImpl implements SymbolTable {

    private final UUID id = UUID.randomUUID();
    private Map<SymbolReference, Symbol> symbols = new HashMap<SymbolReference, Symbol>();

    @Override
    public UUID getId() {
        return id;
    }

    public void add(Symbol sym) {
        symbols.put(sym.getReference(), sym);
    }

    @Override
    public Symbol get(SymbolReference reference) {
        if (!symbols.containsKey(reference)) {
            add(new SymbolImpl(reference));
        }
        return symbols.get(reference);
    }

    @Override
    public Set<SymbolReference> listSymbols() {
        return Collections.unmodifiableSet(symbols.keySet());
    }

    @Override
    public void fireTriggers(Set<Symbol> triggers) {
        for (Symbol s : triggers) {
            this.execute(s.getReference());
        }
    }

    @Override
    public void define(SymbolReference ref, String defn) {
        if (ref == null || defn == null) {
            throw new CannotDefineException();
        }
        final Definition d = new Definition(defn);
        Symbol s = get(ref);
        s.redefine(d, this);
    }

    @Override
    public Object getValue(SymbolReference ref) {
        Symbol s = get(ref);
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
        Symbol s = get(ref);
        s.redefine(d, this);
    }

    @Override
    public void defineTriggeredProc(SymbolReference ref, String defn, String... triggers) {
        final Definition d = new Definition(defn, Definition.ExprType.FUNCTION, triggers);
        Symbol s = get(ref);
        s.redefine(d, this);
    }

    @Override
    public void execute(SymbolReference a) {
        final SymbolReference r = new SymbolReference("____DUMMY_REF");
        define(r, "#{" + a.getName() + "}()");
        getValue(r);
    }
}