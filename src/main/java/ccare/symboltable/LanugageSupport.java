package ccare.symboltable;

import ccare.symboltable.impl.javascript.Definition;

public interface LanugageSupport {

	SymbolDefinition createMethodCall(String name, Object... params);

	SymbolDefinition defineTriggeredProc(String defn, String... triggers);

	SymbolDefinition defineFunction(String defn);

	SymbolDefinition createDefinition(String defn);

}
