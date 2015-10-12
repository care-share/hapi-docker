package ca.uhn.fhir.osgi.impl;

/*
 * #%L
 * HAPI FHIR - OSGi Bundle
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


import java.util.Collection;

import ca.uhn.fhir.osgi.FhirProviderBundle;

/**
 *
 * @author Akana, Inc. Professional Services
 *
 */
public class SimpleFhirProviderBundle implements FhirProviderBundle {

	// /////////////////////////////////////
	// ////////    Spring Wiring    ////////
	// /////////////////////////////////////

	private Collection<Object> providers;

	public void setProviders (Collection<Object> providers) {
		this.providers = providers;
	}
	
	// /////////////////////////////////////
	// /////////////////////////////////////
	// /////////////////////////////////////
	
	public SimpleFhirProviderBundle () {
		super();
	}

	@Override
	public Collection<Object> getProviders () {
		return this.providers;
	}

}
