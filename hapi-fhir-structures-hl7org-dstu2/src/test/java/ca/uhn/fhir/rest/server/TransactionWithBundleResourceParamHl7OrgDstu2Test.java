package ca.uhn.fhir.rest.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.instance.model.Bundle.HTTPVerb;
import org.hl7.fhir.instance.model.Conformance;
import org.hl7.fhir.instance.model.Conformance.SystemInteractionComponent;
import org.hl7.fhir.instance.model.Conformance.SystemRestfulInteraction;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.InstantType;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.Patient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Transaction;
import ca.uhn.fhir.rest.annotation.TransactionParam;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.util.PortUtil;

/**
 * Created by dsotnikov on 2/25/2014.
 */
public class TransactionWithBundleResourceParamHl7OrgDstu2Test {

	@Test
	public void testIt() {
		
	}
	
	private static CloseableHttpClient ourClient;
	private static FhirContext ourCtx = FhirContext.forDstu2Hl7Org();
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(TransactionWithBundleResourceParamHl7OrgDstu2Test.class);
	private static int ourPort;
	private static boolean ourReturnOperationOutcome;

	private static Server ourServer;

	@Before
	public void before() {
		ourReturnOperationOutcome = false;
	}

	@Test
	public void testConformance() {
		ourCtx.getRestfulClientFactory().setSocketTimeout(500000);
		IGenericClient client = ourCtx.newRestfulGenericClient("http://localhost:" + ourPort + "/");
		client.registerInterceptor(new LoggingInterceptor(true));
		Conformance rest = client.fetchConformance().ofType(Conformance.class).prettyPrint().execute();
		boolean supportsTransaction = false;
		for (SystemInteractionComponent next : rest.getRest().get(0).getInteraction()) {
			ourLog.info("Supports interaction: {}");
			if (next.getCodeElement().getValue() == SystemRestfulInteraction.TRANSACTION) {
				supportsTransaction = true;
			}
		}
		
		assertTrue(supportsTransaction);
	}
	
	@Test
	public void testTransactionWithXmlRequest() throws Exception {
		Bundle b = new Bundle();
		InstantType nowInstant = InstantType.withCurrentTime();

		Patient p1 = new Patient();
		p1.addName().addFamily("Family1");
		BundleEntryComponent entry = b.addEntry();
		p1.getIdElement().setValue("1");
		entry.setResource(p1);

		Patient p2 = new Patient();
		p2.addName().addFamily("Family2");
		entry = b.addEntry();
		p2.getIdElement().setValue("2");
		entry.setResource(p2);

		BundleEntryComponent deletedEntry = b.addEntry();
		deletedEntry.getTransaction().setMethod(HTTPVerb.DELETE);
		deletedEntry.getTransaction().setUrl("http://base.com/Patient/123");

		String bundleString = ourCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(b);
		ourLog.info(bundleString);

		HttpPost httpPost = new HttpPost("http://localhost:" + ourPort + "/");
		httpPost.setEntity(new StringEntity(bundleString, ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));
		HttpResponse status = ourClient.execute(httpPost);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertEquals(200, status.getStatusLine().getStatusCode());

		ourLog.info(responseContent);

		Bundle bundle = ourCtx.newXmlParser().parseResource(Bundle.class, responseContent);
		assertEquals(3, bundle.getEntry().size());

		BundleEntryComponent entry0 = bundle.getEntry().get(0);
		assertEquals("Patient/81/_history/91", entry0.getTransactionResponse().getLocation());

		BundleEntryComponent entry1 = bundle.getEntry().get(1);
		assertEquals( "Patient/82/_history/92", entry1.getTransactionResponse().getLocation());

		BundleEntryComponent entry2 = bundle.getEntry().get(2);
		assertEquals("Patient/123/_history/93", entry2.getTransactionResponse().getLocation());
	}

	@Test
	public void testTransactionWithJsonRequest() throws Exception {
		Bundle b = new Bundle();
		InstantType nowInstant = InstantType.withCurrentTime();

		Patient p1 = new Patient();
		p1.addName().addFamily("Family1");
		BundleEntryComponent entry = b.addEntry();
		p1.getIdElement().setValue("1");
		entry.setResource(p1);

		Patient p2 = new Patient();
		p2.addName().addFamily("Family2");
		entry = b.addEntry();
		p2.getIdElement().setValue("2");
		entry.setResource(p2);

		BundleEntryComponent deletedEntry = b.addEntry();
		deletedEntry.getTransaction().setMethod(HTTPVerb.DELETE);
		deletedEntry.getTransaction().setUrl("http://base.com/Patient/123");

		String bundleString = ourCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(b);
		ourLog.info(bundleString);

		HttpPost httpPost = new HttpPost("http://localhost:" + ourPort + "/");
//		httpPost.addHeader("Accept", Constants.CT_ATOM_XML + "; pretty=true");
		httpPost.setEntity(new StringEntity(bundleString, ContentType.create(Constants.CT_FHIR_JSON, "UTF-8")));
		HttpResponse status = ourClient.execute(httpPost);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertEquals(200, status.getStatusLine().getStatusCode());

		ourLog.info(responseContent);

		Bundle bundle = ourCtx.newXmlParser().parseResource(Bundle.class, responseContent);
		assertEquals(3, bundle.getEntry().size());

		BundleEntryComponent entry0 = bundle.getEntry().get(0);
		assertEquals("Patient/81/_history/91", entry0.getTransactionResponse().getLocation());

		BundleEntryComponent entry1 = bundle.getEntry().get(1);
		assertEquals( "Patient/82/_history/92", entry1.getTransactionResponse().getLocation());

		BundleEntryComponent entry2 = bundle.getEntry().get(2);
		assertEquals("Patient/123/_history/93", entry2.getTransactionResponse().getLocation());
	}

