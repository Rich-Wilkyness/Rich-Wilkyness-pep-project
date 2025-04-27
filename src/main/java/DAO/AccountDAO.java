package DAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import Model.Account;
import Util.ConnectionUtil;

/**
 * A DAO is a class that mediates the transformation of data between the format of objects in Java to rows in a
 * database. 
 */
public class AccountDAO {
// Please refrain from using a 'try-with-resources' block when connecting to your database. 
// The ConnectionUtil provided uses a singleton, and using a try-with-resources will cause issues in the tests.

/*
    try {
        sql
        stmt
        rs - executeQuery or boolean execute or int executeUpdate();
        while(rs.next()) - to get data from the db response (rs)
    } catch (SQLException e) {
         System.out.println(e.getMessage()); // this should get changed to a log file for production
    }
*/ 

    /** creates a new account in the account table.
     * @param newAccount is an Account object with username and password 
     * @return if successful, the object with created account_id 
     * @return if unsuccessful, null */
    public Account createAccount(Account newAccount) {
        Connection connection = ConnectionUtil.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;
        try {
            String sql = "INSERT INTO account (username, password) VALUES (?, ?)";
            preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, newAccount.username);
            preparedStatement.setString(2, newAccount.password);

            int accountAdded = preparedStatement.executeUpdate();
            if (accountAdded == 1) {
                generatedKeys = preparedStatement.getGeneratedKeys();
                System.out.println("generated key " + generatedKeys);

                if (generatedKeys.next()) {
                    // first column will be the key, because it is the only key that should be generated 
                    newAccount.account_id = generatedKeys.getInt(1);
                    return newAccount;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating account: " + e.getMessage());
        } finally {
            // close resources
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
        // return null if account could not be created
        return null;
    }

    /**
     * checks if an account exists by username in the account table.
     * @param username this comes from an Account object
     * @return if successfully, finds an existing user with given username, true
     * @return if unsuccessful, false
     */
    public boolean getAccountByUsername(String username) {
        Connection connection = ConnectionUtil.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet usernameExists = null;
        try {
            // use 1 to reduce how much data is retrieved from the db
            String sql = "SELECT * FROM account WHERE username = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);

            usernameExists = preparedStatement.executeQuery();
            // this returns true if data exists
            if (usernameExists.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error checking if username exists: " + e.getMessage());
        } finally {
            // close resources in reverse order
            try {
                if (usernameExists != null) usernameExists.close();
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
        // return false if username does not exist
        return false;
    }
    
    /** Login for an account.
     * @param account takes an Account object with username and password.
     * @return if successful, finds a user with matching username and password, returns an Account object with account_id, username, and password.
     * @return if unsuccessful, returns null.
     */
    public Account getAccountByUsernameAndPassword(Account account) {
        Connection connection = ConnectionUtil.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM account WHERE username = ? AND password = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, account.username);
            preparedStatement.setString(2, account.password);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Account retrievedAccount = new Account(rs.getInt("account_id"), 
                                                    rs.getString("username"), 
                                                    rs.getString("password"));
                return retrievedAccount;
            }
        } catch (SQLException e) {
            System.out.println("Error getting account: " + e.getMessage());
        } finally {
            // close resources in reverse order
            try {
                if (rs != null) rs.close();
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
        // return null if no matching account is found 
        return null;
    }
}
