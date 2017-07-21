package fhirtb;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.codesystems.NarrativeStatus;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;

public class VitalSignsHandler {
	private String serverBaseUrl;
	private FhirContext ctx;
	private Fhircontextconnection fco;

	public VitalSignsHandler() {
		this.FhirCo();
	}

	public void FhirCo() {
		this.fco = new Fhircontextconnection();
		this.serverBaseUrl = fco.getServerBaseUrl();
		this.ctx = fco.getCtx();
	}

	/*
	 * Create the vital sign observation resource for patient not having one
	 * already
	 */
	public String CreateVitalResource(Patient patient, double bodymeasure, String code) throws FHIRException {
		System.out.println("Create VitalSign method called");

		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		// Create an Observation instance
		Observation observation = new Observation();

		observation.setSubject(new Reference(patient));
		observation.setStatus(ObservationStatus.FINAL);
		Narrative text = new Narrative();
		observation.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
		observation.getText().setDivAsString("<div>Vital sign Observation<br/> of patient : "+ patient.getNameFirstRep().getFamily() + "</div>");

		
		// creation of Body Weight vital sign observation resource
		if (code.equals("bodyweight")) {
			// Give the observation a code (what kind of observation is this)
			Coding coding = observation.getCode().addCoding();

			System.out.println("creating a bodyweight resource");
			coding.setCode("29463-7").setSystem("http://loinc.org").setDisplay("Body Weight");

			// Create a default quantity
			Quantity value = new Quantity();
			value.setValue(bodymeasure).setSystem("http://unitsofmeasure.org").setCode("kg");
			observation.setValue(value);

			// Set the reference range
			SimpleQuantity low = new SimpleQuantity();
			low.setValue(35).setSystem("http://unitsofmeasure.org").setCode("kg");
			observation.getReferenceRangeFirstRep().setLow(low);

			SimpleQuantity high = new SimpleQuantity();
			high.setValue(90).setSystem("http://unitsofmeasure.org").setCode("kg");
			observation.getReferenceRangeFirstRep().setHigh(high);
		}
		// creation of Body Height vital sign observation resource
		if (code.equals("bodyheight")) {
			// Give the observation a code (what kind of observation is this)
			Coding coding = observation.getCode().addCoding();

			System.out.println("creating a bodyheight resource");
			coding.setCode("8302-2").setSystem("http://loinc.org").setDisplay("Body Height");

			// Create a default quantity
			Quantity value = new Quantity();
			value.setValue(bodymeasure).setSystem("http://unitsofmeasure.org").setCode("cm");
			observation.setValue(value);

			// Set the reference range
			SimpleQuantity low = new SimpleQuantity();
			low.setValue(145).setSystem("http://unitsofmeasure.org").setCode("cm");
			observation.getReferenceRangeFirstRep().setLow(low);

			SimpleQuantity high = new SimpleQuantity();
			high.setValue(190).setSystem("http://unitsofmeasure.org").setCode("cm");
			observation.getReferenceRangeFirstRep().setHigh(high);
		}
		// creation of Heart Rate vital sign observation resource
		if (code.equals("heartrate")) {
			// Give the observation a code (what kind of observation is this)
			Coding coding = observation.getCode().addCoding();

			System.out.println("creating a heart rate resource");
			coding.setCode("8867-4").setSystem("http://loinc.org").setDisplay("Heart rate");

			// Create a default quantity
			Quantity value = new Quantity();
			value.setValue(bodymeasure).setSystem("http://unitsofmeasure.org").setCode("/min");
			observation.setValue(value);
		}

		// put on the server
		MethodOutcome outcome = client.create().resource(observation).prettyPrint().encodedJson().execute();

		IdType id = (IdType) outcome.getId();
		System.out.println("Resource is available at: " + id.getValue());
		System.out.println("VitalSign resource Id = " + outcome.getId().getIdPart());
		return outcome.getId().getIdPart();
	}

