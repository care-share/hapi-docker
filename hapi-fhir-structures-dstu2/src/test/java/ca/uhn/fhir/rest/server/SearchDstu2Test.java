package ca.uhn.fhir.rest.server;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateAndListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.util.PatternMatcher;
import ca.uhn.fhir.util.PortUtil;

/**
 * Created by dsotnikov on 2/25/2014.
 */
public class SearchDstu2Test {

	private static CloseableHttpClient ourClient;
	private static FhirContext ourCtx = FhirContext.forDstu2();
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SearchDstu2Test.class);
	private static int ourPort;
	private static InstantDt ourReturnPublished;
	private static Server ourServer;
	private static String ourLastMethod;
	private static DateAndListParam ourLastDateAndList;
	private static ReferenceParam ourLastRef;

	@Before
	public void before() {
		ourLastMethod = null;
		ourLastDateAndList = null;
		ourLastRef = null;
	}
	
	@Test
	public void testSearchWhitelist01Failing() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient?_query=searchWhitelist01&ref=value");
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		assertEquals(400, status.getStatusLine().getStatusCode());
	}
	

	@Test
	public void testSearchReferenceParams01() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient?_query=searchNoList&ref=123");
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		assertEquals(200, status.getStatusLine().getStatusCode());
		
		assertEquals("123", ourLastRef.getIdPart());
		assertEquals(null, ourLastRef.getResourceType());
	}
 
	@Test
	public void testSearchReferenceParams02() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient?_query=searchNoList&ref=Patient/123");
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		assertEquals(200, status.getStatusLine().getStatusCode());
		
		assertEquals("123", ourLastRef.getIdPart());
		assertEquals("Patient", ourLastRef.getResourceType());
	}

	@Test
	public void testSearchReferenceParams03() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient?_query=searchNoList&ref:Patient=Patient/123");
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		assertEquals(200, status.getStatusLine().getStatusCode());
		
		assertEquals("123", ourLastRef.getIdPart());
		assertEquals("Patient", ourLastRef.getResourceType());
	}

	@Test
	public void testSearchReferenceParams04() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient?_query=searchNoList&ref:Patient=123");
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		assertEquals(200, status.getStatusLine().getStatusCode());
		
		assertEquals("123", ourLastRef.getIdPart());
		assertEquals("Patient", ourLastRef.getResourceType());
	}

	@Test
	public void testSearchDateAndList() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient?searchDateAndList=2001,2002&searchDateAndList=2003,2004");
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		assertEquals("searchDateAndList", ourLastMethod);
		assertEquals(2, ourLastDateAndList.getValuesAsQueryTokens().size());
		assertEquals(2, ourLastDateAndList.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().size());
		assertEquals(2, ourLastDateAndList.getValuesAsQueryTokens().get(1).getValuesAsQueryTokens().size());
		assertEquals("2001", ourLastDateAndList.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValueAsString());
		assertEquals("2002", ourLastDateAndList.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(1).getValueAsString());		
	}

	
	@Test
	public void testSearchByPost() throws Exception {
		HttpPost httpGet = new HttpPost("http://localhost:" + ourPort + "/Patient/_search");
		StringEntity entity = new StringEntity("searchDateAndList=2001,2002&searchDateAndList=2003,2004", ContentType.APPLICATION_FORM_URLENCODED);
		httpGet.setEntity(entity);
		
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		assertEquals("searchDateAndList", ourLastMethod);
		assertEquals(2, ourLastDateAndList.getValuesAsQueryTokens().size());
		assertEquals(2, ourLastDateAndList.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().size());
		assertEquals(2, ourLastDateAndList.getValuesAsQueryTokens().get(1).getValuesAsQueryTokens().size());
		assertEquals("2001", ourLastDateAndList.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValueAsString());
		assertEquals("2002", ourLastDateAndList.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(1).getValueAsString());
		assertThat(responseContent, containsString("SYSTEM"));
	}

	@Test
	public void testSearchByPut() throws Exception {
		HttpPut httpGet = new HttpPut("http://localhost:" + ourPort + "/Patient/_search");
		StringEntity entity = new StringEntity("searchDateAndList=2001,2002&searchDateAndList=2003,2004", ContentType.APPLICATION_FORM_URLENCODED);
		httpGet.setEntity(entity);
		
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		assertEquals(400, status.getStatusLine().getStatusCode());
	}

		@Test
		public void testSearchByPostWithBodyAndUrlParams() throws Exception {
		
		HttpPost httpGet = new HttpPost("http://localhost:" + ourPort + "/Patient/_search?_format=json");
		StringEntity entity = new StringEntity("searchDateAndList=2001,2002&searchDateAndList=2003,2004", ContentType.APPLICATION_FORM_URLENCODED);
		httpGet.setEntity(entity);
		
		CloseableHttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		assertEquals("searchDateAndList", ourLastMethod);
		assertEquals(2, ourLastDateAndList.getValuesAsQueryTokens().size());
		assertEquals(2, ourLastDateAndList.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().size());
		assertEquals(2, ourLastDateAndList.getValuesAsQueryTokens().get(1).getValuesAsQueryTokens().size());
		assertEquals("2001", ourLastDateAndList.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValueAsString());
		assertEquals("2002", ourLastDateAndList.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(1).getValueAsString());
		assertThat(responseContent, containsString(":\"SYSTEM\""));

	}

	
	@Test
	public void testSearchBlacklist01Failing() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient?_query=searchBlacklist01&ref.black1=value");
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		assertEquals(400, status.getStatusLine().getStatusCode());
	}
	@Test
	public void testSearchBlacklist01Passing() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient?_query=searchBlacklist01&ref.white1=value");
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		assertEquals(200, status.getStatusLine().getStatusCode());
		assertEquals("searchBlacklist01", ourLastMethod);
	}

	@Test
	public void testSearchWhitelist01Passing() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient?_query=searchWhitelist01&ref.white1=value");
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		assertEquals(200, status.getStatusLine().getStatusCode());
		assertEquals("searchWhitelist01", ourLastMethod);
	}

	@Test
	public void testEncodeConvertsReferencesToRelative() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient?_query=searchWithRef");
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		
		assertThat(responseContent, not(containsString("text")));

		assertEquals(200, status.getStatusLine().getStatusCode());
		Patient patient = (Patient) ourCtx.newXmlParser().parseResource(Bundle.class, responseContent).getEntry().get(0).getResource();
		String ref = patient.getManagingOrganization().getReference().getValue();
		assertEquals("Organization/555", ref);
		assertNull(status.getFirstHeader(Constants.HEADER_CONTENT_LOCATION));
	}

	@Test
	public void testEncodeConvertsReferencesToRelativeJson() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient?_query=searchWithRef&_format=json");
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		
		assertThat(responseContent, not(containsString("text")));

		assertEquals(200, status.getStatusLine().getStatusCode());
		Patient patient = (Patient) ourCtx.newJsonParser().parseResource(Bundle.class, responseContent).getEntry().get(0).getResource();
		String ref = patient.getManagingOrganization().getReference().getValue();
		assertEquals("Organization/555", ref);
		assertNull(status.getFirstHeader(Constants.HEADER_CONTENT_LOCATION));
	}

	@Test
	public void testResultBundleHasUuid() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient?_query=searchWithRef");
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);
		
		assertEquals(200, status.getStatusLine().getStatusCode());
		assertThat(responseContent, PatternMatcher.pattern("id value..[0-9a-f-]+\\\""));
	}

	@Test
	public void testResultBundleHasUpdateTime() throws Exception {
		ourReturnPublished = new InstantDt("2011-02-03T11:22:33Z");
		
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient?_query=searchWithBundleProvider&_pretty=true");
		HttpResponse status = ourClient.execute(httpGet);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());
		ourLog.info(responseContent);

		assertThat(responseContent, stringContainsInOrder("<lastUpdated value=\"2011-02-03T11:22:33Z\"/>"));
	}

	@AfterClass
	public static void afterClass() throws Exception {
		ourServer.stop();
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		ourPort = PortUtil.findFreePort();
		ourServer = new Server(ourPort);

		DummyPatientResourceProvider patientProvider = new DummyPatientResourceProvider();

		ServletHandler proxyHandler = new ServletHandler();
		RestfulServer servlet = new RestfulServer(ourCtx);

		servlet.setResourceProviders(patientProvider);
		ServletHolder servletHolder = new ServletHolder(servlet);
		proxyHandler.addServletWithMapping(servletHolder, "/*");
		ourServer.setHandler(proxyHandler);
		ourServer.start();

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(5000, TimeUnit.MILLISECONDS);
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setConnectionManager(connectionManager);
		ourClient = builder.build();

	}

	/**
	 * Created by dsotnikov on 2/25/2014.
	 */
	public static class DummyPatientResourceProvider implements IResourceProvider {
		

		@Override
		public Class<? extends IResource> getResourceType() {
			return Patient.class;
		}

		//@formatter:off
		@Search(queryName="searchWhitelist01")
		public List<Patient> searchWhitelist01(
				@RequiredParam(chainWhitelist="white1", name = "ref") ReferenceParam theParam) {
			ourLastMethod = "searchWhitelist01";
			return Collections.emptyList();
		}
		//@formatter:on

		//@formatter:off
		@Search(queryName="searchNoList")
		public List<Patient> searchNoList(
				@RequiredParam(name = "ref") ReferenceParam theParam) {
			ourLastMethod = "searchNoList";
			ourLastRef = theParam;
			return Collections.emptyList();
		}
		//@formatter:on

		//@formatter:off
		@Search()
		public List<Patient> searchDateAndList(
				@RequiredParam(name = "searchDateAndList") DateAndListParam theParam) {
			ourLastMethod = "searchDateAndList";
			ourLastDateAndList = theParam;
			ArrayList<Patient> retVal = new ArrayList<Patient>();
			Patient patient = new Patient();
			patient.setId("1");
			retVal.add(patient.addIdentifier(new IdentifierDt("SYSTEM", "CODE")));
			return retVal;
		}
		//@formatter:on

		//@formatter:off
		@Search(queryName="searchBlacklist01")
		public List<Patient> searchBlacklist01(
				@RequiredParam(chainBlacklist="black1", name = "ref") ReferenceParam theParam) {
			ourLastMethod = "searchBlacklist01";
			return Collections.emptyList();
		}
		//@formatter:on

		@Search(queryName="searchWithBundleProvider")
		public IBundleProvider searchWithBundleProvider() {
			return new IBundleProvider() {
				
				@Override
				public InstantDt getPublished() {
					return ourReturnPublished;
				}
				
				@Override
				public List<IBaseResource> getResources(int theFromIndex, int theToIndex) {
					throw new IllegalStateException();
				}
				
				@Override
				public Integer preferredPageSize() {
					return null;
				}
				
				@Override
				public int size() {
					return 0;
				}
			};
		}
		
		@Search(queryName="searchWithRef")
		public Patient searchWithRef() {
			Patient patient = new Patient();
			patient.setId("Patient/1/_history/1");
			patient.getManagingOrganization().setReference("http://localhost:" + ourPort + "/Organization/555/_history/666");
			return patient;
		}

	}

}
