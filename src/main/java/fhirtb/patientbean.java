package fhirtb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

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
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;

public class patientbean {

	private String lastname;
	private List<Patient> patients;
	private Patient patient;
	private Practitioner doctor;
	private Date birthdate;
	private List<Practitioner> doctors;
	private String doctorid;
	private String patientid;

	// session param
	private String fhirid;
	HttpSession session;

	// DB query parameter to add a patient account
	private String password;
	private String email;

	private String serverBaseUrl;
	private FhirContext ctx;
	private Fhircontextconnection fco;

	@PostConstruct
	public void fhircontext() {
		System.out.println("<<<<<<<< PATIENT BEAN REPORTING FOR DUTY >>>>>>>>>");

		this.fco = new Fhircontextconnection();
		this.serverBaseUrl = fco.getServerBaseUrl();
		this.ctx = fco.getCtx();

		// initialize the patients list
		this.patients = new ArrayList<Patient>();
		this.doctors = new ArrayList<Practitioner>();

		// get the fhirid form session param
		this.session = SessionUtils.getSession();
		this.fhirid = (String) this.session.getAttribute("fhirid");
		System.out.println(">>>>>>>>>>>>>>>>> fhirid of this user is : " + this.fhirid);

		// if the user is a doctor, fill directly the patients list with his
		// patients from fhir server
		if (session.getAttribute("role").equals("doctor")) {
			System.out.println("role = doctor so fill the list");
			//IF using a fhir server implementing the reference to practitioner from patient resource use the following line
			this.getDoctorsPatients(this.fhirid); 
			//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			//else use the following
			//this.getPatientsForDoctorHAPI(this.fhirid);
		}

	}

	/*
	 * called by prerenderview method on page addpatient
	 */
	public void load() throws ClassNotFoundException {
		getDoctorsForPatient();
		this.setDoctor(new Practitioner());
	}

	/*
	 * the search by lastname when the user is a doctor will only return his
	 * assigned patient resources form the server admin user can see all the
	 * available patient resources
	 */
	public void getPatientsByLastname() {
		if (session.getAttribute("role").equals("doctor")) {
			this.getPatientsByLastnameDoctor(this.fhirid);
		}
		if (session.getAttribute("role").equals("admin")) {
			this.getPatientsByLastnameAdmin();
		}
	}

	/*
	 * get a bundle of all the patients by a lastname search, lastname stored in
	 * bean proprety "lastname"
	 */
	public void getPatientsByLastnameAdmin() {
		// create the RESTful client to work with our FHIR server
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		try {
			// search for the resource created
			Bundle response = client.search().forResource(Patient.class)
					.where(Patient.FAMILY.matches().values(this.lastname)).returnBundle(Bundle.class).execute();

			System.out.println("Found " + response.getTotal() + " patients called " + " : " + this.lastname);

			// initialize the patients list
			this.patients = new ArrayList<Patient>();
			System.out.println("Admin / Patients list initialization");

			response.getEntry().forEach((entry) -> {
				IParser jParser = this.ctx.newJsonParser().setPrettyPrint(true);
				String resourceJSON = jParser.encodeResourceToString(entry.getResource());
				System.out.println(resourceJSON);

				// populate the list with the retrieved bundle's resources
				Patient p = (Patient) entry.getResource();
				this.patients.add(p);
				System.out.println("----------------" + p.getNameFirstRep().getFamily());
			});

		} catch (Exception e) {
			System.out.println("An error occurred trying to search:");
			e.printStackTrace();
		}

	}

