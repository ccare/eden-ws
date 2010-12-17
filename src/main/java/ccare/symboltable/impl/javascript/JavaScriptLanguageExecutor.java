package ccare.symboltable.impl.javascript;

import static java.lang.String.format;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import ccare.symboltable.LanguageExecutor;
import ccare.symboltable.SymbolDefinition;
import ccare.symboltable.SymbolTable;
import ccare.symboltable.exceptions.EvaluationException;

public class JavaScriptLanguageExecutor extends LanguageExecutor {

	public JavaScriptLanguageExecutor(SymbolTable table) {
		super(table);
	}

	@Override
	public Object evaluate(SymbolDefinition definition) {
		final String expr = definition.getExpr();
        Context cx = Context.enter();
        try {
            final Scriptable scope = getScopeFactory().scopeFor(getSymbolTable());
            if (definition.isExecutable()) {
                return compileFunction(cx, scope, expr);
            } else {
                return evalExpression(cx, scope, expr);
            }
        } catch (EcmaError error) {
            throw new EvaluationException(format("Could not evaluate %s", expr), error);
        } finally {
            Context.exit();
        }
	}
	

    private ScopeFactory getScopeFactory() {
        return ScopeFactory.getInstance();
    }

    static Object evalExpression(Context cx, Scriptable scope, String expr) {
        return cx.evaluateString(scope, expr, "<cmd>", 1, null);
    }

    static Function compileFunction(Context cx, Scriptable scope, String expr) {
        return cx.compileFunction(scope, expr, "<func>", 1, null);
    }
	
}
