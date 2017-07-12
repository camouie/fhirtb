package fhirtb;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

public class loginbean implements Serializable {

	private static final long serialVersionUID = 1094801825228386363L;

	private String pwd;
	private String msg;
	private String user;
	private String role;
	private String fhirid;
	private String doctorfhirid;

	private boolean admin;
	private boolean patient;
	private boolean logged;

	// validate login
	public String validateUsernamePassword() throws ClassNotFoundException {
		boolean valid = DAO.validate(user, pwd);
		if (valid) {
			HttpSession session = SessionUtils.getSession();
			session.setAttribute("username", user);

			this.role = DAO.getUserRole(user);
			session.setAttribute("role", role);
			System.out.println("||| session role set to: " + role);

			// tell the bean the user is now logged
			this.logged = true;
			session.setAttribute("logged", this.logged);

			if (this.role.equals("admin"))
				this.setAdmin(true);

			if (this.role.equals("patient")){
				this.setPatient(true);
				this.doctorfhirid = DAO.getDoctorFhirid(user);
				session.setAttribute("doctorid", this.doctorfhirid);
			}

			// fhirid equals to none for admin, otherwise patient and doctors
			// have one.
			this.fhirid = DAO.getFhirid(user);
			session.setAttribute("fhirid", this.fhirid);

			viewNavigation vn = new viewNavigation();
			// if the user is a patient, he has a different "index" page called
			// welcome without list of all patients
			return vn.goHome();

		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Incorrect Username and Passowrd", "Please enter correct username and Password"));
			return "login";
		}
	}

	// logout event, invalidate session
	public String logout() {
		HttpSession session = SessionUtils.getSession();
		session.invalidate();
		return "/login?faces-redirect=true";
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isLogged() {
		return logged;
	}

	public void setLogged(boolean logged) {
		this.logged = logged;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getFhirid() {
		return fhirid;
	}

	public void setFhirid(String fhirid) {
		this.fhirid = fhirid;
	}

	public boolean isPatient() {
		return patient;
	}

	public void setPatient(boolean patient) {
		this.patient = patient;
	}

	public String getDoctorfhirid() {
		return doctorfhirid;
	}

	public void setDoctorfhirid(String doctorfhirid) {
		this.doctorfhirid = doctorfhirid;
	}
	

}