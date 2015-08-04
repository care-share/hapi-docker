package ca.uhn.fhir.rest.gclient;

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

import org.hl7.fhir.instance.model.api.IBaseBundle;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.method.SearchStyleEnum;
import ca.uhn.fhir.rest.param.DateRangeParam;

public interface IQuery<T> extends IClientExecutable<IQuery<T>, T>, IBaseQuery<IQuery<T>> {

	/**
	 * Add an "_include" specification
	 */
	IQuery<T> include(Include theInclude);

	ISort<T> sort();

	IQuery<T> limitTo(int theLimitTo);

	/**
	 * Match only resources where the resource has the given tag. This parameter corresponds to
	 * the <code>_tag</code> URL parameter.
	 * @param theSystem The tag code system, or <code>null</code> to match any code system (this may not be supported on all servers)
	 * @param theCode The tag code. Must not be <code>null</code> or empty.
	 */
	IQuery<T> withTag(String theSystem, String theCode);

	/**
	 * Forces the query to perform the search using the given method (allowable methods are described in the 
	 * <a href="http://www.hl7.org/implement/standards/fhir/http.html#search">FHIR Specification Section 2.1.11</a>)
	 * 
	 * @see SearchStyleEnum
	 * @since 0.6
	 */
	IQuery<T> usingStyle(SearchStyleEnum theStyle);

	IQuery<T> withIdAndCompartment(String theResourceId, String theCompartmentName);

	/**
	 * Add a "_revinclude" specification
	 * 
	 * @since HAPI FHIR 1.0 - Note that option was added to FHIR itself in DSTU2
	 */
	IQuery<T> revInclude(Include theIncludeTarget);

	/**
	 * Add a "_lastUpdated" specification
	 * 
	 * @since HAPI FHIR 1.1 - Note that option was added to FHIR itself in DSTU2
	 */
	IQuery<T> lastUpdated(DateRangeParam theLastUpdated);

	/**
	 * Request that the client return the specified bundle type, e.g. <code>org.hl7.fhir.instance.model.Bundle.class</code>
	 * or <code>ca.uhn.fhir.model.dstu2.resource.Bundle.class</code>
	 */
	<B extends IBaseBundle> IClientExecutable<IQuery<B>, B> returnBundle(Class<B> theClass);
	
}
