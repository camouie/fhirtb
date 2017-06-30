package fhirtb;

import java.util.ArrayList;
import java.util.Date;
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

public class patientbean {

	private String lastname;
	private List<Patient> patients;
	private Patient patient;
	private Date birthdate;
	private String serverBaseUrl;
	private FhirContext ctx;
	private Fhircontextconnection fco;

	@PostConstruct
	public void fhircontext() {
		
		this.fco = new Fhircontextconnection();
		this.serverBaseUrl = fco.getServerBaseUrl();
		this.ctx = fco.getCtx();
		
		//initialize the patients list
		this.patients = new ArrayList<Patient>();

	}

	/*
	 * get a bundle of all the patients by a lastname search, lastname stored in bean proprety "lastname"
	 */
	public void getPatientsByLastname() {
		// create the RESTful client to work with our FHIR server
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		try {
			// search for the resource created
			Bundle response = client.search().forResource(Patient.class)
					.where(Patient.FAMILY.matches().values(this.lastname)).returnBundle(Bundle.class).execute();

			System.out.println("Found " + response.getTotal() + " patients called " + "'" + this.lastname);

			// initialize the patients list
			this.patients = new ArrayList<Patient>();
			System.out.println("j'initialize la liste de patients");

			response.getEntry().forEach((entry) -> {
				IParser jParser = this.ctx.newJsonParser().setPrettyPrint(true);
				String resourceJSON = jParser.encodeResourceToString(entry.getResource());
				System.out.println(resourceJSON);

				// populate the list with the retrieved bundle's resources
				Patient p = (Patient) entry.getResource();
				this.patients.add(p);
				System.out.println("----------------" + p.getName().toString());
			});

		} catch (Exception e) {
			System.out.println("An error occurred trying to search:");
			e.printStackTrace();
		}

	}

	/*
	 * add a patient resource onto the server taking as parameters the firstname, the lastname  and the prefix
	 * the birthdate is taken from the bean proprety
	 */
	public void addPatient(String firstname, String lastname, String prefix) {

		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		this.patient = new Patient();

		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(10000000);

		this.patient.addName().addPrefix(prefix).setFamily(lastname).addGiven(firstname);
		this.patient.setBirthDate(this.birthdate);
		this.patient.addIdentifier().setSystem("tb:fhir").setValue("CP" + randomInt);

		try {
			MethodOutcome outcome = client.create().resource(this.patient).prettyPrint().encodedJson().execute();

			IdType id = (IdType) outcome.getId();
			System.out.println("Resource is available at: " + id.getValue());

		} catch (DataFormatException e) {
			System.out.println("An error occurred trying to upload:");
			e.printStackTrace();
		}

	}
	/*
	 * Getters and setters methods
	 */

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getLastname() {
		return this.lastname;
	}

	public List<Patient> getPatients() {
		return patients;
	}

	public void setPatients(List<Patient> patients) {
		this.patients = patients;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

}
