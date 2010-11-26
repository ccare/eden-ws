<!--
  ~ Copyright (c) 2010, Charles Care
  ~ All rights reserved.
  ~
  ~ Redistribution and use in source and binary forms, with or without modification,
  ~ are permitted provided that the following conditions are met:
  ~
  ~ * Redistributions of source code must retain the above copyright notice,
  ~ this list of conditions and the following disclaimer.
  ~ * Redistributions in binary form must reproduce the above copyright notice,
  ~ this list of conditions and the following disclaimer in the documentation
  ~ and/or other materials provided with the distribution.
  ~ * Neither the name of the author nor the names of its contributors
  ~ may be used to endorse or promote products derived from this software
  ~ without specific prior written permission.
  ~
  ~ THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  ~ AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  ~ IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ~ ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  ~ LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  ~ CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  ~ GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  ~ HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  ~ LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  ~ OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pom="http://maven.apache.org/POM/4.0.0"
                version="1.0">
    <!--

     DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

     Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.

     The contents of this file are subject to the terms of either the GNU
     General Public License Version 2 only ("GPL") or the Common Development
     and Distribution License("CDDL") (collectively, the "License").  You
     may not use this file except in compliance with the License. You can obtain
     a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
     or jersey/legal/LICENSE.txt.  See the License for the specific
     language governing permissions and limitations under the License.

     When distributing the software, include this License Header Notice in each
     file and include the License file at jersey/legal/LICENSE.txt.
     Sun designates this particular file as subject to the "Classpath" exception
     as provided by Sun in the GPL Version 2 section of the License file that
     accompanied this code.  If applicable, add the following below the License
     Header, with the fields enclosed by brackets [] replaced by your own
     identifying information: "Portions Copyrighted [year]
     [name of copyright owner]"

     Contributor(s):

     If you wish your version of this file to be governed by only the CDDL or
     only the GPL Version 2, indicate your decision by adding "[Contributor]
     elects to include this software in this distribution under the [CDDL or GPL
     Version 2] license."  If you don't indicate a single choice of license, a
     recipient has the option to distribute your version of this file under
     either the CDDL, the GPL Version 2 or to extend the choice of license to
     its licensees as provided above.  However, if you add GPL Version 2 code
     and therefore, elected the GPL Version 2 license, then the option applies
     only if the new code is made subject to such option by the copyright
     holder.
    -->
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template
            match="pom:dependency[pom:groupId='com.sun.jersey' or pom:groupId='com.sun.xml.bind' or pom:groupId='javax.servlet']/pom:scope[text()!=test]">
        <scope>provided</scope>
    </xsl:template>

    <xsl:template
            match="pom:dependency[pom:groupId='com.sun.jersey'  or pom:groupId='com.sun.xml.bind' or pom:groupId='javax.servlet']">
        <xsl:copy>
            <xsl:apply-templates/>
            <xsl:if test="count(pom:scope)=0">
                <scope>provided</scope>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="comment()">
        <xsl:comment>
            <xsl:value-of select="."/>
        </xsl:comment>
    </xsl:template>

    <xsl:template match="*">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
