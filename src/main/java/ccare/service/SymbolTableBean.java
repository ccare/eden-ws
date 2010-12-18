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

package ccare.service;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccare.domain.Observable;
import ccare.domain.TableReference;
import ccare.symboltable.SymbolReference;
import ccare.symboltable.SymbolTable;
import ccare.symboltable.impl.SymbolTableImpl;

import com.sun.jersey.api.NotFoundException;

//@Singleton

//@Lock(READ)
public class SymbolTableBean implements SymbolTableService {

    private static final Logger logger = LoggerFactory.getLogger(SymbolTableBean.class);

    private final UUID id = UUID.randomUUID();
    private SymbolTable table = new SymbolTableImpl();
    private Map<TableReference, SymbolTable> tables = new HashMap<TableReference, SymbolTable>();
    private List<SymbolTable> recycleBin = new ArrayList<SymbolTable>();
    private List<TableReference> keys;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
//    @Lock(WRITE)
public void define(SymbolReference reference, Observable d) {
        table.define(reference, d.getDefinition());
    }

    @Override
    public Set<SymbolReference> listSymbols() {
        return table.getSymbols();
    }

    @Override
    public TableReference createSpace(final TableReference ref) throws CannotCreateException {
        return createSpace(ref.getName());
    }

    @Override
    public TableReference createSpace(final String name) throws CannotCreateException {
        return doCreate(name);
    }

    @Override
    public void deleteSpace(final String name) {
        deleteSpace(new TableReference(name));
    }

    @Override
    public void deleteSpace(final TableReference reference) {
        doDelete(reference);
    }

    private TableReference doCreate(String name) throws CannotCreateException {
        if (tables.containsKey(new TableReference(name))) {
            throw new CannotCreateException("Cannot create duplicate space");
        }

        final SymbolTable table = new SymbolTableImpl();
        if (name != null) {
            table.setName(name);
        }
        final TableReference newRef = new TableReference(table.getId(), name);
        tables.put(newRef, table);
        keys = null;
        return newRef;
    }

    private void doDelete(TableReference reference) {
        logger.debug(format("Deleting %s", reference));
        final SymbolTable table = tables.remove(reference);
        if (table == null) {
            throw new NotFoundException();
        } else {
            recycleBin.add(table);
            keys = null;
        }
    }

    @Override
    public List<TableReference> allSpaces() {
        if (keys == null) {
            final Set<TableReference> keyset = tables.keySet();
            keys = new ArrayList<TableReference>(keyset.size());
            keys.addAll(keyset);
        }
        return keys;
    }

    public TableReference getSpaceSummary(String spaceName) {
        final TableReference ref = new TableReference(spaceName);
        return getSpaceSummary(ref);
    }

    public TableReference getSpaceSummary(TableReference ref) {
        final SymbolTable space = getSpace(ref);
        if (space == null) {
            throw new NotFoundException();
        }
        return new TableReference(space.getId(), ref.getName());
    }

    public SymbolTable getSpace(String spaceName) {
        return getSpace(new TableReference(spaceName));
    }

    public SymbolTable getSpace(TableReference ref) {
        return tables.get(ref);
    }
}
