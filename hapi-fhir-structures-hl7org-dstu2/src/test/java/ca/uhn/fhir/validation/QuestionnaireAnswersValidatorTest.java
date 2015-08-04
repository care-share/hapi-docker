package ca.uhn.fhir.validation;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Questionnaire;
import org.hl7.fhir.instance.model.Questionnaire.AnswerFormat;
import org.hl7.fhir.instance.model.Questionnaire.GroupComponent;
import org.hl7.fhir.instance.model.QuestionnaireAnswers;
import org.hl7.fhir.instance.model.QuestionnaireAnswers.QuestionnaireAnswersStatus;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.StringType;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.utils.WorkerContext;
import org.hl7.fhir.instance.validation.QuestionnaireAnswersValidator;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.junit.Before;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;

public class QuestionnaireAnswersValidatorTest {
	private static final FhirContext ourCtx = FhirContext.forDstu2Hl7Org();
	
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(QuestionnaireAnswersValidatorTest.class);
	private QuestionnaireAnswersValidator myVal;

	private WorkerContext myWorkerCtx;
	
	@Before
	public void before() {
		myWorkerCtx = new WorkerContext();
		myVal = new QuestionnaireAnswersValidator(myWorkerCtx);
	}
	
	@Test
	public void testAnswerWithWrongType() {
		Questionnaire q = new Questionnaire();
		q.getGroup().addQuestion().setLinkId("link0").setRequired(true).setType(AnswerFormat.BOOLEAN);
		
		QuestionnaireAnswers qa = new QuestionnaireAnswers();
		qa.getQuestionnaire().setReference("http://example.com/Questionnaire/q1");
		qa.getGroup().addQuestion().setLinkId("link0").addAnswer().setValue(new StringType("FOO"));
		
		myWorkerCtx.getQuestionnaires().put(qa.getQuestionnaire().getReference(), q);
		List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
		myVal.validate(errors, qa);
		
		ourLog.info(errors.toString());
		assertThat(errors.toString(), containsString("Answer to question with linkId[link0] found of type [StringType] but this is invalid for question of type [boolean]"));
	}

	
	@Test
	public void testGroupWithNoLinkIdInQuestionnaireAnswers() {
		Questionnaire q = new Questionnaire();
		GroupComponent qGroup = q.getGroup().addGroup();
		qGroup.addQuestion().setLinkId("link0").setRequired(true).setType(AnswerFormat.BOOLEAN);
		
		QuestionnaireAnswers qa = new QuestionnaireAnswers();
		qa.getQuestionnaire().setReference("http://example.com/Questionnaire/q1");
		org.hl7.fhir.instance.model.QuestionnaireAnswers.GroupComponent qaGroup = qa.getGroup().addGroup();
		qaGroup.addQuestion().setLinkId("link0").addAnswer().setValue(new StringType("FOO"));
		
		myWorkerCtx.getQuestionnaires().put(qa.getQuestionnaire().getReference(), q);
		List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
		myVal.validate(errors, qa);
		
		ourLog.info(errors.toString());
		assertThat(errors.toString(), containsString("Answer to question with linkId[link0] found of type [StringType] but this is invalid for question of type [boolean]"));
	}

