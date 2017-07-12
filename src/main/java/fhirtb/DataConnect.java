package fhirtb;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.faces.context.FacesContext;

public class DataConnect implements Serializable {
	private static final long serialVersionUID = 9204275723046653468L;

	private String db_server = "";
	private String db_user = "";
	private String db_password = "";
	private String db_driver = "";

	public DataConnect() throws ClassNotFoundException {
		FacesContext fc = FacesContext.getCurrentInstance();
		db_server = fc.getExternalContext().getInitParameter("DB-SERVER");
		db_user = fc.getExternalContext().getInitParameter("DB-USER");
		db_password = fc.getExternalContext().getInitParameter("DB-PASSWORD");
		db_driver = fc.getExternalContext().getInitParameter("JDBC-DRIVER");
		Class.forName(db_driver);
	}

	public Connection getConnection() {
		try {
			Connection con = DriverManager.getConnection(db_server, db_user, db_password);
			return con;
		} catch (Exception ex) {
			System.out.println("Database.getConnection() Error -->" + ex.getMessage());
			return null;
		}
	}

	public static void close(Connection con) {
		try {
			con.close();
		} catch (Exception ex) {
		}
	}
}
