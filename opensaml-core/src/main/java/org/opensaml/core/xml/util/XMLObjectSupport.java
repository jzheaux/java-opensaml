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

package org.opensaml.core.xml.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import net.shibboleth.utilities.java.support.xml.QNameSupport;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.Namespace;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLRuntimeException;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A helper class for working with XMLObjects.
 */
public final class XMLObjectSupport {
    
    /** Options for handling output of XMLObject cloning. */
    public enum CloneOutputOption {
        
        /** Completely and recursively drop the DOM from the cloned object and its children. */
        DropDOM,
        
        /** The cloned XMLObject's DOM will be the root document element of a new {@link Document},
         * that is it will be the {@link Element} returned by {@link Document#getDocumentElement()}. */
        RootDOMInNewDocument,
        
        /** The cloned XMLObject's DOM will be owned by the same {@link Document} as the input object
         * (the latter possibly newly created by marshalling internally), but will not be connected 
         * to the node tree associated with the {@link Document#getDocumentElement()}.*/
        UnrootedDOM,
    }
    
    /** Constructor. */
    private XMLObjectSupport() { }
    
    /**
     * Clone an XMLObject by brute force:
     * 
     * <p>
     * 1) Marshall the original object if necessary
     * 2) Clone the resulting DOM Element
     * 3) Unmarshall a new XMLObject tree around it.
     * </p>
     * 
     * <p>
     * This method variant is equivalent to 
     * <code>cloneXMLObject(originalXMLObject, CloneOutputOption.DropDOM).</code>
     * </p>
     * 
     * 
     * @param originalXMLObject the object to be cloned
     * @param <T> the type of object being cloned
     * 
     * @return a clone of the original object
     * 
     * @throws MarshallingException if original object can not be marshalled
     * @throws UnmarshallingException if cloned object tree can not be unmarshalled
     */
    public static <T extends XMLObject> T cloneXMLObject(final T originalXMLObject)
            throws MarshallingException, UnmarshallingException {
        return cloneXMLObject(originalXMLObject, CloneOutputOption.DropDOM);
    }
        
    /**
     * Clone an XMLObject by brute force:
     * 
     * <p>
     * 1) Marshall the original object if necessary
     * 2) Clone the resulting DOM Element
     * 3) Unmarshall a new XMLObject tree around it.
     * </p>
     * 
     * @param originalXMLObject the object to be cloned
     * @param cloneOutputOption  the option for handling the cloned object output
     * @param <T> the type of object being cloned
     * 
     * @return a clone of the original object
     * 
     * @throws MarshallingException if original object can not be marshalled
     * @throws UnmarshallingException if cloned object tree can not be unmarshalled
     */
    @Nullable public static <T extends XMLObject> T cloneXMLObject(@Nullable final T originalXMLObject,
            @Nonnull final CloneOutputOption cloneOutputOption) throws MarshallingException, UnmarshallingException {
        
        if (originalXMLObject == null) {
            return null;
        }
        
        final Marshaller marshaller = getMarshaller(originalXMLObject);
        if (marshaller == null) {
            throw new MarshallingException("Unable to obtain Marshaller for XMLObject: "
                    + originalXMLObject.getElementQName());
        }
        final Element origElement = marshaller.marshall(originalXMLObject);
        
        Element clonedElement = null;
        
        switch (cloneOutputOption) {
            case RootDOMInNewDocument:
                try {
                    final Document newDocument = XMLObjectProviderRegistrySupport.getParserPool().newDocument();
                    // Note: importNode copies the node tree and does not modify the source document
                    clonedElement = (Element) newDocument.importNode(origElement, true);
                    newDocument.appendChild(clonedElement);
                } catch (final XMLParserException e) {
                    throw new XMLRuntimeException("Error obtaining new Document from parser pool", e);
                }
                break;
            case UnrootedDOM:
            case DropDOM:
                clonedElement = (Element) origElement.cloneNode(true);
                break;
            default:
                throw new XMLRuntimeException("Saw unsupported value for CloneOutputOption enum: " + cloneOutputOption);
        }
        
        final Unmarshaller unmarshaller = getUnmarshaller(clonedElement);
        if (unmarshaller == null) {
            throw new UnmarshallingException("Unable to obtain Unmarshaller for element: "
                    + QNameSupport.getNodeQName(clonedElement));
        }
        
        final T clonedXMLObject = (T) unmarshaller.unmarshall(clonedElement);
        if (CloneOutputOption.DropDOM.equals(cloneOutputOption)) {
            clonedXMLObject.releaseDOM();
            clonedXMLObject.releaseChildrenDOM(true);
        }
        return clonedXMLObject;
    }
    
