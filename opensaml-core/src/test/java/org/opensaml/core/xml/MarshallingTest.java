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

package org.opensaml.core.xml;

import org.testng.annotations.Test;
import org.testng.Assert;
import javax.xml.namespace.QName;

import net.shibboleth.utilities.java.support.xml.XMLParserException;

import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.mock.SimpleXMLObject;
import org.opensaml.core.xml.mock.SimpleXMLObjectBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

/**
 * Unit test for marshalling functions.
 */
public class MarshallingTest extends XMLObjectBaseTestCase {

    /** QName for SimpleXMLObject */
    private QName simpleXMLObjectQName;

    /**
     * Constructor
     */
    public MarshallingTest() {
        simpleXMLObjectQName = new QName(SimpleXMLObject.NAMESPACE, SimpleXMLObject.LOCAL_NAME);
    }

    /**
     * Tests marshalling an object that has DOM Attrs.
     * 
     * @throws XMLParserException ...
     */
    @Test
    public void testMarshallingWithAttributes() throws XMLParserException {
        String expectedId = "Firefly";
        String expectedDocumentLocation = "/org/opensaml/core/xml/SimpleXMLObjectWithAttribute.xml";
        Document expectedDocument = parserPool.parse(MarshallingTest.class
                .getResourceAsStream(expectedDocumentLocation));

        SimpleXMLObjectBuilder sxoBuilder = (SimpleXMLObjectBuilder) builderFactory.getBuilder(simpleXMLObjectQName);
        SimpleXMLObject sxObject = sxoBuilder.buildObject();
        sxObject.setId(expectedId);

        assertXMLEquals(expectedDocument, sxObject);
        Assert.assertNotNull(sxObject.getDOM(), "DOM was not cached after marshalling");
    }

    /**
     * Tests marshalling an object that has DOM Element textual content.
     * 
     * @throws XMLParserException ...
     */
    @Test
    public void testMarshallingWithElementContent() throws XMLParserException {
        String expectedDocumentLocation = "/org/opensaml/core/xml/SimpleXMLObjectWithContent.xml";
        Document expectedDocument = parserPool.parse(MarshallingTest.class
                .getResourceAsStream(expectedDocumentLocation));

        SimpleXMLObjectBuilder sxoBuilder = (SimpleXMLObjectBuilder) builderFactory.getBuilder(simpleXMLObjectQName);

        SimpleXMLObject sxObject = sxoBuilder.buildObject();

        SimpleXMLObject child1 = sxoBuilder.buildObject();
        child1.setValue("Content1");
        sxObject.getSimpleXMLObjects().add(child1);

        SimpleXMLObject child2 = sxoBuilder.buildObject();
        child2.setValue("Content2");
        sxObject.getSimpleXMLObjects().add(child2);

        SimpleXMLObject child3 = sxoBuilder.buildObject();
        sxObject.getSimpleXMLObjects().add(child3);

        SimpleXMLObject grandchild1 = sxoBuilder.buildObject();
        grandchild1.setValue("Content3");
        child3.getSimpleXMLObjects().add(grandchild1);

        assertXMLEquals(expectedDocument, sxObject);
        Assert.assertNotNull(sxObject.getDOM(), "DOM was not cached after marshalling");
    }

    /**
     * Tests marshalling an object that has DOM Element children
     * 
     * @throws XMLParserException ...
     * @throws MarshallingException ...
     */
    @Test
    public void testMarshallingWithChildElements() throws XMLParserException, MarshallingException {
        String expectedDocumentLocation = "/org/opensaml/core/xml/SimpleXMLObjectWithChildren.xml";
        Document expectedDocument = parserPool.parse(MarshallingTest.class
                .getResourceAsStream(expectedDocumentLocation));

        SimpleXMLObjectBuilder sxoBuilder = (SimpleXMLObjectBuilder) builderFactory.getBuilder(simpleXMLObjectQName);
        SimpleXMLObject sxObject = sxoBuilder.buildObject();
        SimpleXMLObject sxObjectChild1 = sxoBuilder.buildObject();
        SimpleXMLObject sxObjectChild2 = sxoBuilder.buildObject();
        sxObject.getSimpleXMLObjects().add(sxObjectChild1);
        sxObject.getSimpleXMLObjects().add(sxObjectChild2);

        assertXMLEquals(expectedDocument, sxObject);
        Assert.assertNotNull(sxObject.getDOM(), "DOM was not cached after marshalling");
    }

