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

package org.opensaml.saml.saml1.core.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml1.core.Action;
import org.opensaml.saml.saml1.core.AuthorizationDecisionStatement;
import org.opensaml.saml.saml1.core.DecisionTypeEnumeration;
import org.opensaml.saml.saml1.core.Evidence;
import org.w3c.dom.Attr;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml.saml1.core.impl.AuthorizationDecisionStatementImpl} objects.
 */
public class AuthorizationDecisionStatementUnmarshaller extends SubjectStatementUnmarshaller {

    /** {@inheritDoc} */
    protected void processChildElement(final XMLObject parentSAMLObject, final XMLObject childSAMLObject)
            throws UnmarshallingException {

        final AuthorizationDecisionStatement authorizationDecisionStatement =
                (AuthorizationDecisionStatement) parentSAMLObject;

        if (childSAMLObject instanceof Action) {
            authorizationDecisionStatement.getActions().add((Action) childSAMLObject);
        } else if (childSAMLObject instanceof Evidence) {
            authorizationDecisionStatement.setEvidence((Evidence) childSAMLObject);
        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }

    /** {@inheritDoc} */
    protected void processAttribute(final XMLObject samlObject, final Attr attribute) throws UnmarshallingException {

        final AuthorizationDecisionStatement authorizationDecisionStatement =
                (AuthorizationDecisionStatement) samlObject;

        if (attribute.getNamespaceURI() == null) {
            if (AuthorizationDecisionStatement.DECISION_ATTRIB_NAME.equals(attribute.getLocalName())) {
                try {
                    if (attribute.getValue() != null) {
                        authorizationDecisionStatement.setDecision(
                                DecisionTypeEnumeration.valueOf(attribute.getValue().toUpperCase()));
                    } else {
                        throw new UnmarshallingException("Saw an empty value for Decision attribute");
                    }
                } catch (final IllegalArgumentException e) {
                    throw new UnmarshallingException("Saw an invalid value for Decision attribute: "
                            + attribute.getValue());
                }
            } else if (AuthorizationDecisionStatement.RESOURCE_ATTRIB_NAME.equals(attribute.getLocalName())) {
                authorizationDecisionStatement.setResource(attribute.getValue());
            } else {
                super.processAttribute(samlObject, attribute);
            }
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }
}