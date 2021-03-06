package fhirtb;

import java.util.ArrayList;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.exceptions.FHIRException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;

import org.hl7.fhir.dstu3.model.Bundle;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;

public class observationbean {
	// HTTP parameter
	private String logicalID;
	// references and FHIR connection
	private Fhircontextconnection fco;
	private String serverBaseUrl;
	private FhirContext ctx;
	private VitalSignsHandler bwhandler;

	// object for the resources in the bean
	private Patient patient;
	private Observation Obodyweight;
	private Observation ObodyHeight;
	private Observation Oheartrate;

	// page inputs
	private String lastname;
	private String firstname;
	private Date birthdate;
	private String prefix;
	private Double bodyWeight;
	private Double bodyHeight;
	private Double heartrate;

	// resources ID (when not saving the whole resource in the bean)
	private String bodyweightID;
	private String bodyheightID;
	private String heartrateID;

	@PostConstruct
	public void fhircontext() {
		System.out.println("observation bean reporting for duty");

		this.FhirCo();
		// initialize objects
		this.setPatient(new Patient());
		this.setObodyweight(new Observation());
		this.setObodyHeight(new Observation());
		this.setOheartrate(new Observation());

		// default values
		this.setBodyWeight(0.0);
		this.setBodyHeight(0.0);
		this.setHeartrate(0.0);

		System.out.println("at construct, bodyweight = " + bodyWeight);
		System.out.println("at construct, bodyheight = " + bodyHeight);
		System.out.println("at construct, Heart rate = " + heartrate);
		// Instantiate handler class for the vital sign resources interactions
		this.bwhandler = new VitalSignsHandler();

	}

	public void FhirCo() {
		this.fco = new Fhircontextconnection();
		this.serverBaseUrl = fco.getServerBaseUrl();
		this.ctx = fco.getCtx();
	}

	/*
	 * method called in prerenderview to get the patient and observation
	 * resources before rendering the page "patient"
	 */
	public void load() throws FHIRException, InterruptedException {
		String role = (String) SessionUtils.getSession().getAttribute("role");
		if (!role.equals("patient")) {
			// set the current patient's id from the url parameter for admin and
			// doctors
			this.logicalID = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
					.get("logicalid");

		} else {
			this.logicalID = (String) SessionUtils.getSession().getAttribute("fhirid");
		}

		System.out.println("------LOAD PATIENT WITH ID " + logicalID);

		this.loading();

	}

	/*
	 * method called by page welcome only for patient users
	 */
	public void welcomeload() throws FHIRException, InterruptedException {
		// set the current patient's id from the url parameter
		this.logicalID = (String) SessionUtils.getSession().getAttribute("fhirid");

		System.out.println("------LOAD PATIENT WITH ID " + logicalID);

		this.loading();

	}

	public void loading() throws FHIRException {
		if (logicalID != null) {
			// get the patient from the server with its id in order to update
			// the patient on save button
			this.getPatientbyID();
			// get the existing vital sign resources from server or create ones
			// if none existing
			this.SetBodyWeightResource();
			this.SetBodyHeightResource();
			this.SetHeartRateResource();
			// set the bean variable now the resources are created or found on
			// server
			this.setVariablesFromResources();
		}
	}

	/*
	 * get the patient resource based on the LogicalID received in http
	 * parameter coming from previous page button "edit"
	 */
	private void getPatientbyID() {

		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		
		this.patient = client.read()
                .resource(Patient.class)
                .withId(this.logicalID)
                .execute();
		
			// set the bean properties with the patient resource values
			this.firstname = this.patient.getNameFirstRep().getGivenAsSingleString();
			this.lastname = this.patient.getNameFirstRep().getFamily();
			this.birthdate = this.patient.getBirthDate();
			this.prefix = this.patient.getNameFirstRep().getPrefixAsSingleString();

	}

	/*
	 * Calling the methods for the resources creations
	 */
	public void SetBodyWeightResource() throws FHIRException {
		System.out.println("SetBodyWeight method called");
		this.FhirCo();
		String code = "bodyweight";
		// get the patient's bodyweight
		this.setObodyweight(bwhandler.getPatientVital(this.patient, code));
		// if the bodyweight resource gotten is empty means we need to create
		// one for the patient
		if (this.Obodyweight.isEmpty()) {
			this.bodyweightID = bwhandler.CreateVitalResource(this.patient, this.bodyWeight, code);
			// ask for the resource on the server and set the bean property with
			// it
			this.setObodyweight(bwhandler.getPatientVitalsbyID(this.bodyweightID));
		}

	}

	public void SetBodyHeightResource() throws FHIRException {
		this.FhirCo();
		System.out.println("SetBodyHeight method called");
		String code = "bodyheight";
		// get the patient's bodyweight
		this.setObodyHeight(bwhandler.getPatientVital(this.patient, code));
		// if the bodyweight resource gotten is empty means we need to create
		// one for the patient
		if (this.ObodyHeight.isEmpty()) {
			this.bodyheightID = bwhandler.CreateVitalResource(this.patient, this.bodyHeight, code);
			// ask for the resource on the server and set the bean property with
			// it
			this.setObodyHeight(bwhandler.getPatientVitalsbyID(this.bodyheightID));
		}

	}

