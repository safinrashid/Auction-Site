/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Student Name> Safin Rashid
 * <Student EID> srr3288
 * <5-digit Unique No.> 17155
 * Spring 2023
 */

import java.io.Serializable;

public class Item implements Serializable {

    public String name;
    public String description;
    public Double buyItNowPrice; //also the maximum bid
    public Double startBid;
    public Double currentBid;
    public String topBidderUsername;
    public Integer bidLength;
    public Integer timeLeft;
    public String ownerUsername;
    public Item(){}
    public boolean sold = false;

    public Item(String name, String description, Double startBid, Double buyItNow, Integer bidLength, String ownerUsername) {
        this.name = name;
        this.description = description;
        this.buyItNowPrice = buyItNow;
        this.bidLength = bidLength;
        this.startBid = startBid;
        this.currentBid = startBid;
        this.ownerUsername = ownerUsername;
        this.topBidderUsername = "no bidders yet!";
        this.timeLeft = bidLength;
    }

    public synchronized boolean placeBid(Double bidAmount) {
        return true;
    }

    public synchronized boolean buyOut() {
        if(sold) return false;
        sold = true;
        return true;
    }

}