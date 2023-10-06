/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Student Name> Safin Rashid
 * <Student EID> srr3288
 * <5-digit Unique No.> 17155
 * Spring 2023
 */

import java.security.NoSuchAlgorithmException;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import org.json.JSONException;
import java.io.BufferedReader;
import com.google.gson.Gson;
import java.util.Observable;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Observer;
import java.net.Socket;

class ClientHandler extends PrintWriter implements Runnable, Observer {
    private Server server;
    private BufferedReader reader;
    private PrintWriter writer;
    static boolean loggedInFlag = false;
    Gson gson = new Gson();
    static String username;
    static String password;
    Socket clientSocket;
    DecimalFormat dollarFormat = new DecimalFormat("0.00");

    protected ClientHandler(Server server, Socket clientSocket) throws IOException {
        super(clientSocket.getOutputStream());
        this.server = server;
        this.clientSocket = clientSocket;
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new PrintWriter(this.clientSocket.getOutputStream());
    }

    protected void sendToClient(String string) {
        System.out.println("Sending to client: " + string);
        writer.println(string);
        writer.flush();
    }

    public void run() {
        System.out.println("Triggered from client: " + clientSocket.toString());
        Server.connectedClients.add(clientSocket);
        String message;

        try {
            while(true){
                if((message = reader.readLine()) != null) {
                    System.out.println("Receiving from client: " + message);
                    processMessage(message, clientSocket);
                }
            }
        } catch (Exception ignored) {}
    }

    private void processMessage(String message, Socket clientSocket) throws JSONException {

        PrintWriter out;
        BufferedReader in;
        JSONObject input = new JSONObject(message);
        String type = input.getString("type");

        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            if(type.equals("getItems")) {
                out.println(gson.toJson(new Message(Server.itemSet.keySet().toArray(new String[0]))));
            }

            else if(type.equals("login")) {
                String username = input.getString("username");
                String password = input.getString("password");
                if (Server.customerMap.containsKey(username) && Server.customerMap.get(username).equals(password)) {
                    out.println("Logged In");
                    ClientHandler.username = username;
                    ClientHandler.password = password;
                    loggedInFlag = true;
                    Server.activeCustomerSet.put(username, new Customer(username, password));
                } else if (password.equals("null-guest0")) {
                    if (Server.customerMap.containsKey(username)) {
                        out.println("exists");
                    } else {
                        Server.customerMap.put(username, "null-guest0");
                        out.println("New Guest");
                        ClientHandler.username = username;
                        ClientHandler.password = "null-guest0";
                        loggedInFlag = true;
                        Server.activeCustomerSet.put(username, new Customer(username, "null-guest0"));
                    }
                } else {
                    out.println("ERROR");
                }
            }

            else if(type.equals("item")) {
                String username = input.getString("username");
                String selection = input.getString("selection");
                Item item = new Item();
                if(Server.itemSet.containsKey(selection)){
                    item = Server.itemSet.get(selection);
                }
                out.println(gson.toJson(item));
            }

            else if(type.equals("bid")){
                String username = input.getString("username");
                String selection = input.getString("selection");
                Double bid = input.getDouble("bid");
                if(Server.canPlaceBid(username, selection, bid)){
                    Server.addToHistory(username + " placed a $" + input.getString("bid") + " bid on " + selection);
                    sendToClient(gson.toJson(new Message("bidSuccess", selection, bid)));
                }else{
                    sendToClient(gson.toJson(new Message("bidfail")));
                }
            }
            
            else if(type.equals("buyitnow")){
                String username = input.getString("username");
                String selection = input.getString("selection");
                if(Server.itemSet.containsKey(selection)){
                    if(Server.itemSet.get(selection).buyOut()){
                        Server.itemSet.get(selection).topBidderUsername = username;
                        Server.itemSet.get(selection).ownerUsername = username;
                        Server.itemSet.get(selection).currentBid *= -1;
                        Server.addToHistory(username + " bought out " + selection + " for $" + Server.itemSet.get(selection).buyItNowPrice);
                        sendToClient(gson.toJson(new Message("buySuccess")));
                    }
                    else sendToClient(gson.toJson(new Message("buyFail")));
                }
                else sendToClient(gson.toJson(new Message("buyFail")));
            }

            else if(type.equals("getHistory")){
                sendToClient(gson.toJson(new Message(Server.history)));
            }

            else if(type.equals("newItem")){
                Item newItem = ParseItem(input.toString());
                newItem.ownerUsername = ClientHandler.username;
                Server.addItemToServer(newItem, ClientHandler.username);
                sendToClient(gson.toJson(new Message("newSuccess")));
            }

            else if(type.equals("countdown")){
                String selection = input.getString("selection");
                sendToClient(gson.toJson(new Message(Server.itemSet.get(selection).timeLeft)));
            }

            else sendToClient(gson.toJson(new Message("error")));
            
        } catch (IOException | NullPointerException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static Item ParseItem(String jsonString) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
        JsonObject itemObject = jsonObject.getAsJsonObject("item");

        String name = itemObject.get("name").getAsString();
        String description = itemObject.get("description").getAsString();
        Double buyItNowPrice = itemObject.get("buyItNowPrice").getAsDouble();
        Double startBid = itemObject.get("startBid").getAsDouble();
        Integer bidLength = itemObject.get("bidLength").getAsInt();
        String ownerUsername = itemObject.get("ownerUsername").getAsString();

        Item item = new Item(name, description, startBid, buyItNowPrice, bidLength, ownerUsername);
        item.currentBid = itemObject.get("currentBid").getAsDouble();
        item.topBidderUsername = itemObject.get("topBidderUsername").getAsString();
        item.sold = itemObject.get("sold").getAsBoolean();

        return item;
    }

    @Override
    public void update(Observable o, Object arg) {
        sendToClient((String) arg);
    }
}