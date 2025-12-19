package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Create patient failed");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        if (!isStrongPassword(password)) {
            System.out.println("Create patient failed, please use a strong password (8+ char, at least one upper and one lower, at least one letter and one number, and at least one special character, from \"!\", \"@\", \"#\", \"?\")");
            return;
        }

        if(usernameExistsPatient(username)) {
            System.out.println("Username taken, try again");
            return;
        }

        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);

        try {
            Patient patient = new Patient.PatientBuilder(username, salt, hash).build();
            patient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Create patient failed");
        }
    }

    private static boolean isStrongPassword(String password) {
        if (password.length() < 8) return false;

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if ("!@#?".indexOf(c) >= 0) {
                hasSpecial = true;
            }
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String selectUsername = "SELECT * FROM Patients WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            return true;
        } finally {
            cm.closeConnection();
        }
    }


    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Create patient failed");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        if (!isStrongPassword(password)) {
            System.out.println(
                    "Create caregiver failed, please use a strong password (8+ char, at least one upper and one lower, at least one letter and one number, and at least one special character, from \"!\", \"@\", \"#\", \"?\")"
            );
            return;
        }

        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            Caregiver caregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            caregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Create patient failed");
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();  // true if username exists
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            return true; // prevent duplicate creation if there is an error
        } finally {
            cm.closeConnection();
        }
    }

    private static void loginPatient(String[] tokens) {
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in, try again");
            return;
        }

        if (tokens.length != 3) {
            System.out.println("Login patient failed");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login patient failed");
        }

        if (patient == null) {
            System.out.println("Login patient failed");
        } else {
            System.out.println("Logged in as " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in, try again");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login caregiver failed");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login caregiver failed");
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login caregiver failed");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first");
            return;
        }

        if (tokens.length != 2) {
            System.out.println("Please try again");
            return;
        }

        String dateStr = tokens[1];

        try {
            Date date = Date.valueOf(dateStr);
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            // Get available caregivers for the date
            String caregiverQuery = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username ASC";
            PreparedStatement caregiverStmt = con.prepareStatement(caregiverQuery);
            caregiverStmt.setDate(1, date);
            ResultSet caregiverRs = caregiverStmt.executeQuery();

            System.out.println("Caregivers:");
            boolean caregiversExist = false;
            while (caregiverRs.next()) {
                caregiversExist = true;
                System.out.println(caregiverRs.getString("Username"));
            }
            if (!caregiversExist) System.out.println("No caregivers available");

            // Get available vaccines
            String vaccineQuery = "SELECT Name, Doses FROM Vaccines";
            PreparedStatement vaccineStmt = con.prepareStatement(vaccineQuery);
            ResultSet vaccineRs = vaccineStmt.executeQuery();

            System.out.println("Vaccines:");
            boolean vaccinesExist = false;
            while (vaccineRs.next()) {
                int doses = vaccineRs.getInt("Doses");
                if (doses > 0) {
                    vaccinesExist = true;
                    System.out.println(vaccineRs.getString("Name") + " " + doses);
                }
            }
            if (!vaccinesExist) System.out.println("No vaccines available");

            cm.closeConnection();
        } catch (IllegalArgumentException e) {
            System.out.println("Please try again");
        } catch (SQLException e) {
            System.out.println("Please try again");
        }
    }

    private static void reserve(String[] tokens) {
        if (currentPatient == null) {
            if (currentCaregiver == null) {
                System.out.println("Please login first");
            } else {
                System.out.println("Please login as a patient");
            }
            return;
        }

        if (tokens.length != 3) {
            System.out.println("Please try again");
            return;
        }

        String dateStr = tokens[1];
        String vaccineName = tokens[2];

        try {
            Date date = Date.valueOf(dateStr);
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            // 1. Check for available caregivers
            String caregiverQuery = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username ASC LIMIT 1";
            PreparedStatement caregiverStmt = con.prepareStatement(caregiverQuery);
            caregiverStmt.setDate(1, date);
            ResultSet caregiverRs = caregiverStmt.executeQuery();

            if (!caregiverRs.next()) {
                System.out.println("No caregiver is available");
                cm.closeConnection();
                return;
            }
            String caregiverUsername = caregiverRs.getString("Username");

            // 2. Check vaccine availability
            String vaccineQuery = "SELECT Doses FROM Vaccines WHERE Name = ?";
            PreparedStatement vaccineStmt = con.prepareStatement(vaccineQuery);
            vaccineStmt.setString(1, vaccineName);
            ResultSet vaccineRs = vaccineStmt.executeQuery();

            if (!vaccineRs.next() || vaccineRs.getInt("Doses") <= 0) {
                System.out.println("Not enough available doses");
                cm.closeConnection();
                return;
            }

            // 3. Get next appointment ID
            String idQuery = "SELECT MAX(AppointmentID) as maxId FROM Appointments";
            PreparedStatement idStmt = con.prepareStatement(idQuery);
            ResultSet idRs = idStmt.executeQuery();
            int appointmentId = 1;
            if (idRs.next() && idRs.getInt("maxId") > 0) {
                appointmentId = idRs.getInt("maxId") + 1;
            }

            // 4. Insert appointment
            String insertQuery = "INSERT INTO Appointments(AppointmentID, PatientName, CaregiverName, VaccineName, Date, Time) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = con.prepareStatement(insertQuery);
            insertStmt.setInt(1, appointmentId);
            insertStmt.setString(2, currentPatient.getUsername());
            insertStmt.setString(3, caregiverUsername);
            insertStmt.setString(4, vaccineName);
            insertStmt.setDate(5, date);
            insertStmt.setString(6, "09:00"); // time can be fixed
            insertStmt.executeUpdate();

            // 5. Decrease vaccine dose
            String updateVaccine = "UPDATE Vaccines SET Doses = Doses - 1 WHERE Name = ?";
            PreparedStatement updateStmt = con.prepareStatement(updateVaccine);
            updateStmt.setString(1, vaccineName);
            updateStmt.executeUpdate();

            // 6. Remove caregiver availability
            String deleteAvailability = "DELETE FROM Availabilities WHERE Time = ? AND Username = ?";
            PreparedStatement deleteStmt = con.prepareStatement(deleteAvailability);
            deleteStmt.setDate(1, date);
            deleteStmt.setString(2, caregiverUsername);
            deleteStmt.executeUpdate();

            System.out.println("Appointment ID " + appointmentId + ", Caregiver username " + caregiverUsername);

            cm.closeConnection();
        } catch (IllegalArgumentException e) {
            System.out.println("Please try again");
        } catch (SQLException e) {
            System.out.println("Please try again");
        }
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
        }
    }

    private static void cancel(String[] tokens) {
        if (currentPatient == null && currentCaregiver == null) {
            System.out.println("Please login first");
            return;
        }

        if (tokens.length != 2) {
            System.out.println("Please try again");
            return;
        }

        int appointmentId;
        try {
            appointmentId = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            System.out.println("Please try again");
            return;
        }

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        try {
            AppointmentInfo info = getAppointmentInfo(con, appointmentId);

            if (info == null) {
                System.out.println("Appointment ID " + appointmentId + " does not exist");
                return;
            }

            if (currentPatient != null &&
                    !info.patient.equals(currentPatient.getUsername())) {
                System.out.println("Appointment ID " + appointmentId + " does not exist");
                return;
            }
            if (currentCaregiver != null &&
                    !info.caregiver.equals(currentCaregiver.getUsername())) {
                System.out.println("Appointment ID " + appointmentId + " does not exist");
                return;
            }

            PreparedStatement deleteStmt =
                    con.prepareStatement("DELETE FROM Appointments WHERE AppointmentID = ?");
            deleteStmt.setInt(1, appointmentId);
            deleteStmt.executeUpdate();

            PreparedStatement doseStmt =
                    con.prepareStatement("UPDATE Vaccines SET Doses = Doses + 1 WHERE Name = ?");
            doseStmt.setString(1, info.vaccine);
            doseStmt.executeUpdate();

            PreparedStatement availStmt =
                    con.prepareStatement("INSERT INTO Availabilities (Username, Time) VALUES (?, ?)");
            availStmt.setString(1, info.caregiver);
            availStmt.setDate(2, info.date);
            availStmt.executeUpdate();

            System.out.println("Appointment ID " + appointmentId + " has been successfully canceled");

        } catch (SQLException e) {
            System.out.println("Please try again");
        } finally {
            cm.closeConnection();
        }
    }

    private static class AppointmentInfo {
        int id;
        String vaccine;
        java.sql.Date date;
        String caregiver;
        String patient;

        AppointmentInfo(int id, String vaccine, java.sql.Date date,
                        String caregiver, String patient) {
            this.id = id;
            this.vaccine = vaccine;
            this.date = date;
            this.caregiver = caregiver;
            this.patient = patient;
        }
    }

    private static AppointmentInfo getAppointmentInfo(Connection con, int id) throws SQLException {
        String sql = "SELECT AppointmentID, Vaccine, Date, Caregiver, Patient " +
                "FROM Appointments WHERE AppointmentID = ?";

        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (!rs.next()) {
            return null;
        }

        return new AppointmentInfo(
                rs.getInt("AppointmentID"),
                rs.getString("Vaccine"),
                rs.getDate("Date"),
                rs.getString("Caregiver"),
                rs.getString("Patient")
        );
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        if (tokens.length != 1) {
            System.out.println("Please try again");
            return;
        }

        if (currentPatient == null && currentCaregiver == null) {
            System.out.println("Please login first");
            return;
        }

        try {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String query;
            String username;
            boolean isCaregiver = false;

            if (currentCaregiver != null) {
                username = currentCaregiver.getUsername();
                query = "SELECT AppointmentID, VaccineName, Date, PatientName FROM Appointments WHERE CaregiverName = ? ORDER BY AppointmentID ASC";
                isCaregiver = true;
            } else {
                username = currentPatient.getUsername();
                query = "SELECT AppointmentID, VaccineName, Date, CaregiverName FROM Appointments WHERE PatientName = ? ORDER BY AppointmentID ASC";
            }

            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            boolean exists = false;
            while (rs.next()) {
                exists = true;
                int id = rs.getInt("AppointmentID");
                String vaccine = rs.getString("VaccineName");
                Date date = rs.getDate("Date");
                String other = isCaregiver ? rs.getString("PatientName") : rs.getString("CaregiverName");
                System.out.println(id + " " + vaccine + " " + date + " " + other);
            }

            if (!exists) System.out.println("No appointments scheduled");

            cm.closeConnection();
        } catch (SQLException e) {
            System.out.println("Please try again");
        }
    }

    private static void logout(String[] tokens) {
        if (tokens.length != 1) {
            System.out.println("Please try again");
            return;
        }

        if (currentPatient == null && currentCaregiver == null) {
            System.out.println("Please login first");
            return;
        }
        currentPatient = null;
        currentCaregiver = null;
        System.out.println("Successfully logged out");
    }
}