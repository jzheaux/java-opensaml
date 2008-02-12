/*
 * Copyright 2008 Members of the EGEE Collaboration.
 * Copyright 2008 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opensaml.ws.wsaddressing;

import javax.xml.namespace.QName;

import org.opensaml.xml.AttributeExtensibleXMLObject;
import org.opensaml.xml.ElementExtensibleXMLObject;

/**
 * The &lt;wsa:EndpointReference&gt; element.
 * 
 * @see Address
 * @see ReferenceParameters
 * @see Metadata
 * @see "WS-Addressing 1.0 - Core"
 * 
 * @author Valery Tschopp &lt;tschopp@switch.ch&gt;
 * @version $Revision$
 */
public interface EndpointReference extends AttributeExtensibleXMLObject,
        ElementExtensibleXMLObject, WSAddressingObject {

    /** Element local name. */
    public static final String ELEMENT_LOCAL_NAME= "EndpointReference";

    /** Default element name */
    public final static QName ELEMENT_NAME= new QName(WSAddressingConstants.WSA_NS,
                                                      ELEMENT_LOCAL_NAME,
                                                      WSAddressingConstants.WSA_PREFIX);

    /**
     * Returns the &lt;wsa:Address&gt; child element.
     * 
     * @return the {@link Address} child element or <code>null</code>
     */
    public Address getAddress();

    /**
     * Sets the &lt;wsa:Address&gt; child element.
     * 
     * @param address
     *            the {@link Address} child element to set.
     */
    public void setAddress(Address address);

    /**
     * Returns the optional &lt;wsa:Metadata&gt; child element.
     * 
     * @return the {@link Metadata} child element or <code>null</code>.
     */
    public Metadata getMetadata();

    /**
     * Sets the &lt;wsa:Metadata&gt; child element.
     * 
     * @param metadata
     *            the {@link Metadata} child element to set.
     */
    public void setMetadata(Metadata metadata);

    /**
     * Returns the optional &lt;wsa:ReferenceParameters&gt; child element.
     * 
     * @return the {@link ReferenceParameters} child element or
     *         <code>null</code>.
     */
    public ReferenceParameters getReferenceParameters();

    /**
     * Sets the &lt;wsa:ReferenceParameters&gt; child element.
     * 
     * @param referenceParameters
     *            the {@link ReferenceParameters} child element to set.
     */
    public void setReferenceParameters(ReferenceParameters referenceParameters);

}
