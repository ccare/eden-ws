package ccare.service;

import ccare.domain.Observable;
import ccare.domain.Symbol;
import ccare.domain.SymbolReference;
import ccare.domain.SymbolTable;

import java.util.UUID;

/**
 * User: carecx
 * Date: 13-Oct-2010
 * Time: 22:20:08
 */
public interface SymbolTableService extends SymbolTable {
    public UUID getId();

    public void define(SymbolReference reference, Observable d);

    public void add(Symbol sym);

    public Observable observe(SymbolReference reference);
}
