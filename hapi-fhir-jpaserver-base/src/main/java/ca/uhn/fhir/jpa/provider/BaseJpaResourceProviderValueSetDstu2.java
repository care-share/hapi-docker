package ca.uhn.fhir.jpa.provider;

/*
 * #%L
 * HAPI FHIR JPA Server
 * %%
 * Copyright (C) 2014 - 2015 University Health Network
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.BooleanUtils;

import ca.uhn.fhir.jpa.dao.IFhirResourceDaoValueSet;
import ca.uhn.fhir.jpa.dao.IFhirResourceDaoValueSet.ValidateCodeResult;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.resource.Parameters;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.CodeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;

public class BaseJpaResourceProviderValueSetDstu2 extends JpaResourceProviderDstu2<ValueSet> {

	//@formatter:off
	@Operation(name = "$expand", idempotent = true)
	public ValueSet expand(
			HttpServletRequest theServletRequest,
			@IdParam(optional=true) IdDt theId,
			@OperationParam(name="valueSet", min=0, max=1) ValueSet theValueSet,
			@OperationParam(name="identifier", min=0, max=1) UriDt theIdentifier,
			@OperationParam(name = "filter", min=0, max=1) StringDt theFilter) {
		//@formatter:on
		
		boolean haveId = theId != null && theId.hasIdPart();
		boolean haveIdentifier = theIdentifier != null && isNotBlank(theIdentifier.getValue());
		boolean haveValueSet = theValueSet != null && theValueSet.isEmpty() == false;
		
		if (!haveId && !haveIdentifier && !haveValueSet) {
			throw new InvalidRequestException("$expand operation at the type level (no ID specified) requires an identifier or a valueSet as a part of the request");
		}

		if (moreThanOneTrue(haveId, haveIdentifier, haveValueSet)) {
			throw new InvalidRequestException("$expand must EITHER be invoked at the type level, or have an identifier specified, or have a ValueSet specified. Can not combine these options.");
		}
		
		startRequest(theServletRequest);
		try {
			IFhirResourceDaoValueSet<ValueSet> dao = (IFhirResourceDaoValueSet<ValueSet>) getDao();
			if (haveId) {
				return dao.expand(theId, toFilterString(theFilter));
			} else if (haveIdentifier) {
				return dao.expandByIdentifier(theIdentifier.getValue(), toFilterString(theFilter));
			} else {
				return dao.expand(theValueSet, toFilterString(theFilter));
			}
			
		} finally {
			endRequest(theServletRequest);
		}
	}


	private static boolean moreThanOneTrue(boolean... theBooleans) {
		boolean haveOne = false;
		for (boolean next : theBooleans) {
			if (next) {
				if (haveOne) {
					return true;
				} else {
					haveOne = true;
				}
			}
		}
		return false;
	}


	private String toFilterString(StringDt theFilter) {
		return theFilter != null ? theFilter.getValue() : null;
	}

	//@formatter:off
	@Operation(name = "$validate-code", idempotent = true, returnParameters= {
		@OperationParam(name="result", type=BooleanDt.class, min=1),
		@OperationParam(name="message", type=StringDt.class),
		@OperationParam(name="display", type=StringDt.class)
	})
	public Parameters validateCode(
			HttpServletRequest theServletRequest,
			@IdParam(optional=true) IdDt theId, 
			@OperationParam(name="identifier", min=0, max=1) UriDt theValueSetIdentifier, 
			@OperationParam(name="code", min=0, max=1) CodeDt theCode, 
			@OperationParam(name="system", min=0, max=1) UriDt theSystem,
			@OperationParam(name="display", min=0, max=1) StringDt theDisplay,
			@OperationParam(name="coding", min=0, max=1) CodingDt theCoding,
			@OperationParam(name="codeableConcept", min=0, max=1) CodeableConceptDt theCodeableConcept
			) {
		//@formatter:on
		
		startRequest(theServletRequest);
		try {
			IFhirResourceDaoValueSet<ValueSet> dao = (IFhirResourceDaoValueSet<ValueSet>) getDao();
			ValidateCodeResult result = dao.validateCode(theValueSetIdentifier, theId, theCode, theSystem, theDisplay, theCoding, theCodeableConcept);
			Parameters retVal = new Parameters();
			retVal.addParameter().setName("result").setValue(new BooleanDt(result.isResult()));
			if (isNotBlank(result.getMessage())) {
				retVal.addParameter().setName("message").setValue(new StringDt(result.getMessage()));
			}
			if (isNotBlank(result.getDisplay())) {
				retVal.addParameter().setName("display").setValue(new StringDt(result.getDisplay()));
			}
			return retVal;
		} finally {
			endRequest(theServletRequest);
		}
	}
}
