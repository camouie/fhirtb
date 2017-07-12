package fhirtb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DAO {

	public static boolean validate(String user, String password) throws ClassNotFoundException {
		Connection con = null;
		PreparedStatement ps = null;

		try {
			DataConnect daco = new DataConnect();
			con = daco.getConnection();
			ps = con.prepareStatement("Select username, password from Users where username = ? and password = ?");
			ps.setString(1, user);
			ps.setString(2, password);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				// result found, means valid inputs
				return true;
			}
		} catch (SQLException ex) {
			System.out.println("DB Login error -->" + ex.getMessage());
			return false;
		} finally {
			DataConnect.close(con);
		}
		return false;
	}

	public static void addPractitionerAccount(String uname, String pwd, String fhirid) throws ClassNotFoundException {
		Connection con = null;
		PreparedStatement ps = null;

		try {
			DataConnect daco = new DataConnect();
			con = daco.getConnection();
			ps = con.prepareStatement("insert into Users VALUES(?, ?, ?, ?, ?)");

			ps.setString(1, uname);
			ps.setString(2, pwd);
			ps.setString(3, fhirid);
			ps.setString(4, "doctor");
			ps.setString(5, null);

			ps.execute();

		} catch (SQLException ex) {
			System.out.println("DB Insert doctor error -->" + ex.getMessage());
		} finally {
			DataConnect.close(con);
		}

	}

	public static ArrayList<String> getPractitioners() throws ClassNotFoundException {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList<String> doctorsids = new ArrayList<String>();

		try {
			DataConnect daco = new DataConnect();
			con = daco.getConnection();
			ps = con.prepareStatement("Select fhirid from Users where type = ?");
			ps.setString(1, "doctor");

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				doctorsids.add(rs.getString(1));
			}
			return doctorsids;

		} catch (SQLException ex) {
			System.out.println("DB getting practitioner error -->" + ex.getMessage());
			return doctorsids;
		} finally {
			DataConnect.close(con);
		}
	}
	
	public static ArrayList<String> getPatientsforPractitioner(String doctorid) throws ClassNotFoundException {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList<String> patientsids = new ArrayList<String>();

		try {
			DataConnect daco = new DataConnect();
			con = daco.getConnection();
			ps = con.prepareStatement("Select fhirid from Users where doctorid = ?");
			ps.setString(1, doctorid);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				patientsids.add(rs.getString(1));
			}
			return patientsids;

		} catch (SQLException ex) {
			System.out.println("DB getting patients for practitioner error -->" + ex.getMessage());
			return patientsids;
		} finally {
			DataConnect.close(con);
		}
	}

	public static String getUserRole(String username) throws ClassNotFoundException {
		Connection con = null;
		PreparedStatement ps = null;
		String role = "none";

		try {
			DataConnect daco = new DataConnect();
			con = daco.getConnection();
			ps = con.prepareStatement("Select type from Users where username = ?");
			ps.setString(1, username);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				role = rs.getString(1);
			}

			return role;

		} catch (SQLException ex) {
			System.out.println("DB getting user role error -->" + ex.getMessage());
			return role;
		} finally {
			DataConnect.close(con);
		}

	}

	public static void addPatientAccount(String uname, String pwd, String fhirid, String doctorid)
			throws ClassNotFoundException {
		Connection con = null;
		PreparedStatement ps = null;

		try {
			DataConnect daco = new DataConnect();
			con = daco.getConnection();
			ps = con.prepareStatement("insert into Users VALUES(?, ?, ?, ?, ?)");

			ps.setString(1, uname);
			ps.setString(2, pwd);
			ps.setString(3, fhirid);
			ps.setString(4, "patient");
			ps.setString(5, doctorid);

			ps.execute();

		} catch (SQLException ex) {
			System.out.println("DB Insert patient error -->" + ex.getMessage());
		} finally {
			DataConnect.close(con);
		}

	}

	public static String getDoctorFhirid(String username) throws ClassNotFoundException {
		Connection con = null;
		PreparedStatement ps = null;
		String doctorfhirid = "none";

		try {
			DataConnect daco = new DataConnect();
			con = daco.getConnection();
			ps = con.prepareStatement("Select doctorid from Users where username = ?");
			ps.setString(1, username);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				doctorfhirid = rs.getString(1);
			}

			return doctorfhirid;

		} catch (SQLException ex) {
			System.out.println("DB searching for fhirid error -->" + ex.getMessage());
			return doctorfhirid;
		} finally {
			DataConnect.close(con);
		}

	}
	
	public static String getFhirid(String username) throws ClassNotFoundException {
		Connection con = null;
		PreparedStatement ps = null;
		String fhirid = "none";

		try {
			DataConnect daco = new DataConnect();
			con = daco.getConnection();
			ps = con.prepareStatement("Select fhirid from Users where username = ?");
			ps.setString(1, username);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				fhirid = rs.getString(1);
			}

			return fhirid;

		} catch (SQLException ex) {
			System.out.println("DB searching for fhirid error -->" + ex.getMessage());
			return fhirid;
		} finally {
			DataConnect.close(con);
		}

	}

}
