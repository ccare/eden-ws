package ccare.symboltable.maintainers;

import ccare.symboltable.Symbol;
import ccare.symboltable.impl.javascript.Definition;

public interface DependencyMaintainer {

	void beforeRedefinition(Symbol s, Definition d);

	void afterRedefinition(Symbol s);

}
