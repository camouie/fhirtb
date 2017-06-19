package fhirtb;

import java.util.ArrayList;
import java.util.List;



import javax.annotation.PostConstruct;
import javax.naming.NamingException;



import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.base.composite.BaseHumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.ServerValidationModeEnum;

	public class patientbean {
		public String lastname;
		private List<Patient> patients;

		public void getPatientsByLastname(){
	    	
	    	FhirContext ctx = FhirContext.forDstu2();
			//String serverBaseUrl = "http://sqlonfhir-stu3.azurewebsites.net/fhir";
			String serverBaseUrl = "http://fhirtest.uhn.ca/baseDstu2";
			
			// Disable server validation (don't pull the server's metadata first)
			ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
			
			//increase the timeout
			ctx.getRestfulClientFactory().setConnectTimeout(60 * 1000);
	        ctx.getRestfulClientFactory().setSocketTimeout(60 * 1000);
	        
	        // create the RESTful client to work with our FHIR server 
	        IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
			

	        try {
	            // search for the resource created
	            Bundle response = client.search()
	                    .forResource(Patient.class)
	                    .where(Patient.FAMILY.matches().values(this.lastname))
	                    .returnBundle(Bundle.class)
	                    .execute();

	            System.out.println("Found " + response.getTotal()
	                    + " patients called " + "'" + this.lastname);
	            
	            //initialize the patients list
	            this.patients = new ArrayList<Patient>();
	            
	            response.getEntry().forEach((entry) -> {
	            	IParser jParser = ctx.newJsonParser().setPrettyPrint(true);
	            	String resourceJSON = jParser.encodeResourceToString(entry.getResource());
	            	System.out.println(resourceJSON);
	            	
	            	//populate the list with the retrieved bundle's resources
	            	Patient p = (Patient) entry.getResource();
	            	this.patients.add(p);
	            	System.out.println(p.getNameFirstRep().getFamilyAsSingleString());
	                System.out.println("test pour git");
	            });
	            
	        } catch (Exception e) {
	            System.out.println("An error occurred trying to search:");
	            e.printStackTrace();
	        } 
	    	
	    }
		
		public void setLastname(String lastname) {this.lastname = lastname;}
		public String getLastname() {return this.lastname;}
		public List<Patient> getPatients() {return patients;}
		public void setPatients(List<Patient> patients) {this.patients = patients;}

}
