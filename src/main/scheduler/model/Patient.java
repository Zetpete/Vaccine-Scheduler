package scheduler.model;

import scheduler.db.ConnectionManager;
import scheduler.util.Util;

import java.sql.*;
import java.util.Arrays;

public class Patient {
    private final String username;
    private final byte[] salt;
    private final byte[] hash;

    private Patient(PatientBuilder builder) {
        this.username = builder.username;
        this.salt = builder.salt;
        this.hash = builder.hash;
    }

    // Getters
    public String getUsername() { return username; }
    public byte[] getSalt() { return salt; }
    public byte[] getHash() { return hash; }

    public void saveToDB() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String sql = "INSERT INTO Patients(Username, Salt, Hash) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, this.username);
            ps.setBytes(2, this.salt);
            ps.setBytes(3, this.hash);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    // Builder pattern
    public static class PatientBuilder {
        private final String username;
        private final byte[] salt;
        private final byte[] hash;

        public PatientBuilder(String username, byte[] salt, byte[] hash) {
            this.username = username;
            this.salt = salt;
            this.hash = hash;
        }

        public Patient build() {
            return new Patient(this);
        }
    }

    // Getter class for login
    public static class PatientGetter {
        private final String username;
        private final String password;
        private byte[] salt;
        private byte[] hash;

        public PatientGetter(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public Patient get() throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String sql = "SELECT Salt, Hash FROM Patients WHERE Username = ?";
            try {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, this.username);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    byte[] salt = rs.getBytes("Salt");
                    byte[] hash = Util.trim(rs.getBytes("Hash"));
                    byte[] calculatedHash = Util.generateHash(password, salt);

                    if (!Arrays.equals(hash, calculatedHash)) return null;

                    this.salt = salt;
                    this.hash = hash;
                    return new Patient(new PatientBuilder(username, salt, hash));
                }
                return null;
            } finally {
                cm.closeConnection();
            }
        }
    }
}