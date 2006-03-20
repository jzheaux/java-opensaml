/*
 * Copyright [2005] [University Corporation for Advanced Internet Development, Inc.]
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

/**
 * 
 */
package org.opensaml.saml1.core.validator;

import org.opensaml.common.SAMLVersion;
import org.opensaml.saml1.core.Condition;
import org.opensaml.saml1.core.Conditions;
import org.opensaml.saml1.core.DoNotCacheCondition;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.validation.ValidationException;

/**
 * Spec validator for {@link org.opensaml.saml1.AttributeQuery}
 */
public class ConditionsSpecValidator extends SAML1ObjectSpecValidator {

    /*
     * @see org.opensaml.xml.validation.Validator#validate(org.opensaml.xml.XMLObject)
     */
    public void validate(XMLObject xmlObject) throws ValidationException {
        
        super.validate(xmlObject);
        validateDoNotCacheConiditons((Conditions)xmlObject);
    }
    
    protected void validateDoNotCacheConiditons(Conditions conditions) throws ValidationException {
        
        if (conditions.getVersion() == SAMLVersion.VERSION_11) {
            //
            // DoNotCache is OK for V1.1
            //
            return;
        }
        for (Condition condition : conditions.getConditions()) {
            if (condition instanceof DoNotCacheCondition) {
                throw new ValidationException("SAML1.0 does not support DoNotCacheCondition");
            }
        }
    }
}
