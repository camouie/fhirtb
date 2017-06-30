package fhirtb;

import java.util.ArrayList;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.StringType;
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
	private String serverBaseUrl = "http://fhirtest.uhn.ca/baseDstu3";
	//private String serverBaseUrl = "http://spark.furore.com/fhir";
	private FhirContext ctx;
	private Patient patient;
	private Double bodyWeight;
	private Observation Obodyweight;
	private String lastname;
	private String firstname;
	private Date birthdate;
	private String prefix;
	private String bodyweightID;

	@PostConstruct
	public void fhircontext() {
		System.out.println("observation bean reporting for duty");

		this.ctx = FhirContext.forDstu3();

		// Disable server validation (don't pull the server's metadata first)
		ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);

		// increase the timeout
		ctx.getRestfulClientFactory().setConnectTimeout(60 * 1000);
		ctx.getRestfulClientFactory().setSocketTimeout(60 * 1000);

		this.setPatient(new Patient());
		this.setOBodyweight(new Observation());
		this.setBodyWeight(0.0);
		System.out.println("at construct, bodyweight = " + bodyWeight);

	}

	/*
	 * method called in prerenderview to get the patient and observation
	 * resources before rendering the page "patient"
	 */
	public void load() throws FHIRException {
		// set the current patient's id from the url parameter
		this.logicalID = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("logicalid");

		System.out.println("------LOAD PATIENT WITH ID " + logicalID);

		if (logicalID != null) {
			this.getPatientbyID();
			this.getPatientBodyWeight();
			this.CreateResources();
			this.setVariablesFromResources();

		}

	}

	/*
	 * get the patient resource based on the LogicalID received in http parameter
	 * coming from previous page button "edit"
	 */
	private void getPatientbyID() {

		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		try {
			Bundle response = client.search().forResource(Patient.class)
					.where(new TokenClientParam("_id").exactly().code(this.logicalID)).prettyPrint()
					.returnBundle(Bundle.class).execute();
			
			this.setPatient((Patient) response.getEntry().get(0).getResource());
			//set the bean properties with the patient resource values
			this.firstname = this.patient.getNameFirstRep().getGivenAsSingleString();
			this.lastname = this.patient.getNameFirstRep().getFamily();
			this.birthdate = this.patient.getBirthDate();
			this.prefix = this.patient.getNameFirstRep().getPrefixAsSingleString();
			
			
		} catch (Exception e) {
			System.out.println("An error occurred trying to search:");
			e.printStackTrace();
		}
	}

	/*
	 * Calling the methods for the resources creations
	 */
	public void CreateResources() throws FHIRException {
		if (this.Obodyweight.isEmpty()) {
			this.CreateBodyWeightResource();
			//ask for the resoucre on the server and set the bean proprety with it
			this.getPatientBodyWeightbyID();
		}

	}

	/*
	 * Setting the bean properties with the existing values of the resources
	 * when available
	 */
	public void setVariablesFromResources() {
		//if none have been set yet, default value will be 0.0
		if(this.Obodyweight.hasValueQuantity()){
			try {
				this.bodyWeight = this.Obodyweight.getValueQuantity().getValueElement().getValueAsNumber().doubleValue();
			} catch (FHIRException e) {
				e.printStackTrace();
			}
		}
		
	}

	/*
	 * Method called by button save in Page patient, update all the resources
	 * with the inputs values then redirects to the main page "index"
	 */
	public String updateAllObs() {
		updateBodyWeight();
		updatePatient();
		return "index?faces-redirect=true";

	}

	/*
	 * Get the patient bodyweight resource, if it has one set the bean property
	 * OBodyweight with the resource gotten from server
	 */
	public void getPatientBodyWeight() {
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		try {
			Bundle response = client.search().forResource(Observation.class)
					.where(new TokenClientParam("code").exactly().code("29463-7"))
					.where(new ReferenceClientParam("patient").hasId(this.patient.getIdElement().getIdPart()))
					.prettyPrint().returnBundle(Bundle.class).execute();
			//if we get a resource from the server, we set the Obodyweight object with it
			if (response.getEntry().isEmpty() == false){
				this.setOBodyweight((Observation) response.getEntry().get(0).getResource());
				System.out.println("Obodyweight set form server resource");
			}

		} catch (DataFormatException e) {
			System.out.println("An error occurred trying to get Body Weight observation:");
			e.printStackTrace();
		}
	}

	/*
	 * Create the bodyweight observation resource for patient not having one
	 * already
	 */
	public void CreateBodyWeightResource() throws FHIRException {
		System.out.println("Create Bodyweight method called");

		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		
		// Create an Observation instance
		Observation observation = new Observation();

		observation.setSubject(new Reference(this.patient));

		// Give the observation a code (what kind of observation is this)
		Coding coding = observation.getCode().addCoding();
		coding.setCode("29463-7").setSystem("http://loinc.org").setDisplay("Body Weight");
		
		// Create a default quantity
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

		// put on the server
		try {
			MethodOutcome outcome = client.create().resource(observation).prettyPrint().encodedJson().execute();
			
			IdType id = (IdType) outcome.getId();
			this.bodyweightID = outcome.getId().getIdPart();
			System.out.println("Resource is available at: " + id.getValue());
			System.out.println("Bodyweight Id = " + this.bodyweightID);

		} catch (DataFormatException e) {
			System.out.println("An error occurred trying to upload:");
			e.printStackTrace();
		}
		
	}

	/*
	 * When saving the input weight value we update the weight observatinon
	 * resource of the patient with the entered value
	 */
	public void updateBodyWeight() {
		
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		System.out.println("when saving bodyweight variable is at " + this.bodyWeight);

		// Create a quantity datatype
		Quantity value = new Quantity();
		value.setValue(this.bodyWeight).setSystem("http://unitsofmeasure.org").setCode("kg");
		this.Obodyweight.setValue(value);
		
		System.out.println("when Updating Obodyweight ID is: " + this.Obodyweight.getId());

		try {
			MethodOutcome outcome = client.update().resource(this.Obodyweight).execute();

			IdType id = (IdType) outcome.getId();
			System.out.println("UPDATED Resource is available at: " + id.getValue());

		} catch (DataFormatException e) {
			System.out.println("An error occurred trying to update bodyweight:");
			e.printStackTrace();
		}
	}
	
	public void updatePatient(){
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		
		ArrayList <HumanName>hname = new ArrayList <HumanName>();
		HumanName name = new HumanName();
		name.setFamily(this.lastname);
		
		StringType fname = new StringType(this.firstname);
		ArrayList <StringType> given = new ArrayList <StringType>();
		given.add(fname);
		name.setGiven(given);
		
		StringType pfix = new StringType(this.prefix);
		ArrayList <StringType> prefixes = new ArrayList <StringType>();
		prefixes.add(pfix);
		name.setPrefix(prefixes);
		
		hname.add(name);
		patient.setName(hname);
		
        //this.patient.addName().addPrefix(this.prefix).setFamily(this.lastname).addGiven(this.firstname); 
        this.patient.setBirthDate(this.birthdate);
        
        try {
            MethodOutcome outcome = client.update()
                    .resource(this.patient)
                    .prettyPrint()
                    .encodedJson()
                    .execute();

            IdType id = (IdType) outcome.getId();
            System.out.println("UPDATED Resource is available at: " + id.getValue());

            
        } catch (DataFormatException e) {
            System.out.println("An error occurred trying to upload patient:");
            e.printStackTrace();
        }
		
	}
	
	private void getPatientBodyWeightbyID() {

		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		try {
			Bundle response = client.search().forResource(Observation.class)
					.where(new TokenClientParam("_id").exactly().code(this.bodyweightID)).prettyPrint()
					.returnBundle(Bundle.class).execute();
			
			this.setOBodyweight((Observation) response.getEntry().get(0).getResource());
			System.out.println("Obodyweight set in bean and is at " + Obodyweight.getId());
			
			
		} catch (Exception e) {
			System.out.println("An error occurred trying to search:");
			e.printStackTrace();
		}
	}
	/*
	 * Getters and setters methods for the bean properties
	 */

	public String getLogicalID() {
		return logicalID;
	}

	public void setLogicalID(String logicalID) {
		this.logicalID = logicalID;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Observation getOBodyweight() {
		return Obodyweight;
	}

	public void setOBodyweight(Observation bodyweight) {
		this.Obodyweight = bodyweight;
	}

	public Double getBodyWeight() {
		return bodyWeight;
	}

	public void setBodyWeight(Double bodyWeight) {
		this.bodyWeight = bodyWeight;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getBodyweightID() {
		return bodyweightID;
	}

	public void setBodyweightID(String bodyweightID) {
		this.bodyweightID = bodyweightID;
	}
	

}
