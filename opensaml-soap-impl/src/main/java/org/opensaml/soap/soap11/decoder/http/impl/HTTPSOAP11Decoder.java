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

package org.opensaml.soap.soap11.decoder.http.impl;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.messaging.handler.MessageHandler;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.opensaml.soap.soap11.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.net.MediaType;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.net.HttpServletSupport;

/**
 * Basic SOAP 1.1 decoder for HTTP transport.
 * 
 * <p>
 * This decoder takes a mandatory {@link MessageHandler} instance which is used to determine
 * populate the message that is returned as the {@link MessageContext#getMessage()}.
 * </p>
 * 
 *  <p>
 *  A SOAP message oriented message exchange style might just populate the Envelope as the message.
 *  An application-specific payload-oriented message exchange would handle a specific type
 * of payload structure.  
 * </p>
 */
public class HTTPSOAP11Decoder extends BaseHttpServletRequestXMLMessageDecoder {

    /** Valid Content-Type media types. */
    private static final Set<MediaType> SUPPORTED_MEDIA_TYPES = Sets.newHashSet(MediaType.create("text", "xml"));
    
    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(HTTPSOAP11Decoder.class);
    
    /** Message handler to use in processing the message body. */
    private MessageHandler bodyHandler;
    
    /**
     * Get the configured body handler MessageHandler.
     * 
     * @return Returns the bodyHandler.
     */
    public MessageHandler getBodyHandler() {
        return bodyHandler;
    }

    /**
     * Set the configured body handler MessageHandler.
     * 
     * @param newBodyHandler The bodyHandler to set.
     */
    public void setBodyHandler(final MessageHandler newBodyHandler) {
        bodyHandler = newBodyHandler;
    }

    /** {@inheritDoc} */
    @Override
    protected void doDecode() throws MessageDecodingException {
        final MessageContext messageContext = new MessageContext();
        final HttpServletRequest request = getHttpServletRequest();

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new MessageDecodingException("This message decoder only supports the HTTP POST method");
        }

        log.debug("Unmarshalling SOAP message");
        final Envelope soapMessage;
        try {
            soapMessage = (Envelope) unmarshallMessage(request.getInputStream());
            messageContext.getSubcontext(SOAP11Context.class, true).setEnvelope(soapMessage);
        } catch (final IOException e) {
            log.error("Unable to obtain input stream from HttpServletRequest: {}", e.getMessage());
            throw new MessageDecodingException("Unable to obtain input stream from HttpServletRequest", e);
        }
        
        try {
            getBodyHandler().invoke(messageContext);
        } catch (final MessageHandlerException e) {
            log.error("Error processing SOAP Envelope body: {}", e.getMessage());
            throw new MessageDecodingException("Error processing SOAP Envelope body", e);
        }
        
        if (messageContext.getMessage() == null) {
            log.warn("Body handler did not properly populate the message in message context");
            throw new MessageDecodingException("Body handler did not properly populate the message in message context");
        }
        
        setMessageContext(messageContext);
        
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (getBodyHandler() == null) {
            throw new ComponentInitializationException("Body handler MessageHandler cannot be null");
        }
    }    
    
    /** {@inheritDoc} */
    @Override
    protected XMLObject getMessageToLog() {
        return getMessageContext().getSubcontext(SOAP11Context.class, true).getEnvelope();
    }

    /** {@inheritDoc} */
    @Override
    protected void validateHttpRequest(final HttpServletRequest request) throws MessageDecodingException {
        super.validateHttpRequest(request);
        
        if (!HttpServletSupport.validateContentType(request, SUPPORTED_MEDIA_TYPES, false, false)) {
            log.warn("Saw unsupported request Content-Type: {}", request.getContentType());
            throw new MessageDecodingException(
                    String.format("Content-Type '%s' was not a supported media type", request.getContentType()));
        }
 
    }

}