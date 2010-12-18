package ccare.symboltable;

public interface SymbolTableMXBean {

	String evaluateString(String expression);

	String getName();

	int getSymbolCount();

}
