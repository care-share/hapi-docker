package ca.uhn.fhir.validation;

import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;

public class FhirInstanceValidatorTest {

	private static FhirContext ourCtx = FhirContext.forDstu2Hl7Org();
	private FhirValidator val;
	
	@Before
	public void before() {
		val = ourCtx.newValidator();
		val.setValidateAgainstStandardSchema(false);
		val.setValidateAgainstStandardSchematron(false);
		val.registerValidatorModule(new FhirInstanceValidator());
	}
	
	@Test
	public void testValidateJsonResource() {
		String input = "{"
				+ "\"resourceType\":\"Patient\","
				+ "\"id\":\"123\""
				+ "}";
		
		ValidationResult output = val.validateWithResult(input);
		assertEquals(output.toString(), 0, output.getMessages().size());
	}

	@Test
	public void testValidateJsonResourceBadAttributes() {
		String input = "{"
				+ "\"resourceType\":\"Patient\","
				+ "\"id\":\"123\","
				+ "\"foo\":\"123\""
				+ "}";
		
		
		ValidationResult output = val.validateWithResult(input);
		assertEquals(output.toString(), 1, output.getMessages().size());
		assertEquals("Element is unknown or does not match any slice", output.getMessages().get(0).getMessage());
	}

	@Test
	public void testValidateXmlResource() {
		String input = "<Patient xmlns=\"http://hl7.org/fhir\">"
				+ "<id value=\"123\"/>"
				+ "</Patient>";
		
		ValidationResult output = val.validateWithResult(input);
		assertEquals(output.toString(), 0, output.getMessages().size());
	}


	@Test
	public void testValidateXmlResourceBadAttributes() {
		String input = "<Patient xmlns=\"http://hl7.org/fhir\">"
				+ "<id value=\"123\"/>"
				+ "<foo value=\"222\"/>"
				+ "</Patient>";
		
		ValidationResult output = val.validateWithResult(input);
		assertEquals(output.toString(), 1, output.getMessages().size());
		assertEquals("Element is unknown or does not match any slice", output.getMessages().get(0).getMessage());
	}
}
