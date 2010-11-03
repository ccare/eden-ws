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

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: carecx
 * Date: 03-Nov-2010
 * Time: 20:38:10
 * To change this template use File | Settings | File Templates.
 */
public class XmlProcessingSupport {

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

    static Function createTransformFunction(final TransformerFactory factory, final String xslSource) {
        final StreamSource src = new StreamSource(IOUtils.toInputStream(xslSource));
        final Transformer trans;
        try {
            trans = factory.newTransformer(src);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }

        return new Function() {

            @Override
            public Object call(Context context, Scriptable scope, Scriptable scriptable1, Object[] objects) {
                final String input;
                if (objects.length > 0) {
                    input = objects[0].toString();
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

            @Override
            public Scriptable construct(Context context, Scriptable scriptable, Object[] objects) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getClassName() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Object get(String s, Scriptable scriptable) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Object get(int i, Scriptable scriptable) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean has(String s, Scriptable scriptable) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean has(int i, Scriptable scriptable) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void put(String s, Scriptable scriptable, Object o) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void put(int i, Scriptable scriptable, Object o) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void delete(String s) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void delete(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Scriptable getPrototype() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setPrototype(Scriptable scriptable) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Scriptable getParentScope() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setParentScope(Scriptable scriptable) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Object[] getIds() {
                return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Object getDefaultValue(Class<?> aClass) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean hasInstance(Scriptable scriptable) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    static String removeProcessingInstruction(String xml) {
        return PROCESSING_INSTRUCT_PATTERN.matcher(xml).replaceFirst("");
    }
}