    /**
     * Unmarshall a Document from an InputSteam.
     * 
     * @param parserPool the ParserPool instance to use
     * @param inputStream the InputStream to unmarshall
     * @return the unmarshalled XMLObject
     * @throws XMLParserException if there is a problem parsing the input data
     * @throws UnmarshallingException if there is a problem unmarshalling the parsed DOM
     */
    public static XMLObject unmarshallFromInputStream(final ParserPool parserPool, final InputStream inputStream)
            throws XMLParserException, UnmarshallingException {
        final Logger log = getLogger();
        log.debug("Parsing InputStream into DOM document");

        try {
            final Document messageDoc = parserPool.parse(inputStream);
            final Element messageElem = messageDoc.getDocumentElement();

            if (log.isTraceEnabled()) {
                log.trace("Resultant DOM message was:");
                log.trace(SerializeSupport.nodeToString(messageElem));
            }

            log.debug("Unmarshalling DOM parsed from InputStream");
            final Unmarshaller unmarshaller = getUnmarshaller(messageElem);
            if (unmarshaller == null) {
                log.error("Unable to unmarshall InputStream, no unmarshaller registered for element "
                        + QNameSupport.getNodeQName(messageElem));
                throw new UnmarshallingException(
                        "Unable to unmarshall InputStream, no unmarshaller registered for element "
                                + QNameSupport.getNodeQName(messageElem));
            }

            final XMLObject message = unmarshaller.unmarshall(messageElem);

            log.debug("InputStream succesfully unmarshalled");
            return message;
        } catch (final RuntimeException e) {
            throw new UnmarshallingException("Fatal error unmarshalling XMLObject", e);
        }
    }
    
    /**
     * Unmarshall a Document from a Reader.
     * 
     * @param parserPool the ParserPool instance to use
     * @param reader the Reader to unmarshall
     * @return the unmarshalled XMLObject
     * @throws XMLParserException if there is a problem parsing the input data
     * @throws UnmarshallingException if there is a problem unmarshalling the parsed DOM
     */
    public static XMLObject unmarshallFromReader(final ParserPool parserPool, final Reader reader)
            throws XMLParserException, UnmarshallingException {
        final Logger log = getLogger();
        log.debug("Parsing Reader into DOM document");
        

        try {
            final Document messageDoc = parserPool.parse(reader);
            final Element messageElem = messageDoc.getDocumentElement();

            if (log.isTraceEnabled()) {
                log.trace("Resultant DOM message was:");
                log.trace(SerializeSupport.nodeToString(messageElem));
            }

            log.debug("Unmarshalling DOM parsed from Reader");
            final Unmarshaller unmarshaller = getUnmarshaller(messageElem);
            if (unmarshaller == null) {
                log.error("Unable to unmarshall Reader, no unmarshaller registered for element "
                        + QNameSupport.getNodeQName(messageElem));
                throw new UnmarshallingException(
                        "Unable to unmarshall Reader, no unmarshaller registered for element "
                                + QNameSupport.getNodeQName(messageElem));
            }

            final XMLObject message = unmarshaller.unmarshall(messageElem);

            log.debug("Reader succesfully unmarshalled");
            return message;
        } catch (final RuntimeException e) {
            throw new UnmarshallingException("Fatal error unmarshalling XMLObject", e);
        }
    }

    /**
     * Marshall an XMLObject.  If the XMLObject already has a cached DOM via {@link XMLObject#getDOM()},
     * that Element will be returned.  Otherwise the object will be fully marshalled and that Element returned.
     * 
     * @param xmlObject the XMLObject to marshall
     * @return the marshalled Element
     * @throws MarshallingException if there is a problem marshalling the XMLObject
     */
    @Nonnull public static Element marshall(@Nonnull final XMLObject xmlObject) throws MarshallingException {
        final Logger log = getLogger();
        log.debug("Marshalling XMLObject");
        
        if (xmlObject.getDOM() != null) {
            log.debug("XMLObject already had cached DOM, returning that element");
            return xmlObject.getDOM();
        }

        final Marshaller marshaller = getMarshaller(xmlObject);
        if (marshaller == null) {
            log.error("Unable to marshall XMLObject, no marshaller registered for object: "
                    + xmlObject.getElementQName());
            throw new MarshallingException("Unable to marshall XMLObject, no marshaller registered for object: " 
                    + xmlObject.getElementQName());
        }
        
        final Element messageElem = marshaller.marshall(xmlObject);
        
        if (log.isTraceEnabled()) {
            log.trace("Marshalled XMLObject into DOM:");
            log.trace(SerializeSupport.nodeToString(messageElem));
        }
        
        return messageElem;
    }
    
