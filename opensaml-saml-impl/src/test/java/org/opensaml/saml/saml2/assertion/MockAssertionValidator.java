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

package org.opensaml.saml.saml2.assertion;

import java.util.Collections;
import java.util.Map;

import org.opensaml.saml.common.assertion.AssertionValidationException;
import org.opensaml.saml.common.assertion.ValidationContext;
import org.opensaml.saml.common.assertion.ValidationResult;
import org.opensaml.saml.saml2.core.Assertion;

import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.logic.Constraint;

public class MockAssertionValidator extends SAML20AssertionValidator {
    
    private Map<Assertion, Object> resultsMap;

    public MockAssertionValidator(Map<Assertion, Object> results) {
        super(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null);
        resultsMap = Constraint.isNotNull(results, "Results map was null");
    }

    /** {@inheritDoc} */
    public ValidationResult validate(Assertion assertion, ValidationContext context) throws AssertionValidationException {
        Object result = resultsMap.get(assertion);
        
        if (Throwable.class.isInstance(result)) {
            Throwable throwable = Throwable.class.cast(result);
            
            if (AssertionValidationException.class.isInstance(throwable)) {
                throw AssertionValidationException.class.cast(throwable);
            }
            if (RuntimeException.class.isInstance(throwable)) {
                throw RuntimeException.class.cast(throwable);
            }
            if (Error.class.isInstance(throwable)) {
                throw Error.class.cast(throwable);
            }
            if (Exception.class.isInstance(throwable)) {
                throw new AssertionValidationException(Exception.class.cast(throwable));
            }
            throw new RuntimeException(throwable);
        }
        
        if (ValidationResult.class.isInstance(result)) {
            ValidationResult vr = ValidationResult.class.cast(result);
            if (!ValidationResult.VALID.equals(vr)) {
                context.setValidationFailureMessage("Mock validation was not valid");
            }
            return vr;
        }
        
        if (Pair.class.isInstance(result)) {
            Pair<ValidationResult,String> pair = Pair.class.cast(result);
            if (!ValidationResult.VALID.equals(pair.getFirst())) {
                context.setValidationFailureMessage(pair.getSecond());
            }
            return pair.getFirst();
        }
        
        throw new IllegalArgumentException(String.format("Invalid result type supplied in mock results map for Assertion '%s': %s",
                assertion, result));
    }
    
}