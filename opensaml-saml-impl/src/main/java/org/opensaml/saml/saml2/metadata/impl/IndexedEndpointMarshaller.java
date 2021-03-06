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

package org.opensaml.saml.saml2.metadata.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.metadata.IndexedEndpoint;
import org.w3c.dom.Element;

/**
 * A thread safe Marshaller for {@link org.opensaml.saml.saml2.metadata.IndexedEndpoint} objects.
 */
public class IndexedEndpointMarshaller extends EndpointMarshaller {

    /** {@inheritDoc} */
    public void marshallAttributes(final XMLObject samlObject, final Element domElement) {
        final IndexedEndpoint iEndpoint = (IndexedEndpoint) samlObject;

        if (iEndpoint.getIndex() != null) {
            domElement.setAttributeNS(null, IndexedEndpoint.INDEX_ATTRIB_NAME, iEndpoint.getIndex().toString());
        }

        if (iEndpoint.isDefaultXSBoolean() != null) {
            domElement.setAttributeNS(null, IndexedEndpoint.IS_DEFAULT_ATTRIB_NAME, iEndpoint.isDefaultXSBoolean()
                    .toString());
        }

        super.marshallAttributes(samlObject, domElement);
    }
}