    /**
     * Marshall an XMLObject to an OutputStream.
     * 
     * @param xmlObject the XMLObject to marshall
     * @param outputStream the OutputStream to which to marshall
     * @throws MarshallingException if there is a problem marshalling the object
     */
    public static void marshallToOutputStream(final XMLObject xmlObject, final OutputStream outputStream) 
            throws MarshallingException {
        final Element element = marshall(xmlObject);
        SerializeSupport.writeNode(element, outputStream);
    }
    
    /**
     * Get the namespace URI bound to the specified prefix within the scope of the specified
     * XMLObject.
     *
     * @param xmlObject the XMLObject from which to search
     * @param prefix the prefix to search
     * @return the namespace URI bound to the prefix, or none if not found
     */
    public static String lookupNamespaceURI(final XMLObject xmlObject, final String prefix) {
        XMLObject current = xmlObject;
        
        while (current != null) {
            for (final Namespace ns : current.getNamespaces()) {
                if (Objects.equals(ns.getNamespacePrefix(), prefix)) {
                    return ns.getNamespaceURI();
                }
            }
            current = current.getParent();
        }
        
        return null;
    }
    
    /**
     * Get the prefix bound to the specified namespace URI within the scope of the specified
     * XMLObject.
     *
     * @param xmlObject the XMLObject from which to search
     * @param namespaceURI the namespace URI to search
     * @return the prefix bound to the namespace URI, or none if not found
     */
    public static String lookupNamespacePrefix(final XMLObject xmlObject, final String namespaceURI) {
        XMLObject current = xmlObject;
        
        while (current != null) {
            for (final Namespace ns : current.getNamespaces()) {
                if (Objects.equals(ns.getNamespaceURI(), namespaceURI)) {
                    return ns.getNamespacePrefix();
                }
            }
            current = current.getParent();
        }
        
        return null;
    }
    
    /**
     * Get an SLF4J Logger.
     * 
     * @return a Logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(XMLObjectSupport.class);
    }

    /**
     * Marshall an attribute name and value to a DOM Element. This is particularly useful for attributes whose names
     * appear in namespace-qualified form.
     * 
     * @param attributeName the attribute name in QName form
     * @param attributeValues the attribute values
     * @param domElement the target element to which to marshall
     * @param isIDAttribute flag indicating whether the attribute being marshalled should be handled as an ID-typed
     *            attribute
     */
    public static void marshallAttribute(final QName attributeName, final List<String> attributeValues,
            final Element domElement, final boolean isIDAttribute) {
        marshallAttribute(attributeName, StringSupport.listToStringValue(attributeValues, " "), domElement,
                isIDAttribute);
    }

    /**
     * Marshall an attribute name and value to a DOM Element. This is particularly useful for attributes whose names
     * appear in namespace-qualified form.
     * 
     * @param attributeName the attribute name in QName form
     * @param attributeValue the attribute value
     * @param domElement the target element to which to marshall
     * @param isIDAttribute flag indicating whether the attribute being marshalled should be handled as an ID-typed
     *            attribute
     */
    public static void marshallAttribute(final QName attributeName, final String attributeValue,
            final Element domElement, final boolean isIDAttribute) {
        final Document document = domElement.getOwnerDocument();
        final Attr attribute = AttributeSupport.constructAttribute(document, attributeName);
        attribute.setValue(attributeValue);
        domElement.setAttributeNodeNS(attribute);
        if (isIDAttribute) {
            domElement.setIdAttributeNode(attribute, true);
        }
    }

