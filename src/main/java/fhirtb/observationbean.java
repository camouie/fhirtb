package fhirtb;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SimpleQuantity;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.Bundle;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.ServerValidationModeEnum;
import ca.uhn.fhir.rest.gclient.TokenClientParam;

public class observationbean {
	
	private String logicalID;
	private String serverBaseUrl = "http://fhirtest.uhn.ca/baseDstu3";
	private FhirContext ctx;
	private Patient patient;
	private double bodyWeight;
	
	@PostConstruct
	public void fhircontext () {
		System.out.println("observation bean reporting for duty");
		
		this.ctx = FhirContext.forDstu3();
		//serverBaseUrl = "http://sqlonfhir-stu3.azurewebsites.net/fhir";
		
		// Disable server validation (don't pull the server's metadata first)
		ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		
		//increase the timeout
		ctx.getRestfulClientFactory().setConnectTimeout(60 * 1000);
        ctx.getRestfulClientFactory().setSocketTimeout(60 * 1000);
        
        this.setPatient(new Patient());

    	
    
	}
	
	public void load(){
		//set the current patient's id from the url parameter
		this.logicalID = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
                .get("logicalid");
		
		System.out.println("-------//------LOAD PATIENT WITH ID "+ logicalID);
		
		if(logicalID!=null){
	        //get the patient based on the ID
	    	IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
	    	
	    	 try {
	    	        Bundle response = client.search()
	    	        		.forResource(Patient.class)
	    	        		.where(new TokenClientParam("_id").exactly().code(this.logicalID))
	    	        		.prettyPrint()
	    	        		.returnBundle(Bundle.class)
	    	        		.execute();
	    	        
	    	        this.setPatient((Patient) response.getEntry().get(0).getResource());
	    	 }
	    	 catch (Exception e) {
		            System.out.println("An error occurred trying to search:");
		            e.printStackTrace();
		        } 

	       		
	    	}
		
	}
	
	//get a patient by its ID and get his vital signs
	public void getPatientObs(){
				
	}
	//set the bodyweight of the patient given its ID
	public void setPatientBodyWeight(){
		// Create an Observation instance
		Observation observation = new Observation();
		
		observation.setSubject(new Reference(this.logicalID));
		  
		// Give the observation a code (what kind of observation is this)
		Coding coding = observation.getCode().addCoding();
		coding.setCode("29463-7").setSystem("http://loinc.org").setDisplay("Body Weight");
		 
		// Create a quantity datatype
		Quantity value = new Quantity();
		value.setValue(this.bodyWeight).setSystem("http://unitsofmeasure.org").setCode("kg");
		observation.setValue(value);
		 
		// Set the reference range
		SimpleQuantity low = new SimpleQuantity();
		low.setValue(45).setSystem("http://unitsofmeasure.org").setCode("kg");
		observation.getReferenceRangeFirstRep().setLow(low);
		
		SimpleQuantity high = new SimpleQuantity();
		low.setValue(90).setSystem("http://unitsofmeasure.org").setCode("kg");
		observation.getReferenceRangeFirstRep().setHigh(high);
		
		//put on the server
	}

	public String getLogicalID() {return logicalID;}
	public void setLogicalID(String logicalID) {this.logicalID = logicalID;}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public double getBodyWeight() {
		return bodyWeight;
	}

	public void setBodyWeight(double bodyWeight) {
		this.bodyWeight = bodyWeight;
	}

}