	@Test
	public void testTransactionWithOperationOutcome() throws Exception {
		ourReturnOperationOutcome = true;

		Bundle b = new Bundle();
		InstantType nowInstant = InstantType.withCurrentTime();

		Patient p1 = new Patient();
		p1.addName().addFamily("Family1");
		BundleEntryComponent entry = b.addEntry();
		p1.getIdElement().setValue("1");
		entry.setResource(p1);

		Patient p2 = new Patient();
		p2.addName().addFamily("Family2");
		entry = b.addEntry();
		p2.getIdElement().setValue("2");
		entry.setResource(p2);

		BundleEntryComponent deletedEntry = b.addEntry();
		deletedEntry.getTransaction().setMethod(HTTPVerb.DELETE);
		deletedEntry.getTransaction().setUrl(("Patient/3"));

		String bundleString = ourCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(b);
		ourLog.info(bundleString);

		String base = "http://localhost:" + ourPort + "/";
		HttpPost httpPost = new HttpPost(base);
		httpPost.addHeader("Accept", Constants.CT_ATOM_XML + "; pretty=true");
		httpPost.setEntity(new StringEntity(bundleString, ContentType.create(Constants.CT_ATOM_XML, "UTF-8")));
		HttpResponse status = ourClient.execute(httpPost);
		String responseContent = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertEquals(200, status.getStatusLine().getStatusCode());

		ourLog.info(responseContent);

		Bundle bundle = ourCtx.newXmlParser().parseResource(Bundle.class, responseContent);
		assertEquals(4, bundle.getEntry().size());

		assertEquals(OperationOutcome.class, bundle.getEntry().get(0).getResource().getClass());

		BundleEntryComponent entry0 = bundle.getEntry().get(1);
		assertEquals("Patient/81/_history/91", entry0.getTransactionResponse().getLocation());

		BundleEntryComponent entry1 = bundle.getEntry().get(2);
		assertEquals("Patient/82/_history/92", entry1.getTransactionResponse().getLocation());

		BundleEntryComponent entry2 = bundle.getEntry().get(3);
		assertEquals( "Patient/3/_history/93", entry2.getTransactionResponse().getLocation());
	}

	@AfterClass
	public static void afterClass() throws Exception {
		ourServer.stop();
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		ourPort = PortUtil.findFreePort();
		ourServer = new Server(ourPort);

		DummyProvider patientProvider = new DummyProvider();
		RestfulServer server = new RestfulServer(ourCtx);
		server.setProviders(patientProvider);

		org.eclipse.jetty.servlet.ServletContextHandler proxyHandler = new org.eclipse.jetty.servlet.ServletContextHandler();
		proxyHandler.setContextPath("/");

		ServletHolder handler = new ServletHolder();
		handler.setServlet(server);
		proxyHandler.addServlet(handler, "/*");

		ourServer.setHandler(proxyHandler);
		ourServer.start();

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(500000, TimeUnit.MILLISECONDS);
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setConnectionManager(connectionManager);
		ourClient = builder.build();

	}

	/**
	 * Created by dsotnikov on 2/25/2014.
	 */
	public static class DummyProvider {

		@Transaction
		public Bundle transaction(@TransactionParam Bundle theResources) {
			Bundle retVal = new Bundle();

			if (ourReturnOperationOutcome) {
				OperationOutcome oo = new OperationOutcome();
				oo.addIssue().setDetails("AAAAA");
				retVal.addEntry().setResource(oo);
			}

			int index = 1;
			for (BundleEntryComponent nextEntry : theResources.getEntry()) {
				String newId = "8" + Integer.toString(index);
				if (nextEntry.getTransaction().getMethodElement().getValue() == HTTPVerb.DELETE) {
					newId = new IdType(nextEntry.getTransaction().getUrlElement()).getIdPart();
				}
				 String newIdDt = new IdType("Patient", newId, "9" + Integer.toString(index)).getValue();
				retVal.addEntry().getTransactionResponse().setLocation(newIdDt);
				index++;
			}

			return retVal;
		}

	}

}
