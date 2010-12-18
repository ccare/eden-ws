/*
 * Copyright (c) 2010, Charles Care
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ccare.symboltable;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;

/**
 * User: carecx Date: 26-Oct-2010 Time: 15:24:19
 */
public interface SymbolTable extends SymbolTableMXBean {
	
	/**
	 * Define a symbol in the symbol table.  
	 * 
	 * @param reference The reference of the target symbol
	 * @param definition The definition of the symbol as a String
	 */
	void define(SymbolReference reference, String definition);

	/**
	 * Define a function. Note that implementers <i>may</i> choose to support
	 * defining a function via the {@link #define} method.
	 * 
	 * @param reference The reference of the target symbol
	 * @param definition The definition of the function as a String
	 */
	void defineFunction(SymbolReference reference, String definition);

	/**
	 * Define a Triggered procedure. The proc will be executed
	 * when one of the observables registered as triggers changed.  
	 * 
	 * @param reference The reference of the target symbol
	 * @param definition The definition of the proceedure as a String
	 * @param triggers A var-arg list of triggers 
	 */
	void defineTriggeredProc(SymbolReference reference, String definition, 
			String... triggers);

	/**
	 * Evaluate an expression
	 * 
	 * @param expr an expression to evaluate
	 * @return the result of evaluating the expression
	 */
	Object evaluate(String expr);

	/**
	 * Execute a symbol (if it is executable!)
	 * 
	 * @param reference A reference to the symbol
	 * @return The result of execution
	 * 
	 * @see #execute(SymbolReference, Object...)
	 */
	Object execute(SymbolReference reference);

	/**
	 * Execute a symbol (if it is executable!)
	 * 
	 * @param reference A reference to the symbol
	 * @param params Parameters to pass to the underlying function
	 * @return The result of execution
	 * 
	 * @see #execute(SymbolReference)
	 */
	Object execute(SymbolReference reference, Object... params);

	
	/**
	 * Get the executor instance being used to evaluate
	 * values in this symbol table
	 * @return The executor instance
	 */
	LanguageExecutor getExecutor();

	/** 
	 * Get the unique id of this symbol table
	 * @return unique id 
	 */
	UUID getId();

	String getName();

	Set<SymbolReference> getSymbols();

	Object getValue(SymbolReference bRef);

	void setName(String name);
	
}
