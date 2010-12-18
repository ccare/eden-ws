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

import static org.apache.commons.lang.Validate.notNull;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.xml.XMLObject;

/**
 * Created by IntelliJ IDEA.
 * User: carecx
 * Date: 03-Nov-2010
 * Time: 20:38:10
 * To change this template use File | Settings | File Templates.
 */
class XmlProcessingSupport {

    private static Pattern PROCESSING_INSTRUCT_PATTERN = Pattern.compile("^<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>");

    static String doTransform(final Transformer transformer, StreamSource inputSource, Map<String, Object> m) throws TransformerException {
        if (m != null) {
            for (String k : m.keySet()) {
                transformer.setParameter(k, m.get(k));
            }
        }
        final OutputStream bos = new ByteArrayOutputStream();
        final StreamResult result = new StreamResult(bos);
        transformer.transform(inputSource, result);

        final String transformedString = bos.toString();
        return transformedString;
    }

    static Function createTransformerFactoryFunction() {
        final TransformerFactory factory = TransformerFactory.newInstance();
        return new EmptyFunction() {

            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                if (objects.length == 1) {
                    return createTransformFunction(factory, objects[0]);
                }
                return Undefined.instance;
            }
        };

    }

    static Function createTransformFunction(final TransformerFactory factory, final Object xsl) {
        notNull(xsl);
        String xslString = getXMLStringFromObject(xsl);

        final StreamSource src = new StreamSource(IOUtils.toInputStream(xslString));
        final Transformer trans;
        try {
            trans = factory.newTransformer(src);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }

        return new EmptyFunction() {

            @Override
            public Object call(Context context, Scriptable scope, Scriptable scriptable1, Object[] objects) {
                final String input;
                if (objects.length > 0) {
                    input = getXMLStringFromObject(objects[0]);
                    final StreamSource inputSource = new StreamSource(IOUtils.toInputStream(input));
                    String result;
                    Map<String, Object> paramMap = null;
                    if (objects.length > 1) {
                        Object param2 = objects[1];
                        if (param2 instanceof Scriptable) {
                            Scriptable obj = (Scriptable) param2;
                            final Object[] keys = obj.getIds();
                            paramMap = new HashMap<String, Object>(keys.length);
                            for (Object o : keys) {
                                if (o instanceof String) {
                                    String key = (String) o;
                                    paramMap.put(key, obj.get(key, obj));
                                }
                            }
                        }
                    }

                    try {
                        result = doTransform(trans, inputSource, paramMap);
                    } catch (TransformerException e) {
                        throw new RuntimeException(e);
                    }
                    if (context != null && scope != null) {
                        return context.evaluateString(scope, "XML('" + removeProcessingInstruction(result) + "')", "cmd", 0, null);
                    } else {
                        return result;
                    }
                }
                return null;
            }

        };
    }

    private static String getXMLStringFromObject(Object xsl) {
        String xslString;
        if (xsl instanceof String) {
            xslString = (String) xsl;
        } else if (xsl instanceof XMLObject) {
            xslString = toXMLString((XMLObject) xsl);
        } else {
            xslString = xsl.toString();
        }
        return xslString;
    }

    static String removeProcessingInstruction(String xml) {
        return PROCESSING_INSTRUCT_PATTERN.matcher(xml).replaceFirst("");
    }

    static String toXMLString(XMLObject transformed) {
        final Object o = XMLObject.callMethod(transformed, "toXMLString", null);
        if (o instanceof String) {
            return (String) o;
        } else if (o == null) {
            return Undefined.instance.toString();
        } else {
            return o.toString();
        }
    }
}
