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

package ccare.symboltable;

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
    static final Pattern SPECIALNAME_ESCAPEDPATTERN = Pattern.compile("#\\{([^\\}]+)\\}");
    static final Pattern SPECIALNAME_PATTERN = Pattern.compile("#([^#{][\\w:/#_]*)");
    
    public static Set<String> extractSpecialSymbols(final String input) {
        Set<String> symbols = new HashSet<String>();
        final List<String> regions = pullOutRegions(input);
        for (String region : regions) {
           if (processibleRegion(region)) {
                final Matcher escapedMatcher = SPECIALNAME_ESCAPEDPATTERN.matcher(region);
                while (escapedMatcher.find()) {
                    symbols.add(escapedMatcher.group(1));
                }
                escapedMatcher.reset();
                final String removedEscapedRegions = escapedMatcher.replaceAll("");
                final Matcher normalMatcher = SPECIALNAME_PATTERN.matcher(removedEscapedRegions);
                while (normalMatcher.find()) {
                    symbols.add(normalMatcher.group(1));
                }
            }
        }
        return symbols;
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
        for (final String region: pullOutRegions(in)) {
            if (processibleRegion(region)) {
                final String translatedEscaped = translateObservationsInString(region);
                sb.append(translatedEscaped);
            } else {
                sb.append(region);
            }
        }
        return sb.toString();
    }

    private static String translateObservationsInString(final String input) {
        final String translatedNormalObservables = SPECIALNAME_PATTERN.matcher(input).replaceAll("\\$eden_observe('$1')");
        final String translatedEscapedObservables = SPECIALNAME_ESCAPEDPATTERN.matcher(translatedNormalObservables).replaceAll("\\$eden_observe('$1')");
        return translatedEscapedObservables;
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

    /**
     * Find index bounds of definitions inside an input string
     * @param input
     * @return
     */
    static List<DefnFragment> findExprRange(final String input) {
        final List<String> regions = pullOutRegions(input);
        List<DefnFragment> indexes = new ArrayList();
        int index = 0;
        for (String region : regions) {
            if (processibleRegion(region)) {
                final Matcher matcher = DEFN.matcher(region);
                while (matcher.find() == true)  {
                    final int exprStart = index + matcher.end();
                    indexes.add(new DefnFragment(index + matcher.start(), exprStart,  findEndOfExpr(input, exprStart) ) );
                }
            }
            index = index + region.length();
        }
        return indexes;
    }

    private static boolean processibleRegion(String region) {
        if (region.isEmpty()) {
            return false;
        } else {
            final char c = region.charAt(0);
            return c != '\'' && c != '"';                            
        }
    }

    /**
     * Tuple to represent the location of a Definition Fragment inside a bigger definition
     */
    static class DefnFragment {
        public final int exprStart;
        public final int start;
        public final int exprEnd;

        DefnFragment(int start, int exprStart, int exprEnd) {
            this.start = start;
            this.exprStart = exprStart;
            this.exprEnd = exprEnd;
        }
    }

}