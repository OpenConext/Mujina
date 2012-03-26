/*
*   Copyright 2010 James Cox <james.s.cox@gmail.com>
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package jcox.saml.xml;

import java.util.Collection;
import java.util.Map;

import org.opensaml.Configuration;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.springframework.security.core.GrantedAuthority;

public class AttributeStatementGenerator {

	private final XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

    /*
	public AttributeStatement generateAttributeStatement(Collection<GrantedAuthority> authorities) {
		
		AttributeStatementBuilder attributeStatementBuilder = (AttributeStatementBuilder) builderFactory.getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
		AttributeStatement attributeStatement = attributeStatementBuilder.buildObject();

		//Response/Assertion/AttributeStatement/Attribute
		AttributeBuilder attributeBuilder = (AttributeBuilder)  builderFactory.getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
		Attribute attribute = attributeBuilder.buildObject();
		attribute.setName(GrantedAuthority.class.getName());
		
		//urn:oasis:names:tc:SAML:2.0:attrname-format:basic
		attribute.setNameFormat(Attribute.BASIC);
		
		
		for (GrantedAuthority grantedAuthority : authorities) {
			//this was convoluted to figure out
			//Response/Assertion/AttributeStatement/Attribute/AttributeValue
			XSStringBuilder stringBuilder = (XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
			XSString stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
			stringValue.setValue(grantedAuthority.getAuthority());

	
			attribute.getAttributeValues().add(stringValue);
			
		}
		
		attributeStatement.getAttributes().add(attribute);
		
		return attributeStatement;
	} */

    public AttributeStatement generateAttributeStatement(final Map<String, String> attributes) {
        AttributeStatementBuilder attributeStatementBuilder = (AttributeStatementBuilder) builderFactory.getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
        AttributeStatement attributeStatement = attributeStatementBuilder.buildObject();

        AttributeBuilder attributeBuilder = (AttributeBuilder)  builderFactory.getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
        XSStringBuilder stringBuilder = (XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME);

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            Attribute attribute = attributeBuilder.buildObject();
            attribute.setName(entry.getKey());
            // attribute.setNameFormat(Attribute.BASIC);
            XSString stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
            stringValue.setValue(entry.getValue());
            attribute.getAttributeValues().add(stringValue);
            attributeStatement.getAttributes().add(attribute);
        }

        return attributeStatement;
    }
}
