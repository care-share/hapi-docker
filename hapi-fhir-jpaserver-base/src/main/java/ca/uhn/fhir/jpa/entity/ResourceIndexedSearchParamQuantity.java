package ca.uhn.fhir.jpa.entity;

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

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

//@formatter:off
@Entity
@Table(name = "HFJ_SPIDX_QUANTITY" /*, indexes= {@Index(name="IDX_SP_NUMBER", columnList="SP_VALUE")}*/ )
@org.hibernate.annotations.Table(appliesTo = "HFJ_SPIDX_QUANTITY", indexes= {
		@org.hibernate.annotations.Index(name="IDX_SP_QUANTITY", columnNames= {"RES_TYPE", "SP_NAME", "SP_SYSTEM", "SP_UNITS", "SP_VALUE"}
	)})
//@formatter:on
public class ResourceIndexedSearchParamQuantity extends BaseResourceIndexedSearchParam {

	private static final long serialVersionUID = 1L;

	@Column(name = "SP_SYSTEM", nullable = true, length = 100)
	public String mySystem;

	@Column(name = "SP_UNITS", nullable = true, length = 100)
	public String myUnits;

	@Column(name = "SP_VALUE", nullable = true)
	public BigDecimal myValue;

	public ResourceIndexedSearchParamQuantity() {
		// nothing
	}

	public ResourceIndexedSearchParamQuantity(String theParamName, BigDecimal theValue, String theSystem, String theUnits) {
		setParamName(theParamName);
		setSystem(theSystem);
		setValue(theValue);
		setUnits(theUnits);
	}

	@Override
	public boolean equals(Object theObj) {
		if (this == theObj) {
			return true;
		}
		if (theObj == null) {
			return false;
		}
		if (!(theObj instanceof ResourceIndexedSearchParamQuantity)) {
			return false;
		}
		ResourceIndexedSearchParamQuantity obj = (ResourceIndexedSearchParamQuantity) theObj;
		EqualsBuilder b = new EqualsBuilder();
		b.append(getParamName(), obj.getParamName());
		b.append(getResource(), obj.getResource());
		b.append(getSystem(), obj.getSystem());
		b.append(getUnits(), obj.getUnits());
		b.append(getValue(), obj.getValue());
		return b.isEquals();
	}

	public String getSystem() {
		return mySystem;
	}

	public String getUnits() {
		return myUnits;
	}

	public BigDecimal getValue() {
		return myValue;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder b = new HashCodeBuilder();
		b.append(getParamName());
		b.append(getResource());
		b.append(getSystem());
		b.append(getUnits());
		b.append(getValue());
		return b.toHashCode();
	}

	public void setSystem(String theSystem) {
		mySystem = theSystem;
	}

	public void setUnits(String theUnits) {
		myUnits = theUnits;
	}

	public void setValue(BigDecimal theValue) {
		myValue = theValue;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		b.append("paramName", getParamName());
		b.append("resourceId", getResource().getId()); // TODO: add a field so we don't need to resolve this
		b.append("system", getSystem());
		b.append("units", getUnits());
		b.append("value", getValue());
		return b.build();
	}

}
