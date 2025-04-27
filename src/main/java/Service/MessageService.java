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

    /** verifies - message_text is not blank, is under 255 characters, posted_by refers to a real, existing user 
     * @param createThisMessage is a Message object we want to create in the message table db.
     * @return if successful, the created message with its' message_id.
     * @return if unsuccessful, null.
    */
    public Message createMessage(Message createThisMessage) {
        // check message_text is not blank and is under 255 characters (does this mean = to and under?)
        if (createThisMessage == null || createThisMessage.message_text.isBlank() || createThisMessage.message_text.length() > 255)
        {
            return null;
        }
        // check if posted_by is an existing user --> need additional SQL call 
        // if user does not exist --> return null
        if (!postedByExistingUser(createThisMessage.posted_by)) {
            return null;
        }
        // if we made it here, verification is complete 
        // we now can create the message
        Message createdMessage = messageDAO.createMessage(createThisMessage);
        // not sure if this if statement is needed, but safe for now
        if (createdMessage != null) {
            return createdMessage;
        }
        return null;
    }

    /** determines if a user exists.
     * @param postedBy is the foreign key posted_by of a Message object for an account_id of an Account object.
     * @return true if exists, else false.
    */
    public boolean postedByExistingUser(int postedBy) {
        return messageDAO.postedByExistingUser(postedBy);
    }

    /** No verification.
     * @return a list of all messages if any exist, empty list if none exist.
     */
    public List<Message> getAllMessages() {
        return messageDAO.getAllMessages();
    }

    /** No verification.
     * @param id message_id.
     * @return Message object or null.
     */
    public Message getMessageById(int id) {
        return messageDAO.getMessageById(id);
    }

    /** No verification.
     * @param id message_id.
     * @return Message object or null.
     */
    public Message deleteMessageById(int id) {
        return messageDAO.deleteMessageById(id);
    }

    /** verifies - checks if message_id exists and new message_text is not blank and is not over 255 characters 
     * @param newMessage Message object with new message_text.
     * @return if successful, an updated Message object with new text.
     * @return if unsuccessful, null.
    */
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

    /** No verification.
     * @param id account_id.
     * @return a list of all messages created by an account or an empty list.
     */
    public List<Message> getAllMessagesByAccountId(int id) {
        return messageDAO.getAllMessagesByAccountId(id);
    }
}
