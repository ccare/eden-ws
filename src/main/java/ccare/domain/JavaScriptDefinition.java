package ccare.domain;

import ccare.engine.JavaScriptScopeFactory;
import org.apache.commons.lang.NotImplementedException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ccare.engine.JavaScriptTranslationUtils.extractSpecialSymbols;
import static ccare.engine.JavaScriptTranslationUtils.translateExpression;

/**
 * Created by IntelliJ IDEA.
 * User: carecx
 * Date: 25-Oct-2010
 * Time: 17:06:58
 * To change this template use File | Settings | File Templates.
 */
public class JavaScriptDefinition implements SymbolDefinition {



    public enum ExprType {
        FUNCTION, EXPRESSION
    }

    private final String expr;
    private final ExprType type;

    private List<SymbolReference> triggers;

    public JavaScriptDefinition(String expr) {
        this(expr, ExprType.EXPRESSION, null);
    }

    public JavaScriptDefinition(String expr, ExprType type, String... triggers) {
        this.expr = expr;
        this.type = type;
        if (triggers == null || triggers.length == 0) {
            this.triggers = Collections.<SymbolReference>emptyList();
        } else {
            this.triggers = new ArrayList(triggers.length);
            for (String trigger : triggers) {
                this.triggers.add(new SymbolReference(trigger.replaceAll("^#", "")));
            }
        }
    }

    public String getExpr() {
        return translateExpression(expr);
    }
    

    @Override
    public Collection<SymbolReference> getDependencies() {
        final Set<String> dependentNames = extractSpecialSymbols(expr);
        Set<SymbolReference> refs = new HashSet<SymbolReference>(dependentNames.size());
        for (String symbolName : dependentNames) {
            refs.add(new SymbolReference(symbolName));
        }
        return refs;
    }

    @Override
    public Collection<SymbolReference> getTriggers() {
        return triggers;
    }

    @Override
    public Object evaluate(final SymbolTable t) {
        Context cx = Context.enter();
        try {
            final Scriptable scope = JavaScriptScopeFactory.getInstance().scopeFor(t);
            final String expr = getExpr();
            if (isExecutable()) {
                return cx.compileFunction(scope, expr, "<func>", 1, null);
            } else {
                return cx.evaluateString(scope, expr, "<cmd>", 1, null);
            }
        } finally {
            Context.exit();
        }
    }

    @Override
    public boolean isExecutable() {
        return type == ExprType.FUNCTION;
    }
}
