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

package org.opensaml.xmlsec.keyinfo.impl;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.keyinfo.KeyInfoSupport;
import org.opensaml.xmlsec.signature.KeyInfo;

import com.google.common.base.Strings;

import net.shibboleth.utilities.java.support.codec.EncodingException;


/**
 * A factory implementation which produces instances of {@link KeyInfoGenerator} capable of 
 * handling the information contained within a {@link Credential}.
 * 
 * All boolean options default to false.
 */
public class BasicKeyInfoGeneratorFactory implements KeyInfoGeneratorFactory {
    
    /** The set of options configured for the factory. */
    private final BasicOptions options;
    
    /**
     * Constructor.
     * 
     * All boolean options are initialzed as false;
     */
    public BasicKeyInfoGeneratorFactory() {
        options = newOptions();
    }
    
    /** {@inheritDoc} */
    @Nonnull public Class<? extends Credential> getCredentialType() {
        return Credential.class;
    }

    /** {@inheritDoc} */
    public boolean handles(@Nonnull final Credential credential) {
        // This top-level class can handle any Credential type, with output limited to basic Credential information
        return true;
    }

    /** {@inheritDoc} */
    @Nonnull public KeyInfoGenerator newInstance() {
        //TODO lock options during cloning ?
        final BasicOptions newOptions = options.clone();
        return new BasicKeyInfoGenerator(newOptions);
    }
    
    /**
     * Get the option to emit the entity ID value in a Credential as a KeyName element.
     * 
     * @return return the option value
     */
    public boolean emitEntityIDAsKeyName() {
        return options.emitEntityIDAsKeyName;
    }

    /**
     * Set the option to emit the entity ID value in a Credential as a KeyName element.
     * 
     * @param newValue the new option value to set
     */
    public void setEmitEntityIDAsKeyName(final boolean newValue) {
        options.emitEntityIDAsKeyName = newValue;
    }

    /**
     * Get the option to emit key names found in a Credential as KeyName elements.
     * 
     * @return the option value
     */
    public boolean emitKeyNames() {
        return options.emitKeyNames;
    }

    /**
     * Set the option to emit key names found in a Credential as KeyName elements.
     * 
     * @param newValue the new option value to set
     */
    public void setEmitKeyNames(final boolean newValue) {
        options.emitKeyNames = newValue;
    }

    /**
     * Get the option to emit the value of {@link Credential#getPublicKey()} as a KeyValue element.
     * 
     * @return the option value
     */
    public boolean emitPublicKeyValue() {
        return options.emitPublicKeyValue;
    }

    /**
     * Set the option to emit the value of {@link Credential#getPublicKey()} as a KeyValue element.
     * 
     * @param newValue the new option value to set
     */
    public void setEmitPublicKeyValue(final boolean newValue) {
        options.emitPublicKeyValue = newValue;
    }
    
    /**
     * Get the option to emit the value of {@link Credential#getPublicKey()} as a DEREncodedKeyValue element.
     *
     * @return the option value
     */
    public boolean emitPublicDEREncodedKeyValue() {
        return options.emitPublicDEREncodedKeyValue;
    }
          
    /**
     * Set the option to emit the value of {@link Credential#getPublicKey()} as a DEREncodedKeyValue element.
     *
     * @param newValue the new option value to set
     */
    public void setEmitPublicDEREncodedKeyValue(final boolean newValue) {
        options.emitPublicDEREncodedKeyValue = newValue;
    }
    
    /**
     * Get a new instance to hold options.  Used by the top-level superclass constructor.
     * Subclasses <strong>MUST</strong> override to produce an instance of the appropriate 
     * subclass of {@link BasicOptions}.
     * 
     * @return a new instance of factory/generator options
     */
    @Nonnull protected BasicOptions newOptions() {
        return new BasicOptions();
    }
    
    /**
     * Get the options of this instance. Used by subclass constructors to get the options built by 
     * the top-level class constructor with {@link #newOptions()}.
     * 
     * @return the options instance
     */
    @Nonnull protected BasicOptions getOptions() {
        return options;
    }
    
    /**
     * An implementation of {@link KeyInfoGenerator} capable of  handling the information 
     * contained within a {@link Credential}.
    */
    public class BasicKeyInfoGenerator implements KeyInfoGenerator {
        
        /** The set of options to be used by the generator.*/
        private final BasicOptions options;
       
