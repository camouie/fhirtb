package fhirtb;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.exceptions.FHIRException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;

import org.hl7.fhir.dstu3.model.Bundle;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.ServerValidationModeEnum;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;

public class observationbean {
	
	private String logicalID;
	//private String serverBaseUrl = "http://fhirtest.uhn.ca/baseDstu3";
	private String serverBaseUrl = "http://spark.furore.com/fhir";
	private FhirContext ctx;
	private Patient patient;
	private Double bodyWeight;
	private Observation Obodyweight;
	
	
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
        this.setBodyWeight(2.0);
        System.out.println("at construct, bodyweight = " + bodyWeight);
    
	}
	
	public void load() throws FHIRException{
		//set the current patient's id from the url parameter
		this.logicalID = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
                .get("logicalid");
		
		System.out.println("------LOAD PATIENT WITH ID "+ logicalID);
		
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
	    	 
	    	 this.getPatientBodyWeight();
	    	 this.CreateResources();
	    	 this.setVariablesFromResources();
	    	 
	       		
	    	}
		
	}
	public void CreateResources(){
		if(this.Obodyweight==null){
   		 this.CreateBodyWeightResource();
   		 this.getPatientBodyWeight();
   	 }
		
	}
	
	public void setVariablesFromResources() throws FHIRException{
		if(Obodyweight.hasValueQuantity()==true){
		this.bodyWeight = Obodyweight.getValueQuantity().getValueElement().getValueAsNumber().doubleValue();
		}
	}
	
	public void updateAllObs(){
		updateBodyWeight();
		
	}
	
	//get a patient bodyweight using its ID
	public void getPatientBodyWeight(){
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		
		try {
			Bundle response = client.search().forResource(Observation.class)
					.where(new TokenClientParam("code").exactly().code("29463-7"))
					.where(new ReferenceClientParam("patient").hasId(this.patient.getIdElement().getIdPart()))
					.prettyPrint()
					.returnBundle(Bundle.class)
					.execute();
			if(response.getEntry().isEmpty() == false)
			this.setOBodyweight((Observation) response.getEntry().get(0).getResource());

            
        } catch (DataFormatException e) {
            System.out.println("An error occurred trying to get Body Weight observation:");
            e.printStackTrace();
        }	
				
	}
	//set the bodyweight of the patient given its ID
	public void CreateBodyWeightResource(){
		System.out.println("Body weight method called");
		
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		
		// Create an Observation instance
		Observation observation = new Observation();
		
		observation.setSubject(new Reference(this.patient));
		  
		// Give the observation a code (what kind of observation is this)
		Coding coding = observation.getCode().addCoding();
		coding.setCode("29463-7").setSystem("http://loinc.org").setDisplay("Body Weight");
		 
		// Set the reference range
		SimpleQuantity low = new SimpleQuantity();
		low.setValue(45).setSystem("http://unitsofmeasure.org").setCode("kg");
		observation.getReferenceRangeFirstRep().setLow(low);
		
		SimpleQuantity high = new SimpleQuantity();
		low.setValue(90).setSystem("http://unitsofmeasure.org").setCode("kg");
		observation.getReferenceRangeFirstRep().setHigh(high);
		
		//put on the server
		try {
            MethodOutcome outcome = client.create()
                    .resource(observation)
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
	
	public void updateBodyWeight(){
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		System.out.println("when saving bodyweight variable is at " + this.bodyWeight);
		
		// Create a quantity datatype
		Quantity value = new Quantity();
		value.setValue(this.bodyWeight).setSystem("http://unitsofmeasure.org").setCode("kg");
		this.Obodyweight.setValue(value);
		
		try{
		MethodOutcome outcome = client.update()
				   .resource(this.Obodyweight)
				   .execute();
		
		IdType id = (IdType) outcome.getId();
        System.out.println("UPDATED Resource is available at: " + id.getValue());
		
		} catch (DataFormatException e) {
            System.out.println("An error occurred trying to update bodyweight:");
            e.printStackTrace();
        }
	}

	public String getLogicalID() {return logicalID;}
	public void setLogicalID(String logicalID) {this.logicalID = logicalID;}
	public Patient getPatient() {return patient;}
	public void setPatient(Patient patient) {this.patient = patient;}
	public Observation getOBodyweight() {return Obodyweight;}
	public void setOBodyweight(Observation bodyweight) {this.Obodyweight = bodyweight;}

	public Double getBodyWeight() {
		return bodyWeight;
	}

	public void setBodyWeight(Double bodyWeight) {
		this.bodyWeight = bodyWeight;
	}

}