    /**
     * Tests marshalling a fragment of an already marshalled tree into an existing, but different, DOM tree.
     * 
     * @throws XMLParserException ...
     * @throws MarshallingException ...
     */
    @Test
    public void testMarshallingXMLFragment() throws XMLParserException, MarshallingException {
        String expectedDocumentLocation = "/org/opensaml/core/xml/SOAPMessageWithContent.xml";
        String soapDocLocation = "/org/opensaml/core/xml/SOAPMessage.xml";
        Document soapDoc = parserPool.parse(MarshallingTest.class.getResourceAsStream(soapDocLocation));
        Element soapBody = (Element) soapDoc.getDocumentElement().getElementsByTagNameNS(
                "http://schemas.xmlsoap.org/soap/envelope/", "Body").item(0);
        
        SimpleXMLObjectBuilder sxoBuilder = (SimpleXMLObjectBuilder) builderFactory.getBuilder(simpleXMLObjectQName);
        
        SimpleXMLObject response = sxoBuilder.buildObject(SimpleXMLObject.NAMESPACE, "Response", SimpleXMLObject.NAMESPACE_PREFIX);
        SimpleXMLObject statement = sxoBuilder.buildObject(SimpleXMLObject.NAMESPACE, "Statement", SimpleXMLObject.NAMESPACE_PREFIX);
        response.getSimpleXMLObjects().add(statement);
        
        // Marshall it once so the DOM is cached
        Marshaller marshaller = marshallerFactory.getMarshaller(simpleXMLObjectQName);
        marshaller.marshall(response);
        Assert.assertNotNull(response.getDOM(), "DOM was not cached after marshalling");
        
        // Marshall statement (with cached DOM) into SOAP Body element child
        Document expectedDocument = parserPool.parse(MarshallingTest.class.getResourceAsStream(expectedDocumentLocation));
        Element statementElem = marshaller.marshall(statement, soapBody);
        final Diff diff = DiffBuilder.compare(statementElem.getOwnerDocument()).withTest(expectedDocument)
                .checkForIdentical().build();
        Assert.assertFalse(diff.hasDifferences(), diff.toString());
        Assert.assertNull(response.getDOM(), "Parent of XML fragment DOM was not invalidated during marshalling");
        Assert.assertNotNull(statement.getDOM(), "XML fragment DOM was invalidated during marshalling");
    }
    
    /**
     * Tests marshalling into an existing new empty document. Marshalled DOM should become the 
     * new root element of the document.
     * 
     * @throws XMLParserException ...
     * @throws MarshallingException ...
     */
    @Test
    public void testMarshallingExistingEmptyDocument() throws XMLParserException, MarshallingException {
        Document document = parserPool.newDocument();
        Assert.assertNull(document.getDocumentElement(), "Incorrect document root");
        
        SimpleXMLObject sxo = (SimpleXMLObject) buildXMLObject(SimpleXMLObject.ELEMENT_NAME);
        sxo.setId("idValue");
        
        marshallerFactory.getMarshaller(sxo).marshall(sxo, document);
        Assert.assertNotNull(document.getDocumentElement(), "Incorrect document root");
        Assert.assertTrue(document.getDocumentElement().isSameNode(sxo.getDOM()), "Incorrect document root");
    }
    
    /**
     * Tests marshalling into an existing document which already has a document root element.  Existing
     * root element should be replaced.
     * 
     * @throws XMLParserException ...
     * @throws MarshallingException ...
     */
    @Test
    public void testMarshallingReplaceDocumentRoot() throws XMLParserException, MarshallingException {
        Document document = parserPool.newDocument();
        Element element = document.createElementNS(null, "Foo");
        document.appendChild(element);
        Assert.assertTrue(document.getDocumentElement().isSameNode(element), "Incorrect document root");
        
        SimpleXMLObject sxo = (SimpleXMLObject) buildXMLObject(SimpleXMLObject.ELEMENT_NAME);
        sxo.setId("idValue");
        
        marshallerFactory.getMarshaller(sxo).marshall(sxo, document);
        Assert.assertFalse(document.getDocumentElement().isSameNode(element), "Document root should have been replaced");
        Assert.assertTrue(document.getDocumentElement().isSameNode(sxo.getDOM()), "Incorrect document root");
    }
}
