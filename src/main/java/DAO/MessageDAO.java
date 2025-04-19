package DAO;

import Model.Message;
import Util.ConnectionUtil;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

/**
 * A DAO is a class that mediates the transformation of data between the format of objects in Java to rows in a
 * database. 
 */
public class MessageDAO {
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
    /** create a new message in the message table. newMessage fields: posted_by (int), message_text (String 255), time_posted_epoch (long). */
    public Message createMessage(Message newMessage) {
        Connection connection = ConnectionUtil.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;
        try {
            String sql = "INSERT INTO message (posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
            preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, newMessage.posted_by);
            preparedStatement.setString(2, newMessage.message_text);
            preparedStatement.setLong(3, newMessage.time_posted_epoch);

            int messageAdded = preparedStatement.executeUpdate();
            if (messageAdded == 1) {
                generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    // first column will be the key (message_id), because it is the only key that should be generated 
                    newMessage.message_id = generatedKeys.getInt(1);
                    return newMessage;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating new message: " + e.getMessage());
        } finally {
            // close resources in reverse order of creation
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
        // return null if a message could not be created
        return null;
    }

    /** determines if a user exists when they try and create a new message (createMessage) */
    public boolean postedByExistingUser(int postedBy) {
        Connection connection = ConnectionUtil.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet userExists = null;
        try {
            // use 1 to reduce how much data is retrieved from the db
            String sql = "SELECT 1 FROM message WHERE posted_by = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, postedBy);

            userExists = preparedStatement.executeQuery();
            // this returns true if data exists
            return userExists.next();
        } catch (SQLException e) {
            System.out.println("Error checking posted by existing user: " + e.getMessage());
        } finally {
            // close resources in reverse order
            try {
                if (userExists != null) userExists.close();
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
        // return false if user does not exist
        return false;
    }

    /** returns a list of all messages, it is expected for the list to simply be empty if there are no messages */
    public List<Message> getAllMessages() {
        Connection connection = ConnectionUtil.getConnection();
        List<Message> messages = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM message";
            preparedStatement = connection.prepareStatement(sql);
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Message message = new Message(
                    rs.getInt("message_id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")
                );
                messages.add(message);
            }
        } catch (SQLException e) {
            System.out.println("Error getting all messages: " + e.getMessage());
        } finally {
            // Close resources in reverse order of their creation 
            // Do NOT close the connection here, as it is managed by the singleton
            try {
                // If you close the PreparedStatement, the associated ResultSet is automatically closed. 
                // However, explicitly closing the ResultSet is a good practice to ensure proper resource management.
                if (rs != null) rs.close();
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
        // Return the list of messages (empty if an exception occurred)
        return messages;
    }
    
    public Message getMessageById(int id) {
        Connection connection = ConnectionUtil.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Message message = null;
        try {
            String sql = "SELECT * FROM message WHERE message_id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                message = new Message(
                    rs.getInt("message_id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error getting message by id: " + e.getMessage());
        } finally {
            // close in reverse order
            try {
                if (rs != null) rs.close();
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
        // will return null if no message is found
        return message;
    }

    /** 
     * Deletion of an existing message should remove an existing message from the database. 
     * If the message existed, the response body should contain the now-deleted message.
     * If the message did not exist, the response body should be empty.
     */
    public Message deleteMessageById(int id) {
        Connection connection = ConnectionUtil.getConnection();
        Message message = getMessageById(id);
        if (message == null) {
            return null;
        }
        PreparedStatement preparedStatement = null;
        try {
            String sql = "DELETE FROM message WHERE message_id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            int messageDeleted = preparedStatement.executeUpdate();
            if (messageDeleted == 1) {
                return message;
            }
        } catch (SQLException e) {
            System.out.println("Error deleting message: " + e.getMessage());
        } finally {
            // close resources in reverse
            try {
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
        return null;
    }

    /**  */
    public Message updateMessageById(Message newMessage) {
        Connection connection = ConnectionUtil.getConnection();
        PreparedStatement preparedStatement = null;
        try {
            String sql = "UPDATE message SET message_text = ? WHERE message_id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newMessage.message_text);
            preparedStatement.setInt(2, newMessage.message_id);
            int messageUpdated = preparedStatement.executeUpdate();
            if (messageUpdated == 1) {
                return newMessage;
            }
        } catch (SQLException e) {
            System.out.println("Error updating message: " + e.getMessage());
        } finally {
            // close resources in reverse
            try {
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
        // will return null if message could not be updated
        return null;
    }

    // ---------------------------------------------------------------------------------------------------- 
    // ACCOUNT 

    /** 
     * Retrieves all messages posted by a specific user from the message table.
     * @param postedBy foreign key to account.account_id 
     * @return a list containing all messages posted by a particular user, or empty if no messages exist for that user
     */
    public List<Message> getAllMessagesByAccountId(int postedBy) {
        Connection connection = ConnectionUtil.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        List<Message> messages = new ArrayList<>();
        try {
            String sql = "SELECT * FROM message WHERE posted_by = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, postedBy);
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Message message = new Message(
                    rs.getInt("message_id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")
                );
                messages.add(message);
            }
        } catch (SQLException e) {
            System.out.println("Error getting all message by account: " + e.getMessage());
        } finally {
            // close resources in reverse
            try {
                if (rs != null) rs.close();
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
        // returns an empty list if no messages were found or all messages by posted_by 
        return messages;
    }
}
