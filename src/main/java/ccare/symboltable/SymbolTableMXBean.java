package ccare.symboltable;

public interface SymbolTableMXBean {
	
	String getName();
	
	int getSymbolCount();
	
	String evaluateString(String expression);
	

}