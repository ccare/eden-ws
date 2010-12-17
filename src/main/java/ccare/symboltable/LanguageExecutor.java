package ccare.symboltable;

public abstract class LanguageExecutor {
	
	SymbolTable symbolTable;

	public LanguageExecutor(SymbolTable table) {
		setSymbolTable(table);
	}
	
	public void setSymbolTable(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	public abstract Object evaluate(SymbolDefinition definition);

}
