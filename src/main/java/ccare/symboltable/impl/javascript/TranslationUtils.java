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

package ccare.symboltable.impl.javascript;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: carecx Date: 30-Oct-2010 Time: 16:18:58
 */
public class TranslationUtils {

	static final Pattern DEFN = Pattern.compile("[\\w:/#_]+\\s*is(\\s+)");
	static final Pattern SPECIALNAME_ESCAPEDPATTERN = Pattern
			.compile("#\\{([^\\}]+)\\}");
	static final Pattern SPECIALNAME_PATTERN = Pattern
			.compile("#([^#{][\\w:/#_]*)");

	public static Set<String> extractSpecialSymbols(final String input) {
		Set<String> symbols = new HashSet<String>();
		final List<String> regions = pullOutRegions(input);
		for (String region : regions) {
			if (processibleRegion(region)) {
				final Matcher escapedMatcher = SPECIALNAME_ESCAPEDPATTERN
						.matcher(region);
				while (escapedMatcher.find()) {
					symbols.add(escapedMatcher.group(1));
				}
				escapedMatcher.reset();
				final String removedEscapedRegions = escapedMatcher
						.replaceAll("");
				final Matcher normalMatcher = SPECIALNAME_PATTERN
						.matcher(removedEscapedRegions);
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
			sb.append(sym.replaceAll("\\s*is\\s*", "").replaceAll("^#", ""));
			sb.append("','");
			final String escapedSlashes = expression.replaceAll("\\\\",
					"\\\\\\\\");
			final String escapedQuotes = escapedSlashes
					.replaceAll("'", "\\\\'");
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
		boolean inE4XStart = false;
		boolean inE4XEnd = false;
		StringBuilder tag = null;
		Deque<String> tags = new ArrayDeque<String>();
		int braceLevel = 0;
		while (ptr < s.length()) {
			char c = s.charAt(ptr);
			if (inE4XStart || inE4XEnd) {
				if (c != '>') {
					tag.append(c);
				} else {
					if (inE4XStart) {
						tags.push(tag.toString());
						inE4XStart = false;
					} else if (inE4XEnd) {
						inE4XEnd = false;
						if (tags.peek().equals(tag.toString())) {
							tags.pop();
						} else {
							throw new IllegalStateException("Shouldn't be here");
						}
					}
				}
			} else if (inSingleQuote) {
				if (c == '\\' && s.charAt(ptr + 1) == '\'') {
					ptr++;
				} else if (c == '\'') {
					inSingleQuote = false;
				}
			} else if (inDoubleQuote) {
				if (c == '\\' && s.charAt(ptr + 1) == '"') {
					ptr++;
				} else if (c == '"') {
					inDoubleQuote = false;
				}
			} else if (c == '<') {
				char cc = s.charAt(ptr + 1);
				if (cc != ' ') {
					if (cc == '/') {
						inE4XEnd = true;
						ptr++;
					} else {
						inE4XStart = true;
					}
					tag = new StringBuilder();
				}
			} else if (tags.isEmpty()) {
				if (c == '{') {
					braceLevel++;
				} else if (braceLevel > 0) {
					switch (c) {
					case '}':
						braceLevel--;
						break;
					case '\'':
						inSingleQuote = true;
						break;
					case '"':
						inDoubleQuote = true;
						break;
					}
				} else if (braceLevel == 0) {
					switch (c) {
					case '}':
						return ptr;
					case ';':
						return ptr;
					case '\n':
						return ptr;
					case '\'':
						inSingleQuote = true;
						break;
					case '"':
						inDoubleQuote = true;
						break;
					}
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
		for (final String region : pullOutRegions(in)) {
			if (processibleRegion(region)) {
				final String translatedEscaped = translateObservationsInString(region);
				sb.append(translatedEscaped);
			} else if (e4XRegion(region)) {
				final String translatedEscaped = translateE4XObservation(region);
				sb.append(translatedEscaped);
			} else {
				sb.append(region);
			}
		}
		return sb.toString();
	}

	static String translateE4XObservation(final String input) {
		int levels = 0;
		StringBuilder result = new StringBuilder();
		StringBuilder working = new StringBuilder();
		for (char c : input.toCharArray()) {
			working.append(c);
			if (c == '{') {
				levels++;
			} else if (c == '}') {
				levels--;
			}
			if (levels == 0) {
				final String translated = translateObservationsInString(working);
				result.append(translated);
				working = new StringBuilder();
			}
		}
		return result.toString();
	}

	private static String translateObservationsInString(final CharSequence input) {
		final String translatedNormalObservables = SPECIALNAME_PATTERN.matcher(
				input).replaceAll("\\$eden_observe('$1')");
		final String translatedEscapedObservables = SPECIALNAME_ESCAPEDPATTERN
				.matcher(translatedNormalObservables).replaceAll(
						"\\$eden_observe('$1')");
		return translatedEscapedObservables;
	}

	static List<String> pullOutRegions(final String s) {
		List<String> list = new ArrayList<String>();
		int start = 0;
		int pos = 0;
		boolean inDblString = false;
		boolean inSingleString = false;
		boolean inE4XStart = false;
		boolean inE4XEnd = false;
		boolean inAttrs = false;
		boolean inSingletonNode = false;
		StringBuilder tag = null;
		Deque<String> tags = new ArrayDeque<String>();
		final int length = s.length();
		for (; pos < length; pos++) {
			char c = s.charAt(pos);
			if (inE4XStart && c == '/') {
				inSingletonNode = true;
			} else if (inE4XStart || inE4XEnd) {
				if (inE4XStart && c == ' ') {
					inAttrs = true;
				}

				if (!inAttrs && c != '>') {
					tag.append(c);
				} else {
					if (inE4XStart) {
						if (inSingletonNode) {
							createRegion(s, list, start, pos + 1);
							start = pos + 1;
						} else {
							tags.push(tag.toString());
						}
						tag = null;
						inE4XStart = false;
						inSingletonNode = false;
						inAttrs = false;
					} else if (inE4XEnd) {
						inE4XEnd = false;
						final String nextTagOnStack = tags.peek();
						final String current = tag.toString();
						if (nextTagOnStack.equals(current)) {
							tag = null;
							tags.pop();
							if (tags.isEmpty()) {
								createRegion(s, list, start, pos + 1);
								start = pos + 1;
							}
						} else {
							throw new IllegalStateException("Shouldn't be here");
						}
					}
				}
			} else if (c == '<' && tags.isEmpty() && !inSingleString
					&& !inDblString) {
				char cc = s.charAt(pos + 1);
				if (cc != ' ') {
					if (cc == '/') {
						inE4XEnd = true;
						pos++;
					} else {
						inE4XStart = true;
						createRegion(s, list, start, pos);
						start = pos;
					}
					tag = new StringBuilder();
				}
			} else if (c == '<' && !tags.isEmpty()) {
				char cc = s.charAt(pos + 1);
				if (cc != ' ') {
					if (cc == '/') {
						inE4XEnd = true;
						pos++;
					} else {
						inE4XStart = true;
					}
					tag = new StringBuilder();
				}
			} else if (!tags.isEmpty()) {
				continue;
			} else {
				if (!inSingleString && !inDblString && c == '"') {
					createRegion(s, list, start, pos);
					start = pos;
					inDblString = true;
				} else if (inDblString && c == '"') {
					createRegion(s, list, start, pos + 1);
					pos++;
					start = pos;
					inDblString = false;
				} else if (inDblString && c == '\\') {
					pos++;
				} else if (inSingleString && c == '\\') {
					pos++;
				} else if (!inDblString && !inSingleString && c == '\'') {
					createRegion(s, list, start, pos);
					start = pos;
					inSingleString = true;
				} else if (inSingleString && c == '\'') {
					createRegion(s, list, start, pos + 1);
					pos++;
					start = pos;
					inSingleString = false;
				}
			}

		}
		if (pos < length) {
			createRegion(s, list, start, pos);
		} else {
			createRegion(s, list, start, length);
		}
		return list;
	}

	private static void createRegion(String string, List<String> regionList,
			int startIndex, int endIndex) {
		if (endIndex > startIndex) {
			regionList.add(string.substring(startIndex, endIndex));
		} else if (endIndex != startIndex) {
			throw new IllegalArgumentException("Bad substring range");
		}
	}

	/**
	 * Find index bounds of definitions inside an input string
	 * 
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
				while (matcher.find() == true) {
					final int exprStart = index + matcher.end();
					indexes.add(new DefnFragment(index + matcher.start(),
							exprStart, findEndOfExpr(input, exprStart)));
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
			return c != '\'' && c != '"' && c != '<';
		}
	}

	private static boolean e4XRegion(String region) {
		if (region.isEmpty()) {
			return false;
		} else {
			final char c = region.charAt(0);
			return c == '<';
		}
	}

	/**
	 * Tuple to represent the location of a Definition Fragment inside a bigger
	 * definition
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
