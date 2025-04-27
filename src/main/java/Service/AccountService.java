package Service;

import DAO.AccountDAO;
import Model.Account;

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
public class AccountService {
    public AccountDAO accountDAO;

    public AccountService() {
        accountDAO = new AccountDAO();
    }

    /** used for mock behavior test cases */
    public AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }


    // --------------------------------------------------------------------------------------------------
    // SERVICES

    /** 
     * verifies, username not blank, password 4 characters long, username not already taken.
     * @return if successful, Account object.
     * @return if unsuccessful, null.
     */
    public Account createAccount(Account newAccount) {
        // check username not blank || password is at least 4 characters long 
        if (newAccount == null || newAccount.username == null || newAccount.username.isEmpty() || newAccount.password.length() < 4) 
        {
            return null;
        }
        // check if username exists 
        boolean usernameExists = getAccountByUsername(newAccount.username);
        if (usernameExists)
        {
            return null;
        }
        // if here, we can now create a new account -> send to DAO 
        Account account = accountDAO.createAccount(newAccount);
        return account;
    }

    /** Checks if username exists. 
     * @param username the Account object username to be checked.
     * @return true if exists, else false.
    */
    public boolean getAccountByUsername(String username) {
        return accountDAO.getAccountByUsername(username);
    }

    /** Login. Checks if username and password match existing row in db. 
     * @param account is an Account object with username and password.
     * @return if successful, the Account object plus its account_id.
     * @return if unsuccessful, null.
    */
    public Account getAccountByUsernameAndPassword(Account account) {
        Account gotAccount = accountDAO.getAccountByUsernameAndPassword(account);
        // if null, it means the attempt from the DAO SQL query couldn't find an account with a matching username and password
        return gotAccount;
    }
}