    /**
     * Marshall the attributes represented by the indicated AttributeMap into the indicated DOM Element.
     * 
     * @param attributeMap the AttributeMap
     * @param domElement the target Element
     */
    public static void marshallAttributeMap(final AttributeMap attributeMap, final Element domElement) {
        final Document document = domElement.getOwnerDocument();
        Attr attribute = null;
        for (final Entry<QName, String> entry : attributeMap.entrySet()) {
            attribute = AttributeSupport.constructAttribute(document, entry.getKey());
            attribute.setValue(entry.getValue());
            domElement.setAttributeNodeNS(attribute);
            if (XMLObjectProviderRegistrySupport.isIDAttribute(entry.getKey()) ||
                    attributeMap.isIDAttribute(entry.getKey())) {
                domElement.setIdAttributeNode(attribute, true);
            }
        }
    }

    /**
     * Unmarshall a DOM Attr to an AttributeMap.
     * 
     * @param attributeMap the target AttributeMap
     * @param attribute the target DOM Attr
     */
    public static void unmarshallToAttributeMap(final AttributeMap attributeMap, final Attr attribute) {
        final QName attribQName = QNameSupport.constructQName(attribute.getNamespaceURI(), attribute.getLocalName(),
                attribute.getPrefix());
        attributeMap.put(attribQName, attribute.getValue());
        if (attribute.isId() || XMLObjectProviderRegistrySupport.isIDAttribute(attribQName)) {
            attributeMap.registerID(attribQName);
        }
    }
    
    /**
     * Build an XMLObject based on the element name.
     * 
     * @param elementName the element name
     * @return an XMLObject
     * @throws XMLRuntimeException if the required builder can not be obtained
     */
    public static XMLObject buildXMLObject(final QName elementName) {
        final XMLObjectBuilder<?> builder = getProviderRegistry().getBuilderFactory().getBuilderOrThrow(elementName);
        return builder.buildObject(elementName);
    }
    
    /**
     * Build an XMLObject based on the element nane and xsi:type.
     * 
     * @param elementName the element name
     * @param typeName the xsi:type
     * @return an XMLObject
     * @throws XMLRuntimeException if the required builder can not be obtained
     */
    public static XMLObject buildXMLObject(final QName elementName, final QName typeName) {
        final XMLObjectBuilder<?> builder = getProviderRegistry().getBuilderFactory().getBuilderOrThrow(elementName);
        return builder.buildObject(elementName, typeName);
    }
    
    /**
     * Obtain an XMLObject builder for the given QName.
     * 
     * @param typeOrName the element name or type
     * @return an XMLObject builder, or null if no provider registered
     */
    public static XMLObjectBuilder<?> getBuilder(final QName typeOrName) {
        return getProviderRegistry().getBuilderFactory().getBuilder(typeOrName);
    }
    
    /**
     * Obtain an XMLObject marshaller for the given QName.
     * 
     * @param typeOrName the element name or type
     * @return an XMLObject marshaller, or null if no provider registered
     */
    public static Marshaller getMarshaller(final QName typeOrName) {
        return getProviderRegistry().getMarshallerFactory().getMarshaller(typeOrName);
    }
    
    /**
     * Obtain an XMLObject marshaller for the given XMLObject.
     * 
     * @param xmlObject the XMLObject to be marshalled
     * @return an XMLObject marshaller, or null if no provider registered
     */
    public static Marshaller getMarshaller(final XMLObject xmlObject) {
        return getProviderRegistry().getMarshallerFactory().getMarshaller(xmlObject);
    }
    
    /**
     * Obtain an XMLObject unmarshaller for the given QName.
     * 
     * @param typeOrName the element name or type
     * @return an XMLObject unmarshaller, or null if no provider registered
     */
    public static Unmarshaller getUnmarshaller(final QName typeOrName) {
        return getProviderRegistry().getUnmarshallerFactory().getUnmarshaller(typeOrName);
    }
    
    /**
     * Obtain an XMLObject unmarshaller  for the given DOM Element.
     * 
     * @param element the DOM element
     * @return an XMLObject unmarshaller, or null if no provider registered
     */
    public static Unmarshaller getUnmarshaller(final Element element) {
        return getProviderRegistry().getUnmarshallerFactory().getUnmarshaller(element);
    }
    
    /**
     * Obtain the XMLObject provider registry.
     * 
     * @return the configured XMLObject provider registry
     * @throws XMLRuntimeException if the registry is not available
     */
    private static XMLObjectProviderRegistry getProviderRegistry() {
        final XMLObjectProviderRegistry registry = ConfigurationService.get(XMLObjectProviderRegistry.class);
        if (registry == null) {
            throw new XMLRuntimeException("XMLObjectProviderRegistry was not available from the ConfigurationService");
        }
        return registry;
    }
}