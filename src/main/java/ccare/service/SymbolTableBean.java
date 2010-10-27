package ccare.service;

import ccare.domain.*;
import ccare.domain.Observable;

import javax.ejb.Lock;
import javax.ejb.Singleton;
import java.util.*;

import static javax.ejb.LockType.READ;
import static javax.ejb.LockType.WRITE;

@Singleton
@Lock(READ)
public class SymbolTableBean implements SymbolTableService {

    private final UUID id = UUID.randomUUID();
    private Map<SymbolReference, Object> values = new HashMap<SymbolReference, Object>();
    private Map<SymbolReference, String> defns = new HashMap<SymbolReference, String>();
    private Map<SymbolReference, Symbol> symbols = new HashMap<SymbolReference, Symbol>();

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    @Lock(WRITE)
    public void define(SymbolReference reference, Observable d) {
        defns.put(reference, d.getDefinition());
        values.put(reference, d.getCurrentValue());
    }

    @Override
    public void add(Symbol sym) {
        symbols.put(sym.getReference(), sym);
    }

    @Override
    public Observable observe(SymbolReference reference) {
        Observable o = new Observable();
        o.setDefinition(defns.get(reference));
        o.setCurrentValue(values.get(reference));
        return o;
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
        return Collections.unmodifiableSet(values.keySet());
    }

    @Override
    public void fireTriggers(Set<Symbol> triggers) {
        for (Symbol s : triggers) {
            this.execute(s.getReference());
        }
    }

    @Override
    public void define(SymbolReference ref, String defn) {
        final JavaScriptDefinition d = new JavaScriptDefinition(defn);
        Symbol s = get(ref);
        s.redefine(d, this);
    }

    @Override
    public Object getValue(SymbolReference ref) {
        Symbol s = get(ref);
        return s.getValue(this);
    }

    @Override
    public void defineFunction(SymbolReference ref, String defn) {
        final JavaScriptDefinition d = new JavaScriptDefinition(defn, JavaScriptDefinition.ExprType.FUNCTION);
        Symbol s = get(ref);
        s.redefine(d, this);
    }

    @Override
    public void defineTriggeredProc(SymbolReference ref, String defn, String... triggers) {
        final JavaScriptDefinition d = new JavaScriptDefinition(defn, JavaScriptDefinition.ExprType.FUNCTION, triggers);
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
