package fhirtb;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.ServerValidationModeEnum;

	public class patientbean {
		
		public String lastname;
		private List<Patient> patients;
		IGenericClient client;
		FhirContext ctx;
		public Patient patient;
		
		@PostConstruct
		public void fhircontext () {
			this.ctx = FhirContext.forDstu3();
			//String serverBaseUrl = "http://sqlonfhir-stu3.azurewebsites.net/fhir";
			String serverBaseUrl = "http://fhirtest.uhn.ca/baseDstu3";
			
			// Disable server validation (don't pull the server's metadata first)
			ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
			
			//increase the timeout
			ctx.getRestfulClientFactory().setConnectTimeout(60 * 1000);
	        ctx.getRestfulClientFactory().setSocketTimeout(60 * 1000);
	        
	        // create the RESTful client to work with our FHIR server 
	        this.client = ctx.newRestfulGenericClient(serverBaseUrl);

		}

		public void getPatientsByLastname(){
	        try {
	            // search for the resource created
	            Bundle response = this.client.search()
	                    .forResource(Patient.class)
	                    .where(Patient.FAMILY.matches().values(this.lastname))
	                    .returnBundle(Bundle.class)
	                    .execute();

	            System.out.println("Found " + response.getTotal()
	                    + " patients called " + "'" + this.lastname);
	            
	            //initialize the patients list
	            this.patients = new ArrayList<Patient>();
	            
	            response.getEntry().forEach((entry) -> {
	            	IParser jParser = this.ctx.newJsonParser().setPrettyPrint(true);
	            	String resourceJSON = jParser.encodeResourceToString(entry.getResource());
	            	System.out.println(resourceJSON);
	            	
	            	//populate the list with the retrieved bundle's resources
	            	Patient p = (Patient) entry.getResource();
	            	this.patients.add(p);
	            	System.out.println(p.getNameFirstRep().getFamily());
	            });
	            
	        } catch (Exception e) {
	            System.out.println("An error occurred trying to search:");
	            e.printStackTrace();
	        } 
	    	
	    }
		
		public void addPatient(String firstname, String lastname){
			
	        
	        this.patient = new Patient();
			
			 Random randomGenerator = new Random();
			 int randomInt = randomGenerator.nextInt(10000000);
		
			 String prefix = "Mr";
			 
	        this.patient.addName().addPrefix(prefix).setFamily(lastname).addGiven(firstname); 
	        this.patient.addIdentifier()
	                .setSystem("tb:fhir")
	                .setValue("CP"+randomInt);
	        
	        try {
	            MethodOutcome outcome = this.client.create()
	                    .resource(this.patient)
	                    .prettyPrint()
	                    .encodedJson()
	                    .execute();

	            IdType id = (IdType) outcome.getId();
	            System.out.println("Resource is available at: " + id.getValue());

	            
	        } catch (DataFormatException e) {
	            System.out.println("An error occurred trying to upload:");
	            e.printStackTrace();
	        }

			
		}
		
		public void setLastname(String lastname) {this.lastname = lastname;}
		public String getLastname() {return this.lastname;}
		public List<Patient> getPatients() {return patients;}
		public void setPatients(List<Patient> patients) {this.patients = patients;}

		public Patient getPatient() {
			return patient;
		}

		public void setPatient(Patient patient) {
			this.patient = patient;
		}
		

}
