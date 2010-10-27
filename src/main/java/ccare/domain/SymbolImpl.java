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

    @Override
    public Set<Symbol> getTriggers() {
        return triggers;
    }

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
