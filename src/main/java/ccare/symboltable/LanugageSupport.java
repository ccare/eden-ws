package ccare.symboltable;

import ccare.symboltable.impl.javascript.Definition;

public interface LanugageSupport {

	SymbolDefinition createMethodCall(String name, Object... params);

}
