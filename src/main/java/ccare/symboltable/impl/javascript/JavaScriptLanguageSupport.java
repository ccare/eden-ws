package ccare.symboltable.impl.javascript;

import ccare.symboltable.LanugageSupport;
import ccare.symboltable.SymbolDefinition;

public class JavaScriptLanguageSupport implements LanugageSupport {

	@Override
	public SymbolDefinition createMethodCall(String name, Object[] params) {
		return new Definition("#{" + name + "}(" + encodeParams(params) + ")");
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
