package ccare.symboltable;

public abstract class LanguageExecutor {

	SymbolTable symbolTable;

	public LanguageExecutor(SymbolTable table) {
		setSymbolTable(table);
	}

	public abstract Object evaluate(SymbolDefinition definition);

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	public void setSymbolTable(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

}
