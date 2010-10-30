package ccare.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: carecx
 * Date: 30-Oct-2010
 * Time: 16:18:58
 */
public class JavaScriptTranslationUtils {
    
    static final Pattern DEFN = Pattern.compile("[\\w:/#_]+\\s*is(\\s+)");
    static final Pattern DOUBLE_QUOTE_REGION = Pattern.compile("\"[^\"\\r\\n]*\"");
    static final Pattern SINGLE_QUOTE_REGION = Pattern.compile("'[^'\\r\\n]*'");
    static final Pattern SPECIALNAME_ESCAPEDPATTERN = Pattern.compile("#\\{([^\\}]+)\\}(?<!\\s*is)");
    static final Pattern SPECIALNAME_PATTERN = Pattern.compile("#([^#{][\\w:/#_]*)(?<!\\s+is)");
    
    public static Set<String> extractSpecialSymbols(final String input) {
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

    public static String translateExpression(String expr) {
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
        return s.substring(i, findEndOfExpr(s, i));
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

    static List<Integer[]> findStarts(final String input) {
        final List<String> regions = pullOutRegions(input);
        List<Integer[]> indexes = new ArrayList();
        int index = 0;
        for (String region : regions) {
            if (region.length() > 0) {
                final Matcher matcher = DEFN.matcher(region);
                final char c = region.charAt(0);
                if (c != '\'' && c != '"') {
                    while (matcher.find() == true)  {
                        indexes.add(new Integer[] {index + matcher.start(), index + matcher.end()} );
                    }
                }
                index = index + region.length();
            }
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
