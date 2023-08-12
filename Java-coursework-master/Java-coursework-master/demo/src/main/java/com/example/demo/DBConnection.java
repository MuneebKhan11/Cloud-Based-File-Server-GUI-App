package com.example.demo;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Paths;
import java.io.File;
import java.util.Arrays;


public class DBConnection {
    public static String currentUser;

    public static void setCurrentUser(String newUsername) {
        currentUser = newUsername;
    }

    public static String getCurrentUser() {
        currentUser = currentUser;
        return currentUser;
    }

    private static final String RELATIVE_DB_PATH = "Database.db";
    static final String DB_URL = "jdbc:sqlite:" + new File(RELATIVE_DB_PATH).getAbsolutePath();

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL);
        System.out.println("Connection to the database established successfully.");
        return connection;
    }
    public static boolean isValidPassword(String password) {
        // Password must be at least 8 characters long
        if (password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasSpecialChar = false;
        boolean hasNumeric = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isDigit(c)) {
                hasNumeric = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        // Password must have at least one uppercase letter, one special character, and one numeric value
        return hasUppercase && hasSpecialChar && hasNumeric;
    }
    public static void updateUser(String username, String newPassword) throws SQLException, NoSuchAlgorithmException {
        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain at least one uppercase letter, one numerical value, and one special character.");
        }
        String encryptedPassword = encryptPassword(newPassword);
        String query = "UPDATE User SET Password = ? WHERE Username = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, encryptedPassword); // Set the new encrypted password
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
        }
    }

    public static boolean verifyUser(String username, String password) throws SQLException, NoSuchAlgorithmException {
        String encryptedPassword = encryptPassword(password);
        String query = "SELECT * FROM User WHERE Username = ? AND Password = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, encryptedPassword);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public static void updateLoginTime(String username) throws SQLException {
        String query = "UPDATE User SET LoginTime = ? WHERE Username = ?";

        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();
        // Format the date and time as a string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String loginTime = now.format(formatter);

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, loginTime); // Set the new login time
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
        }
    }

    public static void createUser(String username, String password, String email) throws SQLException, NoSuchAlgorithmException {
        if (password.length() < 8 || !password.matches(".*\\d.*") || !password.matches(".*[A-Z].*") || !password.matches(".*[^a-zA-Z0-9].*")) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain at least one upper case letter, one numerical value, and one special character.");
        }
        String encryptedPassword = encryptPassword(password);
        String query = "INSERT INTO User (Username, Password, Email, RegDate, LoginTime) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            LocalDateTime now = LocalDateTime.now();

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, encryptedPassword);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, now.toString());
            preparedStatement.setString(5, now.toString());

            preparedStatement.executeUpdate();
        }
    }
    public static void encryptFile(String inputFilePath, String outputFilePath, String encryptedPassword) throws Exception {
        // Derive the key and IV from the encrypted password
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] keyAndIv = md.digest(encryptedPassword.getBytes());
        byte[] keyBytes = Arrays.copyOfRange(keyAndIv, 0, 16);
        byte[] ivBytes = Arrays.copyOfRange(keyAndIv, 16, 32);

        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        // Initialize the cipher for encryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        // Read the input file and encrypt its content
        byte[] inputBytes = Files.readAllBytes(Paths.get(inputFilePath));
        byte[] encryptedBytes = cipher.doFinal(inputBytes);

        // Write the encrypted content to the output file
        Files.write(Paths.get(outputFilePath), encryptedBytes);
    }

    public static int getUserId(String username) throws SQLException {
        String query = "SELECT UserID FROM User WHERE Username = ?";
        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("UserID");
                } else {
                    return -1;
                }
            }
        }
    }
    public static String getEncryptedPassword(String username) throws SQLException {
        String query = "SELECT Password FROM User WHERE Username = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("Password");
                }
            }
        }
        return null;
    }

    public static String encryptPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hash = messageDigest.digest(password.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}