        /** Builder for KeyInfo objects. */
        private final XMLObjectBuilder<KeyInfo> keyInfoBuilder;
       
        /**
         * Constructor.
         * 
         * @param newOptions the options to be used by the generator
         */
        protected BasicKeyInfoGenerator(@Nonnull final BasicOptions newOptions) {
            options = newOptions;
            keyInfoBuilder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilderOrThrow(
                    KeyInfo.DEFAULT_ELEMENT_NAME);
        }

        /** {@inheritDoc} */
        @Nullable public KeyInfo generate(@Nullable final Credential credential) throws SecurityException {
            if (credential == null) {
                return null;
            }
            
            final KeyInfo keyInfo = keyInfoBuilder.buildObject(KeyInfo.DEFAULT_ELEMENT_NAME);
            
            processKeyNames(keyInfo, credential);
            processEntityID(keyInfo, credential);
            processPublicKey(keyInfo, credential);
            
            final List<XMLObject> children = keyInfo.getOrderedChildren();
            if (children != null && children.size() > 0) {
                return keyInfo;
            }
            return null;
        }
        
        /** Process the values of {@link Credential#getKeyNames()}.
         * 
         * @param keyInfo the KeyInfo that is being built
         * @param credential the Credential that is geing processed
         */
        protected void processKeyNames(@Nonnull final KeyInfo keyInfo, @Nonnull final Credential credential) {
            if (options.emitKeyNames) {
                for (final String keyNameValue : credential.getKeyNames()) {
                    if (!Strings.isNullOrEmpty(keyNameValue)) {
                        KeyInfoSupport.addKeyName(keyInfo, keyNameValue);
                    }
                }
            }
        }
        
        /** Process the value of {@link Credential#getEntityId()}.
         * 
         * @param keyInfo the KeyInfo that is being built
         * @param credential the Credential that is geing processed
         */
        protected void processEntityID(@Nonnull final KeyInfo keyInfo, @Nonnull final Credential credential) {
            if (options.emitEntityIDAsKeyName) {
                final String keyNameValue = credential.getEntityId();
                if (!Strings.isNullOrEmpty(keyNameValue)) {
                    KeyInfoSupport.addKeyName(keyInfo, keyNameValue);
                }
            }
        }
        
        /** Process the value of {@link Credential#getPublicKey()}.
         * 
         * @param keyInfo the KeyInfo that is being built
         * @param credential the Credential that is being processed
         * @throws SecurityException if the public key can't be encoded properly
         */
        protected void processPublicKey(@Nonnull final KeyInfo keyInfo, @Nonnull final Credential credential)
            throws SecurityException {
            if (credential.getPublicKey() != null) {
                if (options.emitPublicKeyValue) {
                    try {
                        KeyInfoSupport.addPublicKey(keyInfo, credential.getPublicKey());
                    } catch (final EncodingException e) {
                        throw new SecurityException("Can't add public key to key info",e);
                    }
                }
                if (options.emitPublicDEREncodedKeyValue) {
                    try {
                        KeyInfoSupport.addDEREncodedPublicKey(keyInfo, credential.getPublicKey());
                    } catch (final NoSuchAlgorithmException e) {
                        throw new SecurityException("Can't DER-encode key, unsupported key algorithm", e);
                    } catch (final InvalidKeySpecException e) {
                        throw new SecurityException("Can't DER-encode key, invalid key specification", e);
                    }
                }
            }
        }
    }
    
    /**
     * Options to be used in the production of a {@link KeyInfo} from a {@link Credential}.
     */
    protected class BasicOptions implements Cloneable {
        
        /** Emit key names found in a Credential as KeyName elements. */
        private boolean emitKeyNames;
        
        /** Emit the entity ID value in a Credential as a KeyName element. */
        private boolean emitEntityIDAsKeyName;
        
        /** Emit the value of {@link Credential#getPublicKey()} as a KeyValue element. */
        private boolean emitPublicKeyValue;
        
        /** Emit the value of {@link Credential#getPublicKey()} as a DEREncodedKeyValue element. */
        private boolean emitPublicDEREncodedKeyValue;
        
        /** {@inheritDoc} */
        protected BasicOptions clone() {
            try {
                return (BasicOptions) super.clone();
            } catch (final CloneNotSupportedException e) {
                // we know we're cloneable, so this will never happen
                return null;
            }
        }
        
    }

}