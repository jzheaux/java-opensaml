/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.saml.saml1.binding.decoding.impl;

import net.shibboleth.utilities.java.support.xml.SerializeSupport;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml1.binding.decoding.impl.HTTPSOAP11Decoder;
import org.opensaml.saml.saml1.core.Request;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test case for SAML 1.X HTTP SOAP 1.1 message decoder.
 */
public class HTTPSOAP11DecoderTest extends XMLObjectBaseTestCase {
    
    private HTTPSOAP11Decoder decoder;
    
    private MockHttpServletRequest httpRequest;
    
    @BeforeMethod
    protected void setUp() throws Exception {
        httpRequest = new MockHttpServletRequest();
        httpRequest.setMethod("POST");
        httpRequest.setContentType("text/xml; charset=utf-8");
        
        decoder = new HTTPSOAP11Decoder();
        decoder.setParserPool(parserPool);
        decoder.setHttpServletRequest(httpRequest);
        decoder.initialize();
    }

    /**
     * Tests decoding a SOAP 1.1 message.
     * 
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDecoding() throws Exception {
        String requestContent = "<soap11:Envelope xmlns:soap11=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soap11:Body><saml:Request IssueInstant=\"1970-01-01T00:00:00.000Z\" MajorVersion=\"1\" "
                + "MinorVersion=\"1\" RequestID=\"foo\" xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:protocol\"/>"
                + "</soap11:Body></soap11:Envelope>";
        httpRequest.setContent(requestContent.getBytes());
        
        decoder.decode();
        MessageContext messageContext = decoder.getMessageContext();

        Assert.assertNotNull(messageContext.getSubcontext(SOAP11Context.class).getEnvelope());
        Assert.assertTrue(messageContext.getMessage() instanceof Request);
    }
    
    protected String encodeMessage(XMLObject message) throws MarshallingException {
        marshallerFactory.getMarshaller(message).marshall(message);
        return SerializeSupport.nodeToString(message.getDOM());
    }
}
