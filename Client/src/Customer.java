/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Student Name> Safin Rashid
 * <Student EID> srr3288
 * <5-digit Unique No.> 17155
 * Spring 2023
 */

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class Customer {

    public String username;
    public String password;
    private Gson gson;

    public Customer(String u, String p){
        username = u;
        password = p;
    }
    public Customer(String u){
        username = u;
        password = null;
    }

}
