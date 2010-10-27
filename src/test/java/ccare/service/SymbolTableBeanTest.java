package ccare.service;

import ccare.domain.Observable;
import ccare.domain.SymbolReference;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * User: carecx
 * Date: 13-Oct-2010
 * Time: 22:33:38
 */
public class SymbolTableBeanTest {

    @Test
    public void testBeanIsSingleton() throws NamingException {
        InitialContext context = createContext();
        SymbolTableService serviceBean = (SymbolTableService) context.lookup("SymbolTableBeanLocal");
        assertNotNull(serviceBean);
        SymbolTableService serviceBean2 = (SymbolTableService) context.lookup("SymbolTableBeanLocal");
        assertEquals(serviceBean.getId(), serviceBean2.getId());
    }

    @Test
    public void testSetAndGetSymbol() throws NamingException {
        InitialContext context = createContext();
        SymbolTableService serviceBean = (SymbolTableService) context.lookup("SymbolTableBeanLocal");
        Observable def = new Observable();
        def.setDefinition("a+b");
        SymbolReference r = new SymbolReference();
        serviceBean.define(r, def);
        Observable o = serviceBean.observe(r);
        assertEquals(o.getDefinition(), "a+b");
    }

    @Test
    public void testListSymbol() throws NamingException {
        InitialContext context = createContext();
        SymbolTableService serviceBean = (SymbolTableService) context.lookup("SymbolTableBeanLocal");
        Observable def = new Observable();
        def.setDefinition("a+b");
        SymbolReference r = new SymbolReference();
        serviceBean.define(r, def);
        assertThat(serviceBean.listSymbols(), hasItem(r));
    }

    private InitialContext createContext() {
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        InitialContext context = null;
        try {
            context = new InitialContext(props);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return context;
    }

}
