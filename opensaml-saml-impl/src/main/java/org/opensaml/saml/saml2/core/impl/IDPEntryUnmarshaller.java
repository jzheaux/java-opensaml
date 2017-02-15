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

/**
 * 
 */

package org.opensaml.saml.saml2.core.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;
import org.opensaml.saml.saml2.core.IDPEntry;
import org.w3c.dom.Attr;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml.saml2.core.IDPEntry} objects.
 */
public class IDPEntryUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        final IDPEntry entry = (IDPEntry) samlObject;

        if (attribute.getNamespaceURI() == null) {
            if (attribute.getLocalName().equals(IDPEntry.PROVIDER_ID_ATTRIB_NAME)) {
                entry.setProviderID(attribute.getValue());
            } else if (attribute.getLocalName().equals(IDPEntry.NAME_ATTRIB_NAME)) {
                entry.setName(attribute.getValue());
            } else if (attribute.getLocalName().equals(IDPEntry.LOC_ATTRIB_NAME)) {
                entry.setLoc(attribute.getValue());
            } else {
                super.processAttribute(samlObject, attribute);
            }
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }
    
}