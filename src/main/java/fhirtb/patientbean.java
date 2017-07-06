package fhirtb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;

public class patientbean {

	private String lastname;
	private List<Patient> patients;
	private Patient patient;
	private Practitioner doctor;
	private Date birthdate;
	private List<Practitioner> doctors;
	private String doctorid;
	
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
		this.doctors = new ArrayList<Practitioner>();

	}
	/*
	 * called by prerenderview method on page addpatient
	 */
	public void load() throws ClassNotFoundException{
		this.getDoctorsForPatient();
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
		
		//get the selected doctor on the page from the fhir server
		this.getSelectedDoctorbyID();
		this.patient.addGeneralPractitioner(new Reference(this.doctor));

		try {
			MethodOutcome outcome = client.create().resource(this.patient).prettyPrint().encodedJson().execute();

			IdType id = (IdType) outcome.getId();
			System.out.println("Resource is available at: " + id.getValue());

		} catch (DataFormatException e) {
			System.out.println("An error occurred trying to upload:");
			e.printStackTrace();
		}

	}
	public void getDoctorsForPatient() throws ClassNotFoundException {
		// create the RESTful client to work with our FHIR server
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		ArrayList<String> doctorsid = new ArrayList<String>();
		doctorsid = DAO.getPractitioners();
		
		for (String doctorid : doctorsid)
		{
		try {
			Bundle response = client.search().forResource(Practitioner.class)
					.where(new TokenClientParam("_id").exactly().code(doctorid))
					.prettyPrint()
					.returnBundle(Bundle.class)
					.execute();

			System.out.println("Found " + response.getTotal() + " doctor with ID " + doctorid);

			if (response.getEntry().size() != 0) {
				Practitioner p = (Practitioner) response.getEntry().get(0).getResource();
				System.out.println("adding doctor from server to bean list with uname = " + p.getCommunicationFirstRep());
				
				this.doctors.add(p);
			}

		} catch (Exception e) {
			System.out.println("An error occurred trying to search:");
			e.printStackTrace();
		}
		}

	}
	
	public void getSelectedDoctorbyID(){
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		try {
			Bundle response = client.search().forResource(Practitioner.class)
					.where(new TokenClientParam("identifier").exactly().code(this.doctorid))
					.prettyPrint()
					.returnBundle(Bundle.class)
					.execute();

			System.out.println("Found the selected doctor on the server for patient. doctor id : " + doctorid);

			if (response.getEntry().size() != 0) {
				Practitioner p = (Practitioner) response.getEntry().get(0).getResource();
				System.out.println("adding doctor from server to bean list with uname = " + p.getCommunicationFirstRep());
				
				this.setDoctor(p);
			}

		} catch (Exception e) {
			System.out.println("An error occurred trying to search:");
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

	public List<Practitioner> getDoctors() {
		return doctors;
	}

	public void setDoctors(List<Practitioner> doctors) {
		this.doctors = doctors;
	}
	public String getDoctorid() {
		return doctorid;
	}
	public void setDoctorid(String doctorid) {
		this.doctorid = doctorid;
	}
	public void setDoctor(Practitioner doctor) {
		this.doctor = doctor;
	}
	
	
	
}
