/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Student Name> Safin Rashid
 * <Student EID> srr3288
 * <5-digit Unique No.> 17155
 * Spring 2023
 */

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Message implements Serializable {
    //login, loginFail, exists, guest, bid, bidFail, buyOut, buyOutFail, close, history, info
    private String message, username, password, selection;
    private Item item;
    private Customer user;
    int time;
    String type;
    MongoCollection<Document> documentList;
    List<Item> itemList, soldItems;
    Double bid;
    String[] itemNames;
    List<String> history = new ArrayList<>();

    public Message(String type, String username, String misc) {
        this.type = type;
        this.username = username;
        if(type.equals("login")) this.password = misc;
        if(type.equals("item") || type.equals("buyitnow")) this.selection = misc;
    }

    public Message(String type, String username, Item item){
        this.type = type;
        this.username = username;
        this.item = item;
    }

    public Message(String type, String username, String selection, Double bid){
        this.type = type;
        this.selection = selection;
        this.username = username;
        this.bid = bid;
    }

    public Message(String type, String selection, Double bid){
        this.type = type;
        this.selection = selection;
        this.bid = bid;
    }

    public Message(String type, String username){
        this.type = type;
        this.username = username;
    }

    public Message(String type){
        this.type = type;
    }
    public Message(List<String> history){
        this.history = history;
    }

    public Message(Integer time){
        this.time = time;
    }

    public Message(String[] names){
        this.itemNames = names;
    }

    public String getType(){
        return type;
    }

    public Customer getUser(){
        return user;
    }


}