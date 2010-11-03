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

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.xml.XMLObject;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;

import static ccare.symboltable.impl.javascript.RuntimeUtils.evalExpression;
import static ccare.symboltable.impl.javascript.XmlProcessingSupport.*;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: carecx
 * Date: 03-Nov-2010
 * Time: 17:38:29
 * To change this template use File | Settings | File Templates.
 */
public class XmlProcessingSupportTest {

    private final String xsl = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">\n" +
            "<xsl:param name=\"myParam\" select=\"'Hi'\" />" +
            "    <xsl:template match=\"/\">" +
            "<xml><xsl:value-of select=\"$myParam\" /></xml>" +
            "</xsl:template>" +
            "</xsl:stylesheet>";

    private final TransformerFactory factory = TransformerFactory.newInstance();
    private final String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<xml>Hi</xml>";

    @Test(expected = RuntimeException.class)
    public void testCreateTransform() throws IOException, SAXException, TransformerException {
        Function f = createTransformFunction(factory, "");
        assertNotNull(f);
    }

    @Test
    public void testCreateAndRunTransform() throws IOException, SAXException, TransformerException {
        Function f = createTransformFunction(factory, xsl);
        Object transformed = f.call(null, null, null, new Object[]{"<xml/>"});
        assertEquals(result, transformed);
    }

    @Test
    public void testCreateAndRunTransformAsECMA() throws IOException, SAXException, TransformerException {
        Function f = createTransformFunction(factory, xsl);
        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();
            XMLObject transformed = (XMLObject) f.call(cx, scope, null, new Object[]{"<xml/>"});
            final String target = "<xml>Hi</xml>";
            assertEquals(target, toXMLString(transformed));
        } finally {
            Context.exit();
        }
    }

    @Test
    public void testCreateAndRunTransformAsECMAWithArgs() throws IOException, SAXException, TransformerException {

        Function f = createTransformFunction(factory, xsl);
        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();

            final Scriptable params = cx.newObject(scope);
            params.put("myParam", params, "hello there");

            XMLObject transformed = (XMLObject) f.call(cx, scope, null, new Object[]{"<xml/>", params});
            final String target = "<xml>hello there</xml>";
            assertEquals(target, toXMLString(transformed));
        } finally {
            Context.exit();
        }
    }

    @Test
    public void testCreateAndRunTransformAgainstE4XInput() throws IOException, SAXException, TransformerException {
        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();
            final Object xml = evalExpression(cx, scope, "<foo><bar/></foo>");
            final String xslString = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">\n" +
                    "    <xsl:template match=\"foo\">" +
                    "<xml>bar</xml>" +
                    "</xsl:template>" +
                    "</xsl:stylesheet>";
            final Object xsl = cx.evaluateString(scope, xslString, "", 0, null);
            final Function f = createTransformFunction(factory, xsl);
            XMLObject transformed = (XMLObject) f.call(cx, scope, null, new Object[]{xml});
            final String target = "<xml>bar</xml>";
            assertEquals(target, toXMLString(transformed));
        } finally {
            Context.exit();
        }
    }

    @Test
    public void testRemoveProcessingInstruction() {
        final String target = "<xml>Hi</xml>";
        assertEquals(target, removeProcessingInstruction(target));
        assertEquals(target, removeProcessingInstruction(result));
    }


}
