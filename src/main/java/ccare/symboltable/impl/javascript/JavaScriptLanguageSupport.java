package ccare.symboltable.impl.javascript;

import ccare.symboltable.LanugageSupport;
import ccare.symboltable.SymbolDefinition;

public class JavaScriptLanguageSupport implements LanugageSupport {

	@Override
	public SymbolDefinition createDefinition(String defn) {
		if (defn.trim().startsWith("function")) {
			return defineFunction(defn);
		} else {
			return new Definition(defn);
		}

	}

	@Override
	public SymbolDefinition createMethodCall(String name, Object[] params) {
		return new Definition("#{" + name + "}(" + encodeParams(params) + ")");
	}

	@Override
	public SymbolDefinition defineFunction(String defn) {
		return new Definition(defn, Definition.ExprType.FUNCTION);
	}

	@Override
	public SymbolDefinition defineTriggeredProc(String defn, String... triggers) {
		return new Definition(defn, Definition.ExprType.FUNCTION, triggers);
	}

	private String encodeParams(Object... params) {
		if (params.length == 0) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(params[0]);
			for (int i = 1; i < params.length; i++) {
				sb.append(",");
				sb.append(params[i]);
			}
			return sb.toString();
		}
	}

}