	public void SetHeartRateResource() throws FHIRException {
		this.FhirCo();
		System.out.println("SetHeartRate method called");
		String code = "heartrate";

		// get the patient's heartrate
		this.setOheartrate(bwhandler.getPatientVital(this.patient, code));

		// if the heart rate resource gotten is empty means we need to create
		// one for the patient
		if (this.Oheartrate.isEmpty()) {
			this.heartrateID = bwhandler.CreateVitalResource(this.patient, this.heartrate, code);
			// ask for the resource on the server and set the bean property with
			// it
			this.setOheartrate(bwhandler.getPatientVitalsbyID(this.heartrateID));
		}

	}

	/*
	 * Setting the bean properties with the existing values of the resources
	 * when available
	 */
	public void setVariablesFromResources() throws FHIRException {
		// if none have been set yet, default value will be 0.0
		if (this.Obodyweight.hasValueQuantity()) {
			this.bodyWeight = this.Obodyweight.getValueQuantity().getValueElement().getValueAsNumber().doubleValue();
		}
		if (this.ObodyHeight.hasValueQuantity()) {
			this.bodyHeight = this.ObodyHeight.getValueQuantity().getValueElement().getValueAsNumber().doubleValue();
		}
		if (this.Oheartrate.hasValueQuantity()) {
			this.heartrate = this.Oheartrate.getValueQuantity().getValueElement().getValueAsNumber().doubleValue();
		}

	}

	/*
	 * Method called by button save in Page patient, update all the resources
	 * with the inputs values then redirects to the main page "index"
	 */
	public String updateAllObs() {
		bwhandler.updateVitalResource(this.bodyWeight, this.Obodyweight, "bodyweight");
		bwhandler.updateVitalResource(this.bodyHeight, this.ObodyHeight, "bodyheight");
		bwhandler.updateVitalResource(this.heartrate, this.Oheartrate, "heartrate");

		// update the changes made to patient infos in inputs
		updatePatient();

		viewNavigation vn = new viewNavigation();
		return vn.goHome();

	}

	public String updateVitals() {
		System.out.println("[[[[[[[[[[[[[[ update vitals called");
		bwhandler.updateVitalResource(this.bodyWeight, this.Obodyweight, "bodyweight");
		bwhandler.updateVitalResource(this.bodyHeight, this.ObodyHeight, "bodyheight");
		bwhandler.updateVitalResource(this.heartrate, this.Oheartrate, "heartrate");

		viewNavigation vn = new viewNavigation();
		return vn.goHome();
	}
	
	public void updatePatient() {
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		ArrayList<HumanName> hname = new ArrayList<HumanName>();
		HumanName name = new HumanName();
		name.setFamily(this.lastname);

		StringType fname = new StringType(this.firstname);
		ArrayList<StringType> given = new ArrayList<StringType>();
		given.add(fname);
		name.setGiven(given);

		StringType pfix = new StringType(this.prefix);
		ArrayList<StringType> prefixes = new ArrayList<StringType>();
		prefixes.add(pfix);
		name.setPrefix(prefixes);

		hname.add(name);
		patient.setName(hname);

		// this.patient.addName().addPrefix(this.prefix).setFamily(this.lastname).addGiven(this.firstname);
		this.patient.setBirthDate(this.birthdate);

		try {
			MethodOutcome outcome = client.update().resource(this.patient).prettyPrint().encodedJson().execute();

			IdType id = (IdType) outcome.getId();
			System.out.println("UPDATED Resource is available at: " + id.getValue());

		} catch (DataFormatException e) {
			System.out.println("An error occurred trying to upload patient:");
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

	public Double getBodyWeight() {
		return bodyWeight;
	}

	public void setBodyWeight(Double bodyWeight) {
		this.bodyWeight = bodyWeight;
	}

	public Observation getObodyweight() {
		return Obodyweight;
	}

	public void setObodyweight(Observation obodyweight) {
		Obodyweight = obodyweight;
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

	public VitalSignsHandler getBwhandler() {
		return bwhandler;
	}

	public void setBwhandler(VitalSignsHandler bwhandler) {
		this.bwhandler = bwhandler;
	}

	public String getBodyweightID() {
		return bodyweightID;
	}

	public void setBodyweightID(String bodyweightID) {
		this.bodyweightID = bodyweightID;
	}

	public Observation getObodyHeight() {
		return ObodyHeight;
	}

	public void setObodyHeight(Observation obodyHeight) {
		ObodyHeight = obodyHeight;
	}

	public String getBodyheightID() {
		return bodyheightID;
	}

	public void setBodyheightID(String bodyheightID) {
		this.bodyheightID = bodyheightID;
	}

	public Double getBodyHeight() {
		return bodyHeight;
	}

	public void setBodyHeight(Double bodyHeight) {
		this.bodyHeight = bodyHeight;
	}

	public Observation getOheartrate() {
		return Oheartrate;
	}

	public void setOheartrate(Observation oheartrate) {
		Oheartrate = oheartrate;
	}

	public Double getHeartrate() {
		return heartrate;
	}

	public void setHeartrate(Double heartrate) {
		this.heartrate = heartrate;
	}

	public String getHeartrateID() {
		return heartrateID;
	}

	public void setHeartrateID(String heartrateID) {
		this.heartrateID = heartrateID;
	}

}
