package fhirtb;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;

public class practitionerbean {
	private String lastname;
	private String firstname;
	private Practitioner practitioner;
	private Date birthdate;
	private String gender;
	private String email;
	private String telephone;
	private String prefix;

	private String password;

	private String serverBaseUrl;
	private FhirContext ctx;
	private Fhircontextconnection fco;

	@PostConstruct
	public void fhircontext() {
		System.out.println("<<<<< PRACTITIONER BEAN REPORTING FOR DUTY>>>>>>>>>>>>");
		this.fco = new Fhircontextconnection();
		this.serverBaseUrl = fco.getServerBaseUrl();
		this.ctx = fco.getCtx();

	}

	/*
	 * add a practitioner resource onto the server taking parameters taken from
	 * the bean proprety
	 */
	public String addPractitioner() throws ClassNotFoundException {

		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		this.practitioner = new Practitioner();

		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(10000000);

		this.practitioner.addName().addPrefix(this.prefix).setFamily(this.lastname).addGiven(this.firstname);
		this.practitioner.setBirthDate(this.birthdate);
		this.practitioner.addIdentifier().setSystem("tb:fhir").setValue("CP" + randomInt);
		this.practitioner.setActive(true);
		if (this.gender.equals("F"))
			this.practitioner.setGender(AdministrativeGender.FEMALE);
		if (this.gender.equals("M"))
			this.practitioner.setGender(AdministrativeGender.MALE);

		ContactPoint email = new ContactPoint();
		email.setSystem(ContactPoint.ContactPointSystem.EMAIL);
		email.setValue(this.email);

		ContactPoint tel = new ContactPoint();
		tel.setSystem(ContactPoint.ContactPointSystem.PHONE);
		tel.setValue(this.telephone);

		ArrayList<ContactPoint> telecom = new ArrayList<ContactPoint>();
		telecom.add(email);
		telecom.add(tel);

		this.practitioner.setTelecom(telecom);
		
		boolean userExists = DAO.userExists(this.email);
		if(!userExists){
		try {
			MethodOutcome outcome = client.create().resource(this.practitioner).prettyPrint().encodedJson().execute();

			IdType id = (IdType) outcome.getId();
			System.out.println("Resource is available at: " + id.getValue());
			this.practitioner.setId(outcome.getId());

		} catch (DataFormatException e) {
			System.out.println("An error occurred trying to upload:");
			e.printStackTrace();
		}

		System.out.println("adding doctor to database");
		try {
			DAO.addPractitionerAccount(this.email, this.password, this.practitioner.getIdElement().getIdPart());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		viewNavigation vn = new viewNavigation();
		return vn.goHome();
		}
		else{
			System.out.println("user already exists for Practitioner");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Username already exists", "Please enter another username or login with existing one"));
			return "addPractitioner";
		}

	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Practitioner getPractitioner() {
		return practitioner;
	}

	public void setPractitioner(Practitioner practitioner) {
		this.practitioner = practitioner;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
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

	public Fhircontextconnection getFco() {
		return fco;
	}

	public void setFco(Fhircontextconnection fco) {
		this.fco = fco;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
