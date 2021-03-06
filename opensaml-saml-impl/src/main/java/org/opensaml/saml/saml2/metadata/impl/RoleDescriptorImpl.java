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

package org.opensaml.saml.saml2.metadata.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.shibboleth.utilities.java.support.collection.LazyList;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.util.AttributeMap;
import org.opensaml.core.xml.util.XMLObjectChildrenList;
import org.opensaml.saml.common.AbstractSignableSAMLObject;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;

/** Concrete implementation of {@link org.opensaml.saml.saml2.metadata.RoleDescriptor}. */
public abstract class RoleDescriptorImpl extends AbstractSignableSAMLObject implements RoleDescriptor {

    /** ID attribute. */
    private String id;

    /** validUntil attribute. */
    private Instant validUntil;

    /** cacheDurection attribute. */
    private Duration cacheDuration;

    /** Set of supported protocols. */
    private final List<String> supportedProtocols;

    /** Error URL. */
    private String errorURL;

    /** Extensions child. */
    private Extensions extensions;

    /** Organization administering this role. */
    private Organization organization;

    /** "anyAttribute" attributes. */
    private final AttributeMap unknownAttributes;

    /** Contact persons for this role. */
    private final XMLObjectChildrenList<ContactPerson> contactPersons;

    /** Key descriptors for this role. */
    private final XMLObjectChildrenList<KeyDescriptor> keyDescriptors;

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected RoleDescriptorImpl(final String namespaceURI, final String elementLocalName,
            final String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
        unknownAttributes = new AttributeMap(this);
        supportedProtocols = new LazyList<>();
        contactPersons = new XMLObjectChildrenList<>(this);
        keyDescriptors = new XMLObjectChildrenList<>(this);
    }

    /** {@inheritDoc} */
    public String getID() {
        return id;
    }

    /** {@inheritDoc} */
    public void setID(final String newID) {
        final String oldID = this.id;
        this.id = prepareForAssignment(this.id, newID);
        registerOwnID(oldID, this.id);
    }

    /** {@inheritDoc} */
    public boolean isValid() {
        if (null == validUntil) {
            return true;
        }
        
        return Instant.now().isBefore(validUntil);
    }

    /** {@inheritDoc} */
    public Instant getValidUntil() {
        return validUntil;
    }

    /** {@inheritDoc} */
    public void setValidUntil(final Instant dt) {
        validUntil = prepareForAssignment(validUntil, dt);
    }

    /** {@inheritDoc} */
    public Duration getCacheDuration() {
        return cacheDuration;
    }

    /** {@inheritDoc} */
    public void setCacheDuration(final Duration duration) {
        cacheDuration = prepareForAssignment(cacheDuration, duration);
    }

    /** {@inheritDoc} */
    public List<String> getSupportedProtocols() {
        return Collections.unmodifiableList(supportedProtocols);
    }

    /** {@inheritDoc} */
    public boolean isSupportedProtocol(final String protocol) {
        return supportedProtocols.contains(protocol);
    }

    /** {@inheritDoc} */
    public void addSupportedProtocol(final String protocol) {
        final String trimmed = StringSupport.trimOrNull(protocol);
        if (trimmed != null && !supportedProtocols.contains(trimmed)) {
            releaseThisandParentDOM();
            supportedProtocols.add(trimmed);
        }
    }

    /** {@inheritDoc} */
    public void removeSupportedProtocol(final String protocol) {
        final String trimmed = StringSupport.trimOrNull(protocol);
        if (trimmed != null && supportedProtocols.contains(trimmed)) {
            releaseThisandParentDOM();
            supportedProtocols.remove(trimmed);
        }
    }

    /** {@inheritDoc} */
    public void removeSupportedProtocols(final Collection<String> protocols) {
        for (final String protocol : protocols) {
            removeSupportedProtocol(protocol);
        }
    }

    /** {@inheritDoc} */
    public void removeAllSupportedProtocols() {
        supportedProtocols.clear();
    }

    /** {@inheritDoc} */
    public String getErrorURL() {
        return errorURL;
    }

    /** {@inheritDoc} */
    public void setErrorURL(final String url) {
        errorURL = prepareForAssignment(errorURL, url);
    }

    /** {@inheritDoc} */
    public Extensions getExtensions() {
        return extensions;
    }

    /** {@inheritDoc} */
    public void setExtensions(final Extensions ext) {
        extensions = prepareForAssignment(extensions, ext);
    }

    /** {@inheritDoc} */
    public Organization getOrganization() {
        return organization;
    }

    /** {@inheritDoc} */
    public void setOrganization(final Organization org) {
        organization = prepareForAssignment(organization, org);
    }

    /** {@inheritDoc} */
    public List<ContactPerson> getContactPersons() {
        return contactPersons;
    }

    /** {@inheritDoc} */
    public List<KeyDescriptor> getKeyDescriptors() {
        return keyDescriptors;
    }

    /** {@inheritDoc} */
    public AttributeMap getUnknownAttributes() {
        return unknownAttributes;
    }

    /** {@inheritDoc} */
    @Override
    public String getSignatureReferenceID() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public List<XMLObject> getOrderedChildren() {
        final ArrayList<XMLObject> children = new ArrayList<>();

        if (getSignature() != null) {
            children.add(getSignature());
        }

        if (extensions != null) {
            children.add(getExtensions());
        }
        children.addAll(getKeyDescriptors());
        if (organization != null) {
            children.add(getOrganization());
        }
        children.addAll(getContactPersons());

        return Collections.unmodifiableList(children);
    }
}