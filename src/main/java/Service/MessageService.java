package Service;

import DAO.MessageDAO;
import Model.Message;

import java.util.List;

/**
 * The purpose of a Service class is to contain "business logic" that sits between the web layer (controller) and
 * persistence layer (DAO). That means that the Service class performs tasks that aren't done through the web or
 * SQL: programming tasks like checking that the input is valid, conducting additional security checks, or saving the
 * actions undertaken by the API to a logging file.
 *
 * It's perfectly normal to have Service methods that only contain a single line that calls a DAO method. An
 * application that follows best practices will often have unnecessary code, but this makes the code more
 * readable and maintainable in the long run!
 */
public class MessageService {
    public MessageDAO messageDAO;

    public MessageService() {
        messageDAO = new MessageDAO();
    }

    /** used for mock behavior test cases */
    public MessageService(MessageDAO messageDAO) {
        this.messageDAO = messageDAO;
    }

    // -----------------------------------------------------------------------------------------------------------
    // SERVICES

    // verify - message_text is not blank, is under 255 characters, posted_by refers to a real, existing user
    public Message createMessage(Message createThisMessage) {
        // check message_text is not blank and is under 255 characters (does this mean = to and under?)
        if (createThisMessage == null || createThisMessage.message_text.isBlank() || createThisMessage.message_text.length() > 255)
        {
            return null;
        }
        System.out.println("Service, passed verify");
        // check if posted_by is an existing user --> need additional SQL call 
        // if user does not exist --> return null
        if (!postedByExistingUser(createThisMessage.posted_by)) {
            return null;
        }
        System.out.println("Service, passed existing user");
        // if we made it here, verification is complete 
        // we now can create the message
        Message createdMessage = messageDAO.createMessage(createThisMessage);
        System.out.println("Service, message returned from DAO");
        // not sure if this if statement is needed, but safe for now
        if (createdMessage != null) {
            return createdMessage;
        }
        return null;
    }

    /** determines if a user exists when they try and create a new message (createMessage) */
    public boolean postedByExistingUser(int postedBy) {
        return messageDAO.postedByExistingUser(postedBy);
    }

    // no verification needed
    public List<Message> getAllMessages() {
        return messageDAO.getAllMessages();
    }

    // check if message exists?? - this will be done by the DAO - if null is returned the controller will handle the status code
    public Message getMessageById(int id) {
        return messageDAO.getMessageById(id);
    }

    // check if message exists?? - this will be done by the DAO - if null is returned the controller will handle the status code
    public Message deleteMessageById(int id) {
        return messageDAO.deleteMessageById(id);
    }

    // verify - first check if message_id exists and new message_text is not blank and is not over 255 characters 
    public Message updateMessageById(Message newMessage) {
        // check null first or you will get a NullPointerException when trying to access an objects fields
        if (newMessage == null || newMessage.message_text == null || newMessage.message_text.isBlank() || newMessage.message_text.length() > 255) {
            return null;
        }
        // message_id will be checked by DAO --> it will attempt to find where the id is, if it doesn't exist --> return null
        Message updatedMessage = messageDAO.updateMessageById(newMessage);
        if (updatedMessage != null) {
            return getMessageById(updatedMessage.message_id);
        } else {
            return null;
        }
    }


    // --------------------------------------------------------------------------------------------------------------
    // ACCOUNT 

    // check if account exists? - doesn't look like i need to check
    public List<Message> getAllMessagesByAccountId(int id) {
        System.out.println("service account ");

        return messageDAO.getAllMessagesByAccountId(id);
    }
}
