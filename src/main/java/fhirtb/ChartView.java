package fhirtb;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

import java.io.Serializable;
import java.util.ArrayList;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
 
public class ChartView implements Serializable {
 
	private static final long serialVersionUID = 1L;
	
	private LineChartModel lineModel1;

	private Fhircontextconnection fco;

	private String serverBaseUrl;

	private FhirContext ctx;
	
	private Patient patient;
	
	private ArrayList<Double> weigths;
	
	private ArrayList<Observation> observations;
	
	private String patientid;
	
	private Observation Obodyweight;
	
	private HttpSession session;
	
    @PostConstruct
    public void init() {
    	this.FhirCo();
    	//get the user patient fhirid
    	this.session = SessionUtils.getSession();
		this.patientid = (String) this.session.getAttribute("fhirid");
		System.out.println(">>>>>>>>> patient fhir id : " + this.patientid);
		//initialization
    	this.weigths = new ArrayList<Double>();
    	this.observations = new ArrayList<Observation>();
    	VitalSignsHandler vh = new VitalSignsHandler();
    	this.Obodyweight = new Observation();
    	
    	//get the latest patient resource of the user
    	this.getPatientbyID();
    	
    	this.Obodyweight = vh.getPatientVital(patient, "bodyweight");
    			
    	//get all the version of the patient resource available
    	this.getallversionBodyweight();
    	
    	this.generateArrayWeight();
    	
        createLineModels();
    }
    
    public void FhirCo() {
		this.fco = new Fhircontextconnection();
		this.serverBaseUrl = fco.getServerBaseUrl();
		this.ctx = fco.getCtx();
	}
 
    public LineChartModel getLineModel1() {
        return lineModel1;
    }
     
    private void createLineModels() {
    	//test arraylist
    	/*ArrayList<Integer> w = new ArrayList<>();
    	w.add(40);
    	w.add(60);
    	w.add(65);
    	*/
    	
        lineModel1 = initLinearModel(this.weigths);
        lineModel1.setTitle("Your Weight Evolution");
        lineModel1.setLegendPosition("e");
        //Weight axis
        Axis yAxis = lineModel1.getAxis(AxisType.Y);
        yAxis.setMin(0);
        yAxis.setMax(200);
        
        Axis xAxis = lineModel1.getAxis(AxisType.X);
        xAxis.setMin(0);
        xAxis.setMax(this.weigths.size()-1);
        xAxis.setTickCount(this.weigths.size());
        xAxis.setTickFormat("%d");
         
    }
     
    private LineChartModel initLinearModel(ArrayList<Double> weights) {
        LineChartModel model = new LineChartModel();
 
        LineChartSeries series1 = new LineChartSeries();
        series1.setLabel("Weight");
        
        for(int i =0; i<weights.size(); i++){
        	 series1.set(i, weights.get(i));
        }
        model.addSeries(series1);
         
        return model;
    }
    
    public void getallversionBodyweight(){
    	Patient p = this.patient;
    	Observation bw = this.Obodyweight;
    	
    	String v = bw.getMeta().getVersionId();
    	int version = Integer.parseInt(v);
    	System.out.println("<<<<<<<<<<<<< VERSION OF RESOURCE = " + version);

    	IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
		ArrayList <Observation> obs = new ArrayList<Observation>();
		
    	for(int i = version; i>0; i--){
    		String currentversion = String.valueOf(i);
    			
    		Observation ob = client.read()
                    .resource(Observation.class)
                    .withIdAndVersion(bw.getIdElement().getIdPart(), currentversion)
                    .execute();
    			
    			obs.add(ob);
    			System.out.println("\\\\\\\\\\\\\\ found id version : "+ob.getMeta().getVersionId());
    	}
    	
    	this.observations = obs;
    	System.out.println("-- set obs list with all versions of obs resource from server");
    	
    }
    
    private void getPatientbyID() {

		IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);

		try {
			Bundle response = client.search().forResource(Patient.class)
					.where(new TokenClientParam("_id").exactly().code(this.patientid)).prettyPrint()
					.returnBundle(Bundle.class).execute();

			this.setPatient((Patient) response.getEntry().get(0).getResource());

		} catch (Exception e) {
			System.out.println("An error occurred trying to search for patient by id:");
			e.printStackTrace();
		}
	}
    
    public void generateArrayWeight(){
    	for(int i = this.observations.size()-1; i>=0; i--){
    		Observation ob = observations.get(i);
    		if (ob.hasValueQuantity()) {
    			try {
					Double bodyWeight = ob.getValueQuantity().getValueElement().getValueAsNumber().doubleValue();
					this.weigths.add(bodyWeight);
					
				} catch (FHIRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    }
    
    /*
     * getter and setters
     */
    

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public ArrayList<Double> getWeigths() {
		return weigths;
	}

	public void setWeigths(ArrayList<Double> weigths) {
		this.weigths = weigths;
	}

	public String getPatientid() {
		return patientid;
	}

	public void setPatientid(String patientid) {
		this.patientid = patientid;
	}

	public ArrayList<Observation> getObservations() {
		return observations;
	}

	public void setObservations(ArrayList<Observation> observations) {
		this.observations = observations;
	}

	public Observation getObodyweight() {
		return Obodyweight;
	}

	public void setObodyweight(Observation obodyweight) {
		Obodyweight = obodyweight;
	} 
 
}