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


package org.opensaml.xmlsec.impl;

import java.util.Arrays;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.opensaml.xmlsec.WhitelistBlacklistConfiguration.Precedence;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BasicWhitelistBlacklistConfigurationTest {
    
    private BasicWhitelistBlacklistConfiguration config;
    
    @BeforeMethod
    public void setUp() {
        config = new BasicWhitelistBlacklistConfiguration();
    }
    
    @Test
    public void testDefaults() {
        Assert.assertEquals(config.isWhitelistMerge(), false);
        Assert.assertNotNull(config.getWhitelistedAlgorithms());
        Assert.assertTrue(config.getWhitelistedAlgorithms().isEmpty());
        
        Assert.assertEquals(config.isBlacklistMerge(), true);
        Assert.assertNotNull(config.getBlacklistedAlgorithms());
        Assert.assertTrue(config.getBlacklistedAlgorithms().isEmpty());
        
        Assert.assertEquals(config.getWhitelistBlacklistPrecedence(), Precedence.WHITELIST);
    }
    
    @Test
    public void testValidWhitelist() {
        config.setWhitelistedAlgorithms(Arrays.asList("  A   ", null, "   B   ", null, "   C   "));
        
        Assert.assertEquals(config.getWhitelistedAlgorithms().size(), 3);
        Assert.assertTrue(config.getWhitelistedAlgorithms().contains("A"));
        Assert.assertTrue(config.getWhitelistedAlgorithms().contains("B"));
        Assert.assertTrue(config.getWhitelistedAlgorithms().contains("C"));
    }

    @Test
    public void testNullWhitelist() {
        config.setWhitelistedAlgorithms(null);
        Assert.assertNotNull(config.getWhitelistedAlgorithms());
        Assert.assertTrue(config.getWhitelistedAlgorithms().isEmpty());
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void testWhitelistImmutable() {
        config.setWhitelistedAlgorithms(Arrays.asList("A", "B", "C"));
        config.getWhitelistedAlgorithms().add("D");
    }

    @Test
    public void testWhitelistMerge() {
        // Test default
        Assert.assertFalse(config.isWhitelistMerge());
        
        config.setWhitelistMerge(true);
        Assert.assertTrue(config.isWhitelistMerge());
        
        config.setWhitelistMerge(false);
        Assert.assertFalse(config.isWhitelistMerge());
    }

    @Test
    public void testValidBlacklist() {
        config.setBlacklistedAlgorithms(Arrays.asList("   A   ", null, "   B   ", null, "   C   "));
        
        Assert.assertEquals(config.getBlacklistedAlgorithms().size(), 3);
        Assert.assertTrue(config.getBlacklistedAlgorithms().contains("A"));
        Assert.assertTrue(config.getBlacklistedAlgorithms().contains("B"));
        Assert.assertTrue(config.getBlacklistedAlgorithms().contains("C"));
    }
    
    @Test
    public void testNullBlacklist() {
        config.setBlacklistedAlgorithms(null);
        Assert.assertNotNull(config.getBlacklistedAlgorithms());
        Assert.assertTrue(config.getBlacklistedAlgorithms().isEmpty());
    }
    
    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void testBlacklistImmutable() {
        config.setBlacklistedAlgorithms(Arrays.asList("A", "B", "C"));
        config.getBlacklistedAlgorithms().add("D");
    }
    
    @Test
    public void testBlacklistMerge() {
        // Test default
        Assert.assertTrue(config.isBlacklistMerge());
        
        config.setBlacklistMerge(false);
        Assert.assertFalse(config.isBlacklistMerge());
        
        config.setBlacklistMerge(true);
        Assert.assertTrue(config.isBlacklistMerge());
    }

    @Test
    public void testValidPrecedence() {
        // Test default
        Assert.assertEquals(config.getWhitelistBlacklistPrecedence(), Precedence.WHITELIST);
        
        config.setWhitelistBlacklistPrecedence(Precedence.WHITELIST);
        Assert.assertEquals(config.getWhitelistBlacklistPrecedence(), Precedence.WHITELIST);
        
        config.setWhitelistBlacklistPrecedence(Precedence.BLACKLIST);
        Assert.assertEquals(config.getWhitelistBlacklistPrecedence(), Precedence.BLACKLIST);
    }
    
    @Test(expectedExceptions=ConstraintViolationException.class)
    public void testNullPrecedence() {
        config.setWhitelistBlacklistPrecedence(null);
    }
    
}
