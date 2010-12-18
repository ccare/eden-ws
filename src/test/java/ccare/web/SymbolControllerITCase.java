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

package ccare.web;

import org.junit.Ignore;

/**
 * User: carecx Date: 13-Oct-2010 Time: 12:59:36
 */
@Ignore
public class SymbolControllerITCase extends IntegrationSupport {

	// @Test
	// public void testGetStatus() throws Exception {
	// WebResource resource = resource("spaces");
	// SpaceSummary spaces = resource.get(SpaceSummary.class);
	// spaces.setCount(1);
	// assertEquals(new Integer(1), spaces.getCount());
	// }
	//
	//
	// WebResource webResource = resource();
	//
	//
	// public SymbolControllerITCase() throws Exception {
	// super("ccare.web");
	// }
	//
	// @Test
	// public void testGetSummary() throws Exception {
	// String responseMsg = webResource.path("symbols").get(String.class);
	// assertEquals("All symbols served from here", responseMsg);
	// }
	//
	// @Test
	// public void testGetSymbolValue() throws Exception {
	// String resp = webResource.path("symbols/a").get(String.class);
	// assertEquals("eval(B + C)", resp);
	// }
	//
	// @Test
	// public void testNestedSymbolValue() throws Exception {
	// String resp = webResource.path("symbols/a/b").get(String.class);
	// assertEquals("eval(B + C)", resp);
	// }
	//
	// @Test
	// public void testNestedSymbolValueBySelector() throws Exception {
	// String resp = webResource.path("symbols/a/b:value").get(String.class);
	// assertEquals("eval(B + C)", resp);
	// }
	//
	// @Test
	// public void testGetSymbolDefn() throws Exception {
	// Observable resp =
	// webResource.path("symbols/a:definition").get(Observable.class);
	// assertEquals("B + C", resp.getDefinition());
	// }
	//
	// @Test
	// public void testGetSymbolAccessInfo() throws Exception {
	// SecurityDescriptor access =
	// webResource.path("symbols/a:access").get(SecurityDescriptor.class);
	// assertEquals(SecurityDescriptor.ALLOW_ALL, access);
	// }
	//
	// @Test
	// public void testGetHeadInfo() throws Exception {
	// String resp = webResource.path("symbols/a").options(String.class);
	// System.out.println(resp);
	// }
	//

}
