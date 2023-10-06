/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Student Name> Safin Rashid
 * <Student EID> srr3288
 * <5-digit Unique No.> 17155
 * Spring 2023
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Customer implements Serializable {

    private String username;
    private String password;
    public boolean loggedIn = false;

    public Customer() {
        username = null;
        password = null;
        loggedIn = true;
    }

    public Customer(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Customer(String username) {
        this.username = username;
        this.password = null;
    }

    public void logOut(){
        loggedIn = false;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
