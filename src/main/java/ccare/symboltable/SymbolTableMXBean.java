package ccare.symboltable;

public interface SymbolTableMXBean {

	String evaluateString(String expression);

	/**
	 * Get name of the symbol table
	 * @return
	 */
	String getName();

	int getSymbolCount();

}
