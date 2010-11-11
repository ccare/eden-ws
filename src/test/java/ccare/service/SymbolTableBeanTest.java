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

import ccare.domain.Observable;
import ccare.symboltable.SymbolReference;
import org.junit.Ignore;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * User: carecx
 * Date: 13-Oct-2010
 * Time: 22:33:38
 */

public class SymbolTableBeanTest {

    private SymbolTableService service = new SymbolTableBean();

    @Test
    public void testBeanIsSingleton() throws NamingException {
        SymbolTableService serviceBean = service();
        assertNotNull(serviceBean);
        SymbolTableService serviceBean2 = service();
        assertEquals(serviceBean.getId(), serviceBean2.getId());
    }

    @Test
    public void testSetAndGetSymbol() throws NamingException {
        SymbolTableService serviceBean = service();
        Observable def = new Observable();
        def.setDefinition("#a+#b");
        SymbolReference r = new SymbolReference();
        serviceBean.define(r, def);
        Observable o = serviceBean.observe(r);
        assertEquals(o.getDefinition(), "#a+#b");
    }

    @Test
    public void testListSymbol() throws NamingException {
        SymbolTableService serviceBean = service();
        Observable def = new Observable();
        def.setDefinition("a+b");
        SymbolReference r = new SymbolReference();
        serviceBean.define(r, def);
        assertTrue(serviceBean.listSymbols().contains(r));
    }

//    private InitialContext createContext() {
//        Properties props = new Properties();
//        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
//        InitialContext context = null;
//        try {
//            context = new InitialContext(props);
//        } catch (NamingException e) {
//            throw new RuntimeException(e);
//        }
//        return context;
//    }

    private SymbolTableService service() throws NamingException {
        return service;
        //InitialContext context = createContext();
        //return (SymbolTableService) context.lookup("SymbolTableBeanLocal");
    }

}
