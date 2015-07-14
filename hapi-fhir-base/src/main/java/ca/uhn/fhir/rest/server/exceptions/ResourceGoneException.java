package ca.uhn.fhir.rest.server.exceptions;

/*
 * #%L
 * HAPI FHIR - Core Library
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

import net.sourceforge.cobertura.CoverageIgnore;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.base.composite.BaseIdentifierDt;
import ca.uhn.fhir.model.base.resource.BaseOperationOutcome;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.server.Constants;

/**
 * Represents an <b>HTTP 410 Resource Gone</b> response, which geenerally
 * indicates that the resource has been deleted
 */
@CoverageIgnore
public class ResourceGoneException extends BaseServerResponseException {

	public static final int STATUS_CODE = Constants.STATUS_HTTP_410_GONE;

	public ResourceGoneException(IdDt theId) {
		super(STATUS_CODE, "Resource " + (theId != null ? theId.getValue() : "") + " is gone/deleted");
	}

	public ResourceGoneException(Class<? extends IResource> theClass, BaseIdentifierDt thePatientId) {
		super(STATUS_CODE, "Resource of type " + theClass.getSimpleName() + " with ID " + thePatientId + " is gone/deleted");
	}

	public ResourceGoneException(Class<? extends IResource> theClass, IdDt thePatientId) {
		super(STATUS_CODE, "Resource of type " + theClass.getSimpleName() + " with ID " + thePatientId + " is gone/deleted");
	}

	/**
	 * Constructor
	 * 
	 * @param theMessage
	 *            The message
	 *  @param theOperationOutcome The OperationOutcome resource to return to the client
	 */
	public ResourceGoneException(String theMessage, BaseOperationOutcome theOperationOutcome) {
		super(STATUS_CODE, theMessage, theOperationOutcome);
	}

	public ResourceGoneException(String theMessage) {
		super(STATUS_CODE, theMessage);
	}

	private static final long serialVersionUID = 1L;

}