	/*
	 * get the patients according to their lastnames of a doctor to populate the
	 * patients list of the bean with the doctor ID as parameter
	 */
	public void getPatientsByLastnameDoctor(String doctorID) {
		// create the RESTful client to work with our FHIR server
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		try {
			// search for the resource created
			Bundle response = client.search().forResource(Patient.class)
					.where(Patient.FAMILY.matches().values(this.lastname))
					.where(new ReferenceClientParam("general-practitioner").hasId(doctorID)).returnBundle(Bundle.class)
					.execute();

			System.out.println("Found " + response.getTotal() + " patients called " + " : " + this.lastname);

			// initialize the patients list
			this.patients = new ArrayList<Patient>();
			System.out.println("Doctor / Patients list initialization");

			response.getEntry().forEach((entry) -> {
				IParser jParser = this.ctx.newJsonParser().setPrettyPrint(true);
				String resourceJSON = jParser.encodeResourceToString(entry.getResource());
				System.out.println(resourceJSON);

				// populate the list with the retrieved bundle's resources
				Patient p = (Patient) entry.getResource();
				this.patients.add(p);
				System.out.println("----------------" + p.getNameFirstRep().getFamily());
			});

		} catch (Exception e) {
			System.out.println("An error occurred trying to search:");
			e.printStackTrace();
		}

	}

