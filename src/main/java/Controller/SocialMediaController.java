package Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;

// You will need to write your own endpoints and handlers for your controller. 
// The endpoints you will need can be found in readme.md as well as the test cases. 
// You should refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.

/*
Retrieve Request Data:
    Path Parameters: ctx.pathParam("param_name") - example localhost/messages/{message_id}
    Query Parameters: ctx.queryParam("param_name") - example localhost/messages?message_id=1 
    Request Body: ctx.body() (can be parsed into an object using Jackson or another library)

Send Responses:
    JSON Response: ctx.json(object)
    Plain Text Response: ctx.result("message") - any string 
    Set Status Code: ctx.status(200) or ctx.status(400)
 */

public class SocialMediaController {

    AccountService accountService;
    MessageService messageService;

    public SocialMediaController() {
        this.accountService = new AccountService();
        this.messageService = new MessageService();
    }

    /**
     * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.post("/register", this::createAccountHandler);
        app.post("/login", this::getAccountByUsernameAndPasswordHandler);

        app.post("/messages", this::createMessageHandler);
        app.get("/messages", this::getAllMessagesHandler);
        app.get("/messages/{message_id}", this::getMessageByIdHandler);
        app.delete("/messages/{message_id}", this::deleteMessageByIdHandler);
        app.patch("/messages/{message_id}", this::updateMessageByIdHandler);

        app.get("/accounts/{account_id}/messages", this::getAllMessagesByAccountIdHandler);

        return app;
    }

    // ------------------------------------------------------------------------------------------
    // ACCOUNT HANDLERS
    /**
     * mapper is used to convert JSON into an object. The Jackson ObjectMapper will automatically convert the JSON of the POST request into an object.
     * @throws JsonProcessingException will be thrown if there is an issue converting JSON into an object.
     */
    // status 200 if successful, else 400
    private void createAccountHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // mapper will read the JSON data passed from the user and convert it to an object 
        Account account = mapper.readValue(ctx.body(), Account.class);
        Account addedAccount = accountService.createAccount(account);
        if (addedAccount != null) {
            ctx.json(mapper.writeValueAsString(addedAccount)); // default returns status 200
        } else {
            ctx.status(400);
        }
    }

    /**
     * If successful : response body should contain a JSON of the account in the response body, including its account_id
     * If successful : status 200
     * if unsuccessful : status 401 (Unauthorized)
     * @param ctx contain a JSON representation of an Account, not containing an account_id
     * @throws JsonProcessingException
     */
    private void getAccountByUsernameAndPasswordHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = mapper.readValue(ctx.body(), Account.class);
        Account gotAccount = accountService.getAccountByUsernameAndPassword(account);
        if (gotAccount != null) {
            ctx.json(mapper.writeValueAsString(gotAccount)); // returns status 200
        } else {
            ctx.status(401);
        }
    }

    // ------------------------------------------------------------------------------------------
    // MESSAGE HANDLERS

    /** 
     * Purpose: create a new message.
     * creating a new message is successful if message_text is under 255 characters
     * and posted_by is a real existing user.
     * @param ctx contain a JSON representation of a message, not containing message_id
     * @throws JsonProcessingException
     * successful : the created Message object as JSON including its message_id.
     * unsuccess : status 400 
     */
    private void createMessageHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper  = new ObjectMapper();
        Message message = mapper.readValue(ctx.body(), Message.class);
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println("message in handler " + message);
        Message newMessage = messageService.createMessage(message);
        System.out.println("message in handler after " + newMessage);
        if (newMessage != null) {
            ctx.json(mapper.writeValueAsString(newMessage)); // returns status 200 by default
        } else {
            ctx.status(400); // the new message was not created
        }
    }

    /**
     * Purpose: get all messages.
     * @param ctx No info in the request.
     * successful : a list containing Message objects
     * unsuccess : empty list 
     */
    private void getAllMessagesHandler(Context ctx) {
        List<Message> messages = messageService.getAllMessages();
        ctx.json(messages); // returns 200 by default
    }

    /**
     * Purpose: get a single message by its id
     * @param ctx contains a URI parameter for message_id
     * successful : the Message object
     * unsuccess : empty response body
     */
    private void getMessageByIdHandler(Context ctx) {
        // Retrieve the message_id from the path parameter
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));

        // Fetch the message from the service
        Message message = messageService.getMessageById(messageId);

        if (message != null) {
            // Send the message as a JSON response
            ctx.json(message); // Default status is 200
        } else {
            // If the message does not exist
            ctx.status(200).result("");
        }
    }

    /**
     * Purpose: delete a single message by its id
     * @param ctx contains a URI parameter for message_id
     * successful : message existed and was deleted from db, response contains the deleted message
     * unsuccess : message did not exist, empty response body, status 200
     */
    private void deleteMessageByIdHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));

        Message deletedMessage = messageService.deleteMessageById(messageId);
        if (deletedMessage != null) {
            ctx.json(deletedMessage);
        } else {
            ctx.status(200).result("");
        }
    }

    /**
     * Purpose: update a single message by its id with new message_text
     * @param ctx contains a URI parameter for message_id, body contains a new message_text
     * successful : response body of the updated Message object including message_id, message_text, posted_by, and time_posted_epoch
     * unsuccess : status 400
     */
    private void updateMessageByIdHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));
        Message message = mapper.readValue(ctx.body(), Message.class);
        message.message_id = messageId;

        Message updatedMessage = messageService.updateMessageById(message);
        if (updatedMessage != null) {
            ctx.json(updatedMessage);
        } else {
            ctx.status(400);
        }
    }

    /**
     * Purpose: get a complete list of all messages from a single user by their account_id
     * @param ctx contains a URI parameter for account_id
     * successful : list of Message objects
     * unsuccess : empty list, status 200
     */
    private void getAllMessagesByAccountIdHandler(Context ctx) {
        int accountId = Integer.parseInt(ctx.pathParam("account_id"));
        List<Message> messages = messageService.getAllMessagesByAccountId(accountId);
        ctx.json(messages);
    }
}
