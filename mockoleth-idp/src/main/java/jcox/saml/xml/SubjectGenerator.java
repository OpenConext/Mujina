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

import jcox.util.TimeService;

import org.opensaml.Configuration;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationDataBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;

public class SubjectGenerator {

	private final XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
	private final TimeService timeService;
		
	public SubjectGenerator(TimeService timeService) {
		super();
		this.timeService = timeService;
	}

	public Subject generateSubject( String recepientAssertionConsumerURL, int validForInSeconds, String subjectName,  String inResponseTo, String clientAddress) {
		
		//Response/Assertion/Subject/NameID
		NameIDBuilder nameIDBuilder = (NameIDBuilder) builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME);
		NameID nameID = nameIDBuilder.buildObject();
		nameID.setValue(subjectName);
		nameID.setFormat(NameIDType.UNSPECIFIED);

		//Response/Assertion/Subject
		SubjectBuilder subjectBuilder =  (SubjectBuilder)builderFactory.getBuilder(Subject.DEFAULT_ELEMENT_NAME);
		Subject subject = subjectBuilder.buildObject();
		
		subject.setNameID(nameID);
		
		SubjectConfirmationBuilder subjectConfirmationBuilder = (SubjectConfirmationBuilder)builderFactory.getBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
		SubjectConfirmation subjectConfirmation = subjectConfirmationBuilder.buildObject();
		subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
		
		SubjectConfirmationDataBuilder subjectConfirmationDataBuilder = (SubjectConfirmationDataBuilder)builderFactory.getBuilder(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
		SubjectConfirmationData subjectConfirmationData = subjectConfirmationDataBuilder.buildObject();
		
		subjectConfirmationData.setRecipient(recepientAssertionConsumerURL);
		subjectConfirmationData.setInResponseTo(inResponseTo);
		subjectConfirmationData.setNotOnOrAfter(timeService.getCurrentDateTime().plusSeconds(validForInSeconds));
		subjectConfirmationData.setAddress(clientAddress);
		
		subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
		
		subject.getSubjectConfirmations().add(subjectConfirmation);
		
		return subject;
	}

	
}
