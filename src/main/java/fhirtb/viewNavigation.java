package fhirtb;

public class viewNavigation {

	public String goHome() {
		if (SessionUtils.getSession().getAttribute("role").equals("patient")) {
			return "/secured/welcomePatient?faces-redirect=true";
		} else {
			return "/secured/index?faces-redirect=true";
		}

	}

}
