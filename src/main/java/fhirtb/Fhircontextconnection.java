package fhirtb;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.ServerValidationModeEnum;

public class Fhircontextconnection {
	// private String serverBaseUrl = "https://fhirtest.uhn.ca/baseDstu2";
	// private String serverBaseUrl = "http://spark.furore.com/fhir";
	// private String serverBaseUrl =
	// "http://localhost:8080/hapi-fhir-jpaserver-example/baseDstu3";
	// private String serverBaseUrl = "http://vonk.furore.com/";
	// private String serverBaseUrl =
	// "http://52.90.126.238:8080/fhir/baseDstu3/";
	//FHIR TEST SERVER HAPI
	//private String serverBaseUrl = "http://fhirtest.uhn.ca/baseDstu3";
	
	//PRIVATE SERVER TEST
 	private String serverBaseUrl = "http://213.136.91.24:8080/hapi-fhir-jpaserver-example/baseDstu3";
	private FhirContext ctx;

	public Fhircontextconnection() {
		this.ctx = FhirContext.forDstu3();

		// Disable server validation
		ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);

		// increase the timeout
		ctx.getRestfulClientFactory().setConnectTimeout(60 * 1000);
		ctx.getRestfulClientFactory().setSocketTimeout(60 * 1000);
	}

	public String getServerBaseUrl() {
		return serverBaseUrl;
	}

	public void setServerBaseUrl(String serverBaseUrl) {
		this.serverBaseUrl = serverBaseUrl;
	}

	public FhirContext getCtx() {
		return ctx;
	}

	public void setCtx(FhirContext ctx) {
		this.ctx = ctx;
	}

}