	/*
	 * retrieve the list of all doctor resources from id stored in DB
	 */
	public void getDoctorsForPatient() throws ClassNotFoundException {
		// create the RESTful client to work with our FHIR server
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		ArrayList<String> doctorsid = new ArrayList<String>();
		doctorsid = DAO.getPractitioners();

		for (String doctorid : doctorsid) {
			try {
				Bundle response = client.search().forResource(Practitioner.class)
						.where(new TokenClientParam("_id").exactly().code(doctorid)).prettyPrint()
						.returnBundle(Bundle.class).execute();

				System.out.println("Found " + response.getTotal() + " doctor with ID " + doctorid);

				if (response.getEntry().size() != 0) {
					Practitioner p = (Practitioner) response.getEntry().get(0).getResource();
					System.out.println("adding doctor from server to bean list with lastname = "
							+ p.getNameFirstRep().getFamily());

					this.doctors.add(p);
				}

			} catch (Exception e) {
				System.out.println("An error occurred trying to search:");
				e.printStackTrace();
			}
		}

}
	/*
	 * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	 * Method to retrieve all patient resources of a doctor based on all their ids stored in DB
	 * 
	 * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	 */
	public void getPatientsForDoctorHAPI(String idfhir){
		// create the RESTful client to work with our FHIR server
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		ArrayList<String> patientsid = new ArrayList<String>();
		try {
			patientsid = DAO.getPatientsforPractitioner(idfhir);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (String patientid : patientsid) {
			try {
				Bundle response = client.search().forResource(Patient.class)
						.where(new TokenClientParam("_id").exactly().code(patientid)).prettyPrint()
						.returnBundle(Bundle.class).execute();

				System.out.println("Found " + response.getTotal() + " patients with ID doctor = " + doctorid);
				
				// initialize the patients list
				this.patients = new ArrayList<Patient>();
				System.out.println("Initialize patients list for doctor // DB");
				
				response.getEntry().forEach((entry) -> {
					// populate the list with the retrieved bundle's resources
					Patient p = (Patient) entry.getResource();
					this.patients.add(p);
					System.out.println("----------------ADDED PATIENT CALLED : " + p.getNameFirstRep().getFamily());
				});

			} catch (Exception e) {
				System.out.println("An error occurred trying to search:");
				e.printStackTrace();
			}
		}

	}
	/*
	 * get all the patients of a doctor retrieved by its ID
	 */
	public void getDoctorsPatients(String doctorID) {

		// create the RESTful client to work with our FHIR server
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		try {
			Bundle response = client.search().forResource(Patient.class)
					.where(new ReferenceClientParam("general-practitioner").hasId(doctorID)).prettyPrint()
					.returnBundle(Bundle.class).execute();

			System.out.println("Found " + response.getTotal() + " patients with doctor id " + ":" + doctorID);

			// initialize the patients list
			this.patients = new ArrayList<Patient>();
			System.out.println("Initialize patients list for doctor");

			response.getEntry().forEach((entry) -> {
				// populate the list with the retrieved bundle's resources
				Patient p = (Patient) entry.getResource();
				this.patients.add(p);
				System.out.println("----------------ADDED PATIENT CALLED : " + p.getNameFirstRep().getFamily());
			});

		} catch (Exception e) {
			System.out.println("An error occurred trying to search:");
			e.printStackTrace();
		}

	}


	/*
	 * add a patient resource onto the server taking as parameters the
	 * firstname, the lastname and the prefix the birthdate is taken from the
	 * bean proprety
	 */
	public String addPatient(String firstname, String lastname, String prefix) throws ClassNotFoundException {

		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		this.patient = new Patient();

		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(10000000);

		this.patient.addName().addPrefix(prefix).setFamily(lastname).addGiven(firstname);
		this.patient.setBirthDate(this.birthdate);
		this.patient.addIdentifier().setSystem("tb:fhir").setValue("CP" + randomInt);

		// get the selected doctor on the page from the fhir server
		/*
		 * /!\ general practitioner attribute does not exist on most FHIR server implementations
		 */
		this.getSelectedDoctorbyID();
		this.patient.addGeneralPractitioner();
		List<Reference> ref = new ArrayList<Reference>();
		ref.add(new Reference(this.doctor));
		this.patient.setGeneralPractitioner(ref);
		
		System.out.println("doctor added has name : " + this.doctor.getNameFirstRep().getFamily());
		System.out.println("DOCTOR SET FOR PATIENT");

		try {
			MethodOutcome outcome = client.create().resource(this.patient).prettyPrint().encodedJson().execute();

			IdType id = (IdType) outcome.getId();
			System.out.println("Resource is available at: " + id.getValue());
			this.patientid = id.getIdPart();
			// create the observation now, because of latencies of some test
			// servers
			this.createOBSforPatient();
			// create an account for the patient in the DB so he can login later
			// on
			this.createPatientAccount();

		} catch (DataFormatException e) {
			System.out.println("An error occurred trying to upload:");
			e.printStackTrace();
		}

		viewNavigation vn = new viewNavigation();
		return vn.goHome();

	}



	/*
	 * retrieve a Doctor resource given its ID
	 */
	public void getSelectedDoctorbyID() {
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		try {
			Bundle response = client.search().forResource(Practitioner.class)
					.where(new TokenClientParam("_id").exactly().code(this.doctorid)).prettyPrint()
					.returnBundle(Bundle.class).execute();

			System.out.println("Found the selected doctor on the server for patient. doctor id : " + doctorid);

			Practitioner p = (Practitioner) response.getEntry().get(0).getResource();
			System.out.println(
					"-----doctor set from server to bean doctor with name = " + p.getNameFirstRep().getFamily());
			this.setDoctor(p);

		} catch (Exception e) {
			System.out.println("An error occurred trying to search:");
			e.printStackTrace();
		}
	}

	/*
	 * create the observation resources needed for the patient in the
	 * application
	 */
	public void createOBSforPatient() {
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		try {
			Bundle response = client.search().forResource(Patient.class)
					.where(new TokenClientParam("_id").exactly().code(this.patientid)).prettyPrint()
					.returnBundle(Bundle.class).execute();

			this.setPatient((Patient) response.getEntry().get(0).getResource());
			VitalSignsHandler vh = new VitalSignsHandler();
			vh.CreateVitalResource(this.patient, 0.0, "bodyweight");
			vh.CreateVitalResource(this.patient, 0.0, "bodyheight");
			vh.CreateVitalResource(this.patient, 0.0, "heartrate");

		} catch (Exception e) {
			System.out.println("An error occurred trying to search:");
			e.printStackTrace();
		}
	}

	/*
	 * method to create a patient account in the DB
	 */
	public void createPatientAccount() throws ClassNotFoundException {
		DAO dao = new DAO();
		dao.addPatientAccount(this.email, this.password, this.patientid, this.doctorid);
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

	public Practitioner getDoctor() {
		return doctor;
	}

	public void setDoctor(Practitioner doctor) {
		this.doctor = doctor;
	}

	public String getPatientid() {
		return patientid;
	}

	public void setPatientid(String patientid) {
		this.patientid = patientid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFhirid() {
		return fhirid;
	}

	public void setFhirid(String fhirid) {
		this.fhirid = fhirid;
	}

}
