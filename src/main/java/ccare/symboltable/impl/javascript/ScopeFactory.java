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

package ccare.symboltable.impl.javascript;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import ccare.symboltable.SymbolReference;
import ccare.symboltable.SymbolTable;

/**
 * User: carecx
 * Date: 27-Oct-2010
 * Time: 09:46:35
 */
public class ScopeFactory {

    Scriptable rootScope;
    static ScopeFactory instance;

    public static ScopeFactory getInstance() {
        if (instance == null) {
            synchronized (ScopeFactory.class) {
                instance = new ScopeFactory();
            }
        }
        return instance;
    }

    private Scriptable getRootScope() {
        if (rootScope == null) {
            synchronized (this) {
                Context cx = Context.enter();
                try {
                    // New scope / runtime env
                    ScriptableObject sharedScope = cx.initStandardObjects();
                    sharedScope.sealObject();
                    rootScope = sharedScope;
                } finally {
                    Context.exit();
                }
            }
        }
        return rootScope;
    }

    private Scriptable createScope() {
        Context cx = Context.enter();
        try {
            // New scope / runtime env
            Scriptable sharedScope = getRootScope();
            Scriptable newScope = cx.newObject(sharedScope);
            return newScope;
        } finally {
            Context.exit();
        }
    }

    public Scriptable scopeFor(final SymbolTable t) {
        final Scriptable scope = createScope();
        Function f = createObserveFunction(t);
        ScriptableObject.putProperty(scope, "$eden_observe", f);
        Function g = createDefineFunction(t);
        ScriptableObject.putProperty(scope, "$eden_define", g);
        Function xsl = XmlProcessingSupport.createTransformerFactoryFunction();
        ScriptableObject.putProperty(scope, "XSL", xsl);

        return scope;
    }

    private Function createDefineFunction(SymbolTable t) {
        return createObserveFunction(t);
    }

    private Function createObserveFunction(final SymbolTable t) {
        return new EmptyFunction() {

            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                if (objects.length == 1) {
                    final Object o = objects[0];
                    final String str = o.toString();
                    final SymbolReference ref = new SymbolReference(str);
                    final Object value = t.getValue(ref);
                    return value;
                } else if (objects.length == 2) {
                    final Object sym = objects[0];
                    final Object defn = objects[1];
                    final String name = sym.toString();
                    final SymbolReference ref = new SymbolReference(name);
                    t.define(ref, defn.toString());
                    return null;
                }
                return Undefined.instance;
            }

        };
    }


}
