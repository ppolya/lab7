package db;

import mainlib.Reader;
import mainlib.User;
import server.Server;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class UserUtil implements Util {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALG = "SHA-512";
    private String userSalt;
    private String userPepper;


    @Override
    public boolean register(User user) {
        String statement = "INSERT INTO users (username,password,salt) VALUES(?,?,?)";
        if (!checkUser(user)) {
            try {
                PreparedStatement preparedStatement = Server.getDatabase().connection.prepareStatement(statement);
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, getStringHash(generateHash(user)));
                preparedStatement.setString(3, userSalt);
                if (preparedStatement.executeUpdate() != 0) {
                    return true;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean authorize(User user) {
        return checkUser(user);
    }


    private String getUserHash(User user) {
        String sql = "SELECT * FROM users";
        String password = null;
        Statement st;
        ResultSet resultSet;
        try {
            st = Server.getDatabase().connection.createStatement();
            resultSet = st.executeQuery(sql);
            while (resultSet.next()) {
                password = resultSet.getString("password");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return password;
    }

    private boolean checkUser(User user) {
        String statement = "SELECT * FROM users WHERE username=?";
        ResultSet resultSet = null;
        int count = 0;
        try {
            PreparedStatement preparedStatement = Server.getDatabase().connection.prepareStatement(statement);
            preparedStatement.setString(1, user.getUsername());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                count++;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return count > 0;
    }

    public boolean checkUserName(String user) {
        String statement = "SELECT * FROM users WHERE username=?";
        ResultSet resultSet = null;
        int count = 0;
        try {
            PreparedStatement preparedStatement = Server.getDatabase().connection.prepareStatement(statement);
            preparedStatement.setString(1, user);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                count++;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return count > 0;
    }



    private byte[] generateHash(User user) {
        MessageDigest md = null;
        byte[] hash = null;
        try {
            md = MessageDigest.getInstance(ALG);
            String password = user.getPassword();
            userSalt = getStringSalt(generateSalt());
            userPepper = generatePepper();
            hash = md.digest((userPepper + password + userSalt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            Reader.PrintErr("no such encryption algorithm: " + ALG);
        }
        return hash;
    }

    private byte[] generateSalt(){
        byte[] salt = new byte[6];
        RANDOM.nextBytes(salt);
        return salt;
    }

    private String generatePepper() {
        return UUID.randomUUID().toString();
    }


    private String getStringSalt(byte[] salt) {
        return DatatypeConverter.printHexBinary(salt);
    }

    private String getStringHash(byte[] hash) {
        return DatatypeConverter.printHexBinary(hash);
    }

}