	/*
	 * retrieve an Observation resource based on its id
	 */
	public Observation getPatientVitalsbyID(String observationID) {
		Observation Ovital = new Observation();
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		try {
			Bundle response = client.search().forResource(Observation.class)
					.where(new TokenClientParam("_id").exactly().code(observationID)).prettyPrint()
					.returnBundle(Bundle.class).execute();

			Ovital = (Observation) response.getEntry().get(0).getResource();
			System.out.println("Obodyvital retrieve by id in class and is at " + Ovital.getId());

		} catch (Exception e) {
			System.out.println("An error occurred trying to search:");
			e.printStackTrace();
		}
		System.out.println("method getPatientvitalsbyID returns reached");

		return Ovital;
	}

	/*
	 * Get the patient vital resource, if it has one set the bean property
	 * object with the resource gotten from server
	 */
	public Observation getPatientVital(Patient patient, String code) {
		String rcode = "";
		switch (code) {
		case "bodyweight":
			rcode = "29463-7";
			break;

		case "bodyheight":
			rcode = "8302-2";
			break;

		case "heartrate":
			rcode = "8867-4";
			break;
		}

		System.out.println("----searching for vital obs with code : " + rcode);
		Observation Ovital = new Observation();

		this.FhirCo();

		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		System.out.println("searching for existing vital obs resource on server with patient id : "
				+ patient.getIdElement().getIdPart());
		String searchUrl = "Observation?subject=" + patient.getIdElement().getIdPart() + "&code=" + rcode
				+ "&_pretty=true";
		System.out.println("search URL at the moment is: " + searchUrl);

		try {
			Bundle response = client.search()
					// .forResource(Observation.class)
					// .where(new
					// TokenClientParam("code").exactly().code(rcode))
					// .where(new
					// ReferenceClientParam("subject").hasId(patient.getIdElement().getIdPart()))
					// .where(new
					// StringClientParam("patient").matches().value(patient.getIdElement().getIdPart()))
					// .prettyPrint()
					.byUrl(searchUrl).returnBundle(Bundle.class).execute();

			// if we get a resource from the server, we set the Obodyweight
			// object with it
			System.out.println("size bundle of observation for patient is : " + response.getEntry().size());
			// if (response.getEntry().isEmpty() == false) {
			if (response.getEntry().size() != 0) {
				Ovital = (Observation) response.getEntry().get(0).getResource();
				System.out.println("Obs set form server resource");
			}

		} catch (DataFormatException e) {
			System.out.println("An error occurred trying to get observation:");
			e.printStackTrace();
		}
		return Ovital;

	}

	/*
	 * When saving the input vital sign value we update the observation resource
	 * of the patient with the entered value
	 */
	public void updateVitalResource(double bodymeasure, Observation Ovital, String code) {

		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		System.out.println("when saving bodymeasure variable is at " + bodymeasure);

		// Create a quantity data type
		Quantity value = new Quantity();

		switch (code) {
		case "bodyheight":
			value.setValue(bodymeasure).setSystem("http://unitsofmeasure.org").setCode("cm");
			break;

		case "bodyweight":
			value.setValue(bodymeasure).setSystem("http://unitsofmeasure.org").setCode("kg");
			break;

		case "heartrate":
			value.setValue(bodymeasure).setSystem("http://unitsofmeasure.org").setCode("/min");
			break;
		}

		Ovital.setValue(value);

		System.out.println("when Updating Ovital ID is: " + Ovital.getId());

		try {
			MethodOutcome outcome = client.update().resource(Ovital).execute();

			IdType id = (IdType) outcome.getId();
			System.out.println("UPDATED Resource is available at: " + id.getValue());

		} catch (DataFormatException e) {
			System.out.println("An error occurred trying to update observation:" + code);
			e.printStackTrace();
		}
	}
	
	public void deleteobs(String id) {
		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		
		Observation Ovital = new Observation();

		// Invoke the client
		Bundle bundle = client.search().forResource(Observation.class)
				.where(new ReferenceClientParam("subject").hasId(id)).prettyPrint().returnBundle(Bundle.class)
				.execute();

		System.out.println("size bundle of observation TO DELETE for patient is : " + bundle.getEntry().size());

		bundle.getEntry().forEach((entry) -> {
			// populate the list with the retrieved bundle's resources
			Observation todelete = (Observation) entry.getResource();
			System.out.println("deleting obs resource for patient with obs id : " + todelete.getIdElement().getIdPart());
			client.delete().resourceById(new IdDt("Observation", todelete.getIdElement().getIdPart() )).execute();
			

		});

	}

}