	@Test
	public void testMultipleGroupsWithNoLinkIdInQuestionnaireAnswers() {
		Questionnaire q = new Questionnaire();
		GroupComponent qGroup = q.getGroup().addGroup();
		qGroup.addQuestion().setLinkId("link0").setRequired(true).setType(AnswerFormat.BOOLEAN);
		GroupComponent qGroup2 = q.getGroup().addGroup();
		qGroup2.addQuestion().setLinkId("link1").setRequired(true).setType(AnswerFormat.BOOLEAN);
		
		QuestionnaireAnswers qa = new QuestionnaireAnswers();
		qa.getQuestionnaire().setReference("http://example.com/Questionnaire/q1");
		org.hl7.fhir.instance.model.QuestionnaireAnswers.GroupComponent qaGroup = qa.getGroup().addGroup();
		qaGroup.addQuestion().setLinkId("link0").addAnswer().setValue(new StringType("FOO"));
		
		myWorkerCtx.getQuestionnaires().put(qa.getQuestionnaire().getReference(), q);
		List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
		myVal.validate(errors, qa);
		
		ourLog.info(errors.toString());
		assertThat(errors.toString(), containsString("Questionnaire in invalid, unable to validate QuestionnaireAnswers: Multiple groups found at this position with linkId[]"));
	}
	
	
	@Test
	public void testCodedAnswer() {
		String questionnaireRef = "http://example.com/Questionnaire/q1";
		
		Questionnaire q = new Questionnaire();
		q.getGroup().addQuestion().setLinkId("link0").setRequired(false).setType(AnswerFormat.CHOICE).setOptions(new Reference("http://somevalueset"));
		myWorkerCtx.getQuestionnaires().put(questionnaireRef, q);
				
		ValueSet options = new ValueSet();
		options.getCodeSystem().setSystem("urn:system").addConcept().setCode("code0");
		options.getCompose().addInclude().setSystem("urn:system2").addConcept().setCode("code2");
		myWorkerCtx.getValueSets().put("http://somevalueset", options);
		
		QuestionnaireAnswers qa;
		List<ValidationMessage> errors;
		
		// Good code
		
		qa = new QuestionnaireAnswers();
		qa.getQuestionnaire().setReference(questionnaireRef);
		qa.getGroup().addQuestion().setLinkId("link0").addAnswer().setValue(new Coding().setSystem("urn:system").setCode("code0"));
		errors = new ArrayList<ValidationMessage>();
		myVal.validate(errors, qa);
		assertEquals(errors.toString(), 0, errors.size());

		qa = new QuestionnaireAnswers();
		qa.getQuestionnaire().setReference(questionnaireRef);
		qa.getGroup().addQuestion().setLinkId("link0").addAnswer().setValue(new Coding().setSystem("urn:system2").setCode("code2"));
		errors = new ArrayList<ValidationMessage>();
		myVal.validate(errors, qa);
		assertEquals(errors.toString(), 0, errors.size());

		// Bad code
		
		qa = new QuestionnaireAnswers();
		qa.getQuestionnaire().setReference(questionnaireRef);
		qa.getGroup().addQuestion().setLinkId("link0").addAnswer().setValue(new Coding().setSystem("urn:system").setCode("code1"));
		errors = new ArrayList<ValidationMessage>();
		myVal.validate(errors, qa);
		ourLog.info(errors.toString());
		assertThat(errors.toString(), containsString("location=//QuestionnaireAnswers/group[0]/question[0]/answer[0]"));
		assertThat(errors.toString(), containsString("message=Question with linkId[link0] has answer with system[urn:system] and code[code1] but this is not a valid answer for ValueSet[http://somevalueset]"));
		
		qa = new QuestionnaireAnswers();
		
		qa.getQuestionnaire().setReference(questionnaireRef);
		qa.getGroup().addQuestion().setLinkId("link0").addAnswer().setValue(new Coding().setSystem("urn:system2").setCode("code3"));
		errors = new ArrayList<ValidationMessage>();
		myVal.validate(errors, qa);
		ourLog.info(errors.toString());
		assertThat(errors.toString(), containsString("location=//QuestionnaireAnswers/group[0]/question[0]/answer[0]"));
		assertThat(errors.toString(), containsString("message=Question with linkId[link0] has answer with system[urn:system2] and code[code3] but this is not a valid answer for ValueSet[http://somevalueset]"));
		
	}


	@Test
	public void testMissingRequiredQuestion() {
		
		Questionnaire q = new Questionnaire();
		q.getGroup().addQuestion().setLinkId("link0").setRequired(true).setType(AnswerFormat.STRING);
		q.getGroup().addQuestion().setLinkId("link1").setRequired(true).setType(AnswerFormat.STRING);
		
		QuestionnaireAnswers qa = new QuestionnaireAnswers();
		qa.setStatus(QuestionnaireAnswersStatus.COMPLETED);
		qa.getQuestionnaire().setReference("http://example.com/Questionnaire/q1");
		qa.getGroup().addQuestion().setLinkId("link1").addAnswer().setValue(new StringType("FOO"));
		
		myWorkerCtx.getQuestionnaires().put(qa.getQuestionnaire().getReference(), q);
		List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
		myVal.validate(errors, qa);
		
		ourLog.info(errors.toString());
		assertThat(errors.toString(), containsString("Missing answer to required question with linkId[link0]"));
	}

