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
    public static final Pattern DEFN = Pattern.compile("#[\\w:/#_]+\\s*is(\\s+)");

    private static final Pattern SPECIALNAME_DIFFICULT_DEFINITION_OBJECT = Pattern.compile("[^#]*#[\\w:/#_]+\\s*is[^\\{]*\\{.*");
    public static final Pattern SPECIALNAME_DIFFICULT_DEFINITION_SINGLEQUOTE = Pattern.compile("[^#]*#[\\w:/#_]+\\s*is[^']'.*");
    private static final Pattern SPECIALNAME_DEFINITION = Pattern.compile("#([\\w:/#_]+)\\s*is\\s*([^\\};\\n]*)");
    public static final Pattern SPECIALNAME_PATTERN = Pattern.compile("#([^#{][\\w:/#_]*)(?<!\\s+is)");
    private static final Pattern SPECIALNAME_ESCAPEDPATTERN = Pattern.compile("#\\{([^\\}]+)\\}(?<!\\s*is)");
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
//        final String translatedDefns;
//
//            translatedDefns = translateExpression(SPECIALNAME_PATTERN.matcher(expr).replaceAll("\\$eden_observe('$1')"));
//        if (SPECIALNAME_DIFFICULT_DEFINITION_OBJECT.matcher(expr).matches()) {
//            translatedDefns = translateExpression(SPECIALNAME_PATTERN.matcher(expr).replaceAll("\\$eden_observe('$1')"));
//            //throw new NotImplementedException("Cannot (yet) parse definitions involving objects");
//        } else if (SPECIALNAME_DIFFICULT_DEFINITION_SINGLEQUOTE.matcher(expr).matches()) {
//            translatedDefns = translateExpression(SPECIALNAME_PATTERN.matcher(expr).replaceAll("\\$eden_observe('$1')"));
//        } else {
//            translatedDefns = SPECIALNAME_DEFINITION.matcher(expr).replaceAll("\\$eden_define('$1','$2')");
//        }
//        final String translatedSimples = SPECIALNAME_PATTERN.matcher(expr).replaceAll("\\$eden_observe('$1')");
//        final String translatedEscaped = SPECIALNAME_ESCAPEDPATTERN.matcher(translatedSimples).replaceAll("\\$eden_observe('$1')");
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

    static int findEndOfExpr(final String s, final int i) {
        if (s.length() == 0) {
            return 0;
        } else if (s.length() == i) {
            return i;
        }
        int ptr = i;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int braceLevel = 0;
        while (ptr < s.length()) {
            char c = s.charAt(ptr);
            if (inSingleQuote) {
                if (c == '\\' && s.charAt(ptr+1) == '\'') {
                    ptr++;
                } else if (c == '\'') {
                    inSingleQuote = false;
                }
            } else if (inDoubleQuote) {
                if (c == '\\' && s.charAt(ptr+1) == '"') {
                    ptr++;
                } else if (c == '"') {
                    inDoubleQuote = false;
                }
            } else if (c == '{') {
                braceLevel++;
            } else if (braceLevel > 0) {
                switch (c) {
                    case '}' : braceLevel--; break;
                    case '\'' : inSingleQuote = true; break;
                    case '"' : inDoubleQuote = true; break;
                }
            } else if (braceLevel == 0) {
                switch (c) {
                    case '}' : return ptr;
                    case ';' : return ptr;
                    case '\n' : return ptr;
                    case '\'' : inSingleQuote = true; break;
                    case '"' : inDoubleQuote = true; break;
                }
            }
            ptr++;
        }
        return ptr;
    }

    static String extractExpr(String s, int i) {
        return s.substring(i, JavaScriptDefinition.findEndOfExpr(s, i));
    }

    static String translateExpression(String expr) {
        List<DefnFragment> fragments = findExprRange(expr);
        StringBuilder sb = new StringBuilder();
        int ptr = 0;
        for (DefnFragment d : fragments) {
            final int defnStart = d.start;
            final int start = d.exprStart;
            final int end = d.exprEnd;
            final String preamble = expr.substring(ptr, defnStart);
            final String sym = expr.substring(defnStart, start);
            final String expression = expr.substring(start, end);
            sb.append(preamble);
            sb.append("$eden_define('");
            sb.append(sym.replaceAll("\\s*is\\s*","").replaceAll("^#",""));
            sb.append("','");
            final String escapedSlashes = expression.replaceAll("\\\\", "\\\\\\\\");
            final String escapedQuotes = escapedSlashes.replaceAll("'", "\\\\'");
            sb.append(escapedQuotes);
            sb.append("')");
            ptr = end;
        }
        final String remainingCode = expr.substring(ptr, expr.length());
        sb.append(encodeObservation(remainingCode));
        final String s = sb.toString();


        return s;

        //return translatedEscaped.replaceAll("(\\$eden_define[^\\$]*)\\$eden_observe\\('([^\\)']*)'\\)","$1\\$eden_observe(\\\\'$2\\\\')");

      //  return translatedEscaped.replaceAll("(\\$eden_define[^\\$]*)\\$eden_observe","x')");
    }

    static String encodeObservation(final String in) {
        final StringBuilder sb = new StringBuilder();
        for (final String s: pullOutRegions(in)) {
            if (!s.isEmpty()) {
                final char c = s.charAt(0);
                if (c == '"' || c == '\'') {
                    sb.append(s);
                } else {
                    final String translatedSimples = SPECIALNAME_PATTERN.matcher(s).replaceAll("\\$eden_observe('$1')");
                    final String translatedEscaped = SPECIALNAME_ESCAPEDPATTERN.matcher(translatedSimples).replaceAll("\\$eden_observe('$1')");
                    sb.append(translatedEscaped);
                }
            }
        }
        return sb.toString();
    }

    static List<String> pullOutRegions(final String s) {
        List<String> list = new ArrayList<String>();
        int start = 0;
        int pos = 0;
        boolean inDblString = false;
        boolean inSingleString = false;
        final int length = s.length();
        for (; pos < length; pos++) {
            char c = s.charAt(pos);
            if (!inSingleString && !inDblString && c == '"') {
               list.add(s.substring(start, pos));
               start = pos;
               inDblString = true;
            } else if (inDblString && c == '"') {
               list.add(s.substring(start, pos+1));
               pos++;
               start = pos;
               inDblString= false;               
            } else if (inDblString && c == '\\') {
               pos++;
            } else if (inSingleString && c == '\\') {
               pos++;
            } else if (!inDblString && !inSingleString && c == '\'') {
               list.add(s.substring(start, pos));
               start = pos;
               inSingleString = true;
            } else if (inSingleString && c == '\'') {
               list.add(s.substring(start, pos+1));
               pos++;
               start = pos;
               inSingleString= false;
            }

        }
        if (pos < length) {
            list.add(s.substring(start, pos));
        } else {
            list.add(s.substring(start, length));            
        }
        return list;
    }


    static List<Integer[]> findStarts(String input) {
        final Matcher matcher = JavaScriptDefinition.DEFN.matcher(input);
        List<Integer[]> indexes = new ArrayList();
        while (matcher.find() == true)  {
            indexes.add(new Integer[] {matcher.start(), matcher.end()} );
        }
        return indexes;
    }


    static List<DefnFragment> findExprRange(final String s) {
        final List<DefnFragment> list = new ArrayList<DefnFragment>();
        for (Integer[] i : findStarts(s)){
            final int start = i[1];
            list.add(new DefnFragment(i[0], start, findEndOfExpr(s, start)));
        }
        return list;
    }


    static class DefnFragment {
        public final int exprStart;
        public final int start;
        public final int exprEnd;

        public DefnFragment(int start, int exprStart, int exprEnd) {
            this.start = start;
            this.exprStart = exprStart;
            this.exprEnd = exprEnd;
        }
    }

}
