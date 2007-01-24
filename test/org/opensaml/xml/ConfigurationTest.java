/*
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.xml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.validation.Validator;
import org.opensaml.xml.validation.ValidatorSuite;
import org.w3c.dom.Document;

/**
 * Test case for the library configuration mechanism.
 */
public class ConfigurationTest extends TestCase {
    
    /** System configuration utility */
    private XMLConfigurator configurator;

    /** Parser pool used to parse example config files */
    private ParserPool parserPool;
    
    /** SimpleElement QName */
    private QName simpleXMLObjectQName;
    
    /**
     * Constructor
     * @throws ConfigurationException 
     */
    public ConfigurationTest() throws ConfigurationException {
        configurator = new XMLConfigurator();
        
        HashMap<String, Boolean> features = new HashMap<String, Boolean>();
        features.put("http://apache.org/xml/features/validation/schema/normalized-value", Boolean.FALSE);
        features.put("http://apache.org/xml/features/dom/defer-node-expansion", Boolean.FALSE);

        parserPool = new ParserPool(true, null, features);
        simpleXMLObjectQName = new QName("http://www.example.org/testObjects", "SimpleElement");
    }

    /**
     * Tests loading of multiple configuration files.
     */
    public void testObjectProviderConfiguration() throws Exception {
        
        // Test loading the SimpleXMLObject configuration where builder contains additional children
        InputStream sxConfig = Configuration.class.getResourceAsStream("/data/org/opensaml/xml/SimpleXMLObjectConfiguration.xml");
        Document sxConfigDoc = parserPool.parse(sxConfig);
        configurator.load(sxConfigDoc);
        
        XMLObjectBuilder sxBuilder = Configuration.getBuilderFactory().getBuilder(simpleXMLObjectQName);
        assertNotNull("SimpleXMLObject did not have a registered builder", sxBuilder);
        
        Marshaller sxMarshaller = Configuration.getMarshallerFactory().getMarshaller(simpleXMLObjectQName);
        assertNotNull("SimpleXMLObject did not have a registered marshaller", sxMarshaller);
        
        Unmarshaller sxUnmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(simpleXMLObjectQName);
        assertNotNull("SimpleXMLObject did not have a registered unmarshaller", sxUnmarshaller);
        
        // Test loading a configuration with bogus classes
        InputStream nonConfig = Configuration.class.getResourceAsStream("/data/org/opensaml/xml/NonexistantClassConfiguration.xml");
        Document nonConfigDoc = parserPool.parse(nonConfig);
        try {
            configurator.load(nonConfigDoc);
            fail("Configuration loaded file that contained invalid classes");
        }catch(ConfigurationException e){
            // this is supposed to fail
        }
    }
    
    /**
     * Tests that ValidatorSuites are correctly configured.
     * 
     * @throws XMLParserException thrown if the configuration XML file can not be read
     * @throws ConfigurationException thrown if there the ValidatorSuites can not be configured
     */
    public void testValidatorSuiteConfiguration() throws XMLParserException, ConfigurationException {
        String suite1Id = "TestSuite1";
        String suite2Id = "TestSuite2";
        
        InputStream validatorConfig = Configuration.class
                .getResourceAsStream("/data/org/opensaml/xml/ValidatorSuiteConfiguration.xml");
        Document validatorConfigDoc = parserPool.parse(validatorConfig);
        configurator.load(validatorConfigDoc);

        ValidatorSuite suite1 = Configuration.getValidatorSuite(suite1Id);
        assertNotNull("ValidatorSuite TestSuite1 was not configured", suite1);
        assertNotNull("ValidatorSuite TestSuite1 configuration Element was not available", Configuration.getValidatorSuiteConfiguration(suite1Id));
        List<Validator> suite1Validators = suite1.getValidators(simpleXMLObjectQName);
        assertEquals("Suite1 did not have expected number of validators", 2, suite1Validators.size());

        ValidatorSuite suite2 = Configuration.getValidatorSuite(suite2Id);
        assertNotNull("ValidatorSuite TestSuite2 was not configured", suite2);
        assertNotNull("ValidatorSuite TestSuite2 configuration Element was not available", Configuration.getValidatorSuiteConfiguration(suite2Id));
        List<Validator> suite2Validators = suite2.getValidators(simpleXMLObjectQName);
        assertEquals("Suite2 did not have expected number of validators", 1, suite2Validators.size());
    }
    
    /**
     * Tests that global ID attribute registration/deregistration is functioning properly.
     */
    public void testIDAttributeRegistration() {
        QName attribQname = new QName("http://example.org", "someIDAttribName", "test");
        
        assertFalse("Non-registered ID attribute check returned true", Configuration.isIDAttribute(attribQname));
        
        Configuration.registerIDAttribute(attribQname);
        assertTrue("Registered ID attribute check returned false", Configuration.isIDAttribute(attribQname));
        
        Configuration.deregisterIDAttribute(attribQname);
        assertFalse("Non-registered ID attribute check returned true", Configuration.isIDAttribute(attribQname));
    }
}