	@Test
	public void testUnexpectedAnswer() {
		Questionnaire q = new Questionnaire();
		q.getGroup().addQuestion().setLinkId("link0").setRequired(false).setType(AnswerFormat.BOOLEAN);
		
		QuestionnaireAnswers qa = new QuestionnaireAnswers();
		qa.getQuestionnaire().setReference("http://example.com/Questionnaire/q1");
		qa.getGroup().addQuestion().setLinkId("link1").addAnswer().setValue(new StringType("FOO"));
		
		myWorkerCtx.getQuestionnaires().put(qa.getQuestionnaire().getReference(), q);
		List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
		myVal.validate(errors, qa);
		
		ourLog.info(errors.toString());
		assertThat(errors.toString(), containsString("location=//QuestionnaireAnswers/group[0]/question[0]"));
		assertThat(errors.toString(), containsString("message=Found answer with linkId[link1] but this ID is not allowed at this position"));
	}
	
	@Test
	public void testUnexpectedGroup() {
		Questionnaire q = new Questionnaire();
		q.getGroup().addQuestion().setLinkId("link0").setRequired(false).setType(AnswerFormat.BOOLEAN);
		
		QuestionnaireAnswers qa = new QuestionnaireAnswers();
		qa.getQuestionnaire().setReference("http://example.com/Questionnaire/q1");
		qa.getGroup().addGroup().setLinkId("link1");
		
		myWorkerCtx.getQuestionnaires().put(qa.getQuestionnaire().getReference(), q);
		List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
		myVal.validate(errors, qa);
		
		ourLog.info(errors.toString());
		assertThat(errors.toString(), containsString("location=//QuestionnaireAnswers/group[0]/group[0]"));
		assertThat(errors.toString(), containsString("Group with linkId[link1] found at this position, but this group does not exist at this position in Questionnaire"));
	}

//	@Test
	public void validateHealthConnexExample() throws Exception {
		String input = IOUtils.toString(QuestionnaireAnswersValidatorTest.class.getResourceAsStream("/questionnaireanswers-0f431c50ddbe4fff8e0dd6b7323625fc.xml"));

		QuestionnaireAnswers qa = ourCtx.newXmlParser().parseResource(QuestionnaireAnswers.class, input);
		ArrayList<ValidationMessage> errors = new ArrayList<ValidationMessage>();
		myVal.validate(errors, qa);
		assertEquals(errors.toString(), 0, errors.size());
		
		/*
		 * Now change a coded value
		 */
		//@formatter:off
		input = input.replaceAll("<answer>\n" + 
				"					<valueCoding>\n" + 
				"						<system value=\"f69573b8-cb63-4d31-85a4-23ac784735ab\"/>\n" + 
				"						<code value=\"2\"/>\n" + 
				"						<display value=\"Once/twice\"/>\n" + 
				"					</valueCoding>\n" + 
				"				</answer>", "<answer>\n" + 
				"					<valueCoding>\n" + 
				"						<system value=\"f69573b8-cb63-4d31-85a4-23ac784735ab\"/>\n" + 
				"						<code value=\"GGG\"/>\n" + 
				"						<display value=\"Once/twice\"/>\n" + 
				"					</valueCoding>\n" + 
				"				</answer>");
		assertThat(input, containsString("GGG"));
		//@formatter:on
		
		qa = ourCtx.newXmlParser().parseResource(QuestionnaireAnswers.class, input);
		errors = new ArrayList<ValidationMessage>();
		myVal.validate(errors, qa);
		assertEquals(errors.toString(), 10, errors.size());
	}
	

}
