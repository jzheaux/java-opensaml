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

package org.opensaml.xmlsec.encryption.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.xmlsec.encryption.CipherData;
import org.opensaml.xmlsec.encryption.EncryptedType;
import org.opensaml.xmlsec.encryption.EncryptionMethod;
import org.opensaml.xmlsec.encryption.EncryptionProperties;
import org.opensaml.xmlsec.signature.KeyInfo;

/**
 * Abstract implementation of {@link org.opensaml.xmlsec.encryption.EncryptedType}.
 */
public abstract class EncryptedTypeImpl extends AbstractXMLObject implements EncryptedType {
    
    /** id attribute value. */
    private String id;
    
    /** Type attribute value. */
    private String type;
    
    /** MimeType attribute value. */
    private String mimeType;
    
    /** Encoding attribute value. */
    private String encoding;
    
    /** EncryptionMethod child element. */
    private EncryptionMethod encryptionMethod;
    
    /** EncryptionMethod child element. */
    private KeyInfo keyInfo;
    
    /** CipherData child element. */
    private CipherData cipherData;
    
    /** EncryptionProperties child element. */
    private EncryptionProperties encryptionProperties;
    
    /**
     * Constructor.
     * 
     * @param namespaceURI namespace URI
     * @param elementLocalName local name
     * @param namespacePrefix namespace prefix
     */
    protected EncryptedTypeImpl(final String namespaceURI, final String elementLocalName,
            final String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }
    
    /** {@inheritDoc} */
    public String getID() {
        return this.id;
    }
    
    /** {@inheritDoc} */
    public void setID(final String newID) {
        final String oldID = this.id;
        this.id = prepareForAssignment(this.id, newID);
        registerOwnID(oldID, this.id);
    }
    
    /** {@inheritDoc} */
    public String getType() {
        return this.type;
    }
    
    /** {@inheritDoc} */
    public void setType(final String newType) {
        this.type = prepareForAssignment(this.type, newType);
    }
    
    /** {@inheritDoc} */
    public String getMimeType() {
        return this.mimeType;
    }
    
    /** {@inheritDoc} */
    public void setMimeType(final String newMimeType) {
        this.mimeType = prepareForAssignment(this.mimeType, newMimeType);
    }
    
    /** {@inheritDoc} */
    public String getEncoding() {
        return this.encoding;
    }
    
    /** {@inheritDoc} */
    public void setEncoding(final String newEncoding) {
        this.encoding = prepareForAssignment(this.encoding, newEncoding);
    }

    /** {@inheritDoc} */
    public EncryptionMethod getEncryptionMethod() {
        return this.encryptionMethod;
    }

    /** {@inheritDoc} */
    public void setEncryptionMethod(final EncryptionMethod newEncryptionMethod) {
        this.encryptionMethod = prepareForAssignment(this.encryptionMethod, newEncryptionMethod);
    }

    /** {@inheritDoc} */
    public KeyInfo getKeyInfo() {
        return this.keyInfo;
    }

    /** {@inheritDoc} */
    public void setKeyInfo(final KeyInfo newKeyInfo) {
        this.keyInfo = prepareForAssignment(this.keyInfo, newKeyInfo);
    }

    /** {@inheritDoc} */
    public CipherData getCipherData() {
        return this.cipherData;
    }

    /** {@inheritDoc} */
    public void setCipherData(final CipherData newCipherData) {
        this.cipherData = prepareForAssignment(this.cipherData, newCipherData);
    }

    /** {@inheritDoc} */
    public EncryptionProperties getEncryptionProperties() {
        return this.encryptionProperties;
    }

    /** {@inheritDoc} */
    public void setEncryptionProperties(final EncryptionProperties newEncryptionProperties) {
        this.encryptionProperties = prepareForAssignment(this.encryptionProperties, newEncryptionProperties);
    }

    /** {@inheritDoc} */
    public List<XMLObject> getOrderedChildren() {
        final ArrayList<XMLObject> children = new ArrayList<>();
        
        if (encryptionMethod != null) {
            children.add(encryptionMethod);
        }
        if (keyInfo != null) {
            children.add(keyInfo);
        }
        if (cipherData != null) {
            children.add(cipherData);
        }
        if (encryptionProperties!= null) {
            children.add(encryptionProperties);
        }
        
        if (children.size() == 0) {
           return null;
        }
        
        return Collections.unmodifiableList(children);
    }

}