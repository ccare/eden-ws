package ccare.domain;

import ccare.engine.JavaScriptScopeFactory;
import org.apache.commons.lang.NotImplementedException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern SPECIALNAME_DIFFICULT_DEFINITION_OBJECT = Pattern.compile("[^#]*#[\\w:/#_]+\\s*is[^\\{]*\\{.*");
    private static final Pattern SPECIALNAME_DIFFICULT_DEFINITION_SINGLEQUOTE = Pattern.compile("[^#]*#[\\w:/#_]+\\s*is[^']'.*");
    private static final Pattern SPECIALNAME_DEFINITION = Pattern.compile("#([\\w:/#_]+)\\s*is\\s*([^\\};\\n]*)");
    private static final Pattern SPECIALNAME_PATTERN = Pattern.compile("#([\\w:/#_]+)");
    private static final Pattern SPECIALNAME_ESCAPEDPATTERN = Pattern.compile("#\\{([^\\}]+)\\}");
    private static final Pattern DOUBLE_QUOTE_REGION = Pattern.compile("\"[^\"\\r\\n]*\"");
    private static final Pattern SINGLE_QUOTE_REGION = Pattern.compile("'[^'\\r\\n]*'");
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
        if (SPECIALNAME_DIFFICULT_DEFINITION_OBJECT.matcher(expr).matches()) {
            throw new NotImplementedException("Cannot (yet) parse definitions involving objects");
        }
        if (SPECIALNAME_DIFFICULT_DEFINITION_SINGLEQUOTE.matcher(expr).matches()) {
            throw new NotImplementedException("Cannot (yet) parse definitions involving single quotes");
        }
        final String translatedDefns = SPECIALNAME_DEFINITION.matcher(expr).replaceAll("\\$eden_define('$1','$2')");
        final String translatedSimples = SPECIALNAME_PATTERN.matcher(translatedDefns).replaceAll("\\$eden_observe('$1')");
        final String translatedEscaped = SPECIALNAME_ESCAPEDPATTERN.matcher(translatedSimples).replaceAll("\\$eden_observe('$1')");
        return translatedEscaped;
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
            if (isExecutable()) {
                return cx.compileFunction(scope, getExpr(), "<func>", 1, null);
            } else {
                return cx.evaluateString(scope, getExpr(), "<cmd>", 1, null);
            }
        } finally {
            Context.exit();
        }
    }

    @Override
    public boolean isExecutable() {
        return type == ExprType.FUNCTION;
    }

    static Set<String> extractSpecialSymbols(final String input) {
        final String removedDblQuotedRegions = DOUBLE_QUOTE_REGION.matcher(input).replaceAll("");
        final String removedSingleQuotedRegions = SINGLE_QUOTE_REGION.matcher(removedDblQuotedRegions).replaceAll("");
        final String removedEscapedRegions = SPECIALNAME_ESCAPEDPATTERN.matcher(removedSingleQuotedRegions).replaceAll("");
        final Matcher m = SPECIALNAME_PATTERN.matcher(removedEscapedRegions);
        Set<String> rtn = new HashSet<String>();

        while (m.find()) {
            final String s = m.group(0).replaceAll("^#", "");
            rtn.add(s);
        }

        final Matcher m2 = SPECIALNAME_ESCAPEDPATTERN.matcher(removedSingleQuotedRegions);
        while (m2.find()) {
            final String s = m2.group(0).replaceAll("^#\\{", "");
            rtn.add(s.replaceAll("\\}$", ""));
        }

        return rtn;
    }

}
