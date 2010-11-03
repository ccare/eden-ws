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

/**
 * Created by IntelliJ IDEA.
 * User: carecx
 * Date: 03-Nov-2010
 * Time: 22:10:41
 * To change this template use File | Settings | File Templates.
 */
public abstract class EmptyFunction implements Function {      

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
}
