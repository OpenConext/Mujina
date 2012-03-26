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

package nl.surfnet.mockoleth.saml.xml;

import org.opensaml.Configuration;

import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.StatusBuilder;
import org.opensaml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.saml2.core.impl.StatusMessageBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;

public class StatusGenerator {

	private final XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
	
	
	public Status generateStatus( String value ) {
		
		StatusBuilder builder = (StatusBuilder) builderFactory.getBuilder(Status.DEFAULT_ELEMENT_NAME);
		
		Status status =  (Status) builder.buildObject();
		
		StatusCodeBuilder codeBuilder = (StatusCodeBuilder) builderFactory.getBuilder(StatusCode.DEFAULT_ELEMENT_NAME);
		
		StatusCode statusCode = (StatusCode) codeBuilder.buildObject();
		statusCode.setValue(value);
		status.setStatusCode(statusCode);
		
		return status;
	}
	
	public Status generateStatus( String value, String subStatus, String message ) {
		
		StatusBuilder builder = (StatusBuilder) builderFactory.getBuilder(Status.DEFAULT_ELEMENT_NAME);
		
		Status status =  (Status) builder.buildObject();
	

		
		StatusCodeBuilder codeBuilder = (StatusCodeBuilder) builderFactory.getBuilder(StatusCode.DEFAULT_ELEMENT_NAME);
		StatusCode statusCode = (StatusCode) codeBuilder.buildObject();
		statusCode.setValue(value);
		
		StatusCode subStatusCode = (StatusCode) codeBuilder.buildObject();
		subStatusCode.setValue(subStatus);
		statusCode.setStatusCode(subStatusCode);
		
		
		status.setStatusCode(statusCode);
		
		StatusMessageBuilder statusMessageBuilder = (StatusMessageBuilder) builderFactory.getBuilder(StatusMessage.DEFAULT_ELEMENT_NAME);
		
		StatusMessage statusMessage = statusMessageBuilder.buildObject();
		
		statusMessage.setMessage(message);
		status.setStatusMessage(statusMessage);
		
		return status;
	}
	
}
