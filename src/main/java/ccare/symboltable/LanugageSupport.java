package ccare.symboltable;

public interface LanugageSupport {

	SymbolDefinition createDefinition(String defn);

	SymbolDefinition createMethodCall(String name, Object... params);

	SymbolDefinition defineFunction(String defn);

	SymbolDefinition defineTriggeredProc(String defn, String... triggers);

}
