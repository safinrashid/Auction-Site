/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Student Name> Safin Rashid
 * <Student EID> srr3288
 * <5-digit Unique No.> 17155
 * Spring 2023
 */

import java.security.NoSuchAlgorithmException;
import javafx.collections.FXCollections;
import javafx.application.Application;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.MediaPlayer;
import java.security.MessageDigest;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.shape.Shape;
import java.text.DecimalFormat;
import org.json.JSONException;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import java.math.BigInteger;
import com.google.gson.Gson;
import org.json.JSONObject;
import javafx.scene.Parent;
import java.util.ArrayList;
import org.json.JSONArray;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Objects;
import javafx.fxml.FXML;
import java.net.Socket;
import java.util.List;
import java.net.URL;
import java.io.*;
//http://javafx.com/javafx/17.0.2-ea

public class Client extends Application{
    private static final int PORT = 5000;
    Socket socket = new Socket("localhost", PORT);
    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
    Gson gson = new Gson();
    List<String> ownedItems = new ArrayList<>(), history = new ArrayList<>();
    IncomingReader in;
    static String username = "", password = "", selectedCurrency = "$";
    boolean loggedInUser = false, loggedInGuest = false;
    Customer customerInfo;
    boolean passwordVisible = false;
    DecimalFormat dollarFormat = new DecimalFormat("0.00");
    private static List<Thread> runningThreads = new ArrayList<>();
    @FXML private TextField usernameField, bidField, addItemName, addItemDescription, addItemStartingPrice, addBuyItNowPrice, addItemDuration;
    @FXML private PasswordField passwordField;
    @FXML private Button showPasswordButton, placeBidButton, buyitnowButton, loginButton, refreshButton, newItemButton;
    @FXML private BorderPane loginStack, auctionStack, historyStack, infoStack;
    @FXML private Label auctioncornerUsername, historycornerUsername, infocornerUsername, passwordLabel,
    usernameLabel, guestLabel, myItems, itemName, itemDescription, itemOwner, startBid, currBid, buyitnow,
    timeRemaining, topBidder, auctionError, urHighestBidder, currencyLabel1, currencyLabel2, auctionHistoryLabel;
    @FXML private ComboBox<String> itemSelect, currencySelect;
    @FXML private Shape itemCurtain;
    public Client() throws IOException {}

    public static void main(String[] args){
        launch(args);
    }

    public void startClient() {
        in = new IncomingReader();
        in.start();
        System.out.println("Connected to server");
    }

    static Stage primaryStage;
    @Override
    public void start(Stage stage){
        try {
            Client client = new Client();
            client.startClient();
            primaryStage = stage;
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("UI.fxml")));
            primaryStage.setTitle("eHills by Safin Rashid");
            primaryStage.setOnCloseRequest(event -> System.exit(0));
            primaryStage.getIcons().add(new Image("media/hillicon.png"));
            primaryStage.setScene(new Scene(root, 1250, 700));
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Client Loading Error");
            alert.showAndWait();
            clickSound();
            e.printStackTrace();
        }
    }

    private void clickSound() {
        URL url = getClass().getClassLoader().getResource("media/clicksound.mp3");
        Media sound = new Media(url.toString());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }

    protected void sendToServer(Message out){
        System.out.println("Sending to server: " + gson.toJson(out));
        output.println(gson.toJson(out));
        output.flush();
    }

    public void loginButtonAction() throws IOException, JSONException {
        clickSound();
        if(passwordField.getText().isEmpty() || usernameField.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Incomplete Login");
            alert.setContentText("Please enter both username and password\nor\nContinue as guest with only username");
            alert.showAndWait();
            clickSound();
            return;
        }
        String username = usernameField.getText();
        String password = passwordField.getText();
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        LoginHandler(username, password);
        if(loggedInUser){
            Client.username = username;
            Client.password = password;
            EnterEHills();
        }else{EHillsLogin();}
    }

    public void guestButtonAction() throws IOException, JSONException, NoSuchAlgorithmException {
        clickSound();
        if(usernameField.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Guest Login Failed");
            alert.setContentText("Please enter a username for guest.");
            alert.showAndWait();
            clickSound();
            return;
        }
        if(! passwordField.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Guest Login Failed");
            alert.setContentText("Only enter username for guest.");
            alert.showAndWait();
            clickSound();
            return;
        }
        String username = usernameField.getText();
        System.out.println("Username: " + username);
        LoginHandler(username, "null-guest0");
        Client.username = username;
        if(loggedInGuest) EnterEHills();
    }

    public void auctionButtonAction() {
        clickSound();
        auctionStack.toFront();
        auctioncornerUsername.setText("welcome, " + Client.username + "!");
    }

    public void refreshButtonAction() throws IOException, JSONException {
        clickSound();
        if (itemSelect.getSelectionModel().getSelectedItem() != null) itemSelectedButtonAction();
    }

    static Stage newItemWindow = new Stage();
    public void newItemButtonAction() throws IOException{
        clickSound();
        newItemWindow.setTitle("New eHills Item");
        newItemWindow.setResizable(false);
        newItemWindow.getIcons().add(new Image("media/hillicon.png"));
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("NewItemUI.fxml")));
        Scene scene = new Scene(root);
        newItemWindow.setScene(scene);
        newItemWindow.show();
    }

    public void addNewItemButtonAction() {
        clickSound();
        if(addItemName.getText().isEmpty() || addItemDescription.getText().isEmpty() || addItemStartingPrice.getText().isEmpty() ||
                addBuyItNowPrice.getText().isEmpty() || addItemDuration.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Add Item Error");
            alert.setContentText("Fill out all item fields!");
            alert.showAndWait();
            clickSound();
            return;
        }
        try {
            Item newItem = new Item(addItemName.getText(), addItemDescription.getText(), Double.parseDouble(addItemStartingPrice.getText()),
                    Double.parseDouble(addBuyItNowPrice.getText()), Integer.valueOf(addItemDuration.getText()), username);

            sendToServer(new Message("newItem", username, newItem));
            input.readLine();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            Platform.runLater(() -> itemSelect.getItems().add(newItem.name));
            alert.setHeaderText("Item Added to eHills");
            alert.showAndWait();
            clickSound();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Item could not be added!");
            alert.showAndWait();
            clickSound();
            return;
        }
        newItemWindow.close();
    }

    public void itemSelectedButtonAction() throws IOException, JSONException {
        clickSound();
        for (Thread runningThread : runningThreads) {
            runningThread.stop();
        }
        itemCurtain.toBack();
        placeBidButton.setDisable(false);
        buyitnowButton.setDisable(false);
        bidField.setDisable(false);
        selectedCurrency = currencySelect.getSelectionModel().getSelectedItem();
        currencyLabel1.setText(selectedCurrency);
        currencyLabel2.setText(selectedCurrency);
        JSONObject item;
        try {
            String selection = itemSelect.getSelectionModel().getSelectedItem();
            if (selection != null) itemName.setText("item: " + selection);
            sendToServer(new Message("item", username, selection));
            String response = input.readLine();
            item = new JSONObject(response);
            urHighestBidder.setText("");
            auctionError.setText("");
            try {
                itemName.setText("item: " + item.getString("name"));
            } catch (JSONException ignored) {}
            itemDescription.setText("description: " + item.getString("description"));
            startBid.setText("starting bid: $" + dollarFormat.format(Double.parseDouble(item.getString("startBid"))));
            if(Double.parseDouble(item.getString("currentBid")) < 0){
                currBid.setText("current bid: bought out!");
                buyitnow.setText("");
                placeBidButton.setDisable(true);
                buyitnowButton.setDisable(true);
                bidField.setDisable(true);
            }else {
                currBid.setText("current bid: $" + dollarFormat.format(Double.parseDouble(item.getString("currentBid"))));
            }
            buyitnow.setText(dollarFormat.format(Double.parseDouble(item.getString("buyItNowPrice"))));
            if(item.getString("timeLeft").equals("0")){
                timeRemaining.setText("auction is over!");
                placeBidButton.setDisable(true);
                buyitnowButton.setDisable(true);
                bidField.setDisable(true);
            }else {
                sendToServer(new Message("countdown", selection));
                JSONObject time = new JSONObject(input.readLine());
                Thread t = new Thread(new countdown(Integer.parseInt(time.getString("time"))));
                runningThreads.add(t);
                t.start();
            }
            itemOwner.setText("owner: " + item.getString("ownerUsername"));
            topBidder.setText("top bidder: " + item.getString("topBidderUsername"));
        } catch (JSONException e) {
            e.printStackTrace();
            itemName.setText("item: ");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Invalid Item");
            alert.setContentText("The item you selected is not in auction!");
            alert.showAndWait();
            clickSound();
            return;
        }

        if (item.getString("ownerUsername").equals(username)) {
            auctionError.setText("you own this item!");
            auctionError.toFront();
            urHighestBidder.toBack();
            urHighestBidder.setText("");
            placeBidButton.setDisable(true);
            buyitnowButton.setDisable(true);
            bidField.setDisable(true);
            buyitnow.setText("");
        } else {
            auctionError.setText("");
            placeBidButton.setDisable(false);
            buyitnowButton.setDisable(false);
            bidField.setDisable(false);
        }
        if (item.getString("topBidderUsername").equals(username)) {
            urHighestBidder.setText("you are the highest bidder!");
            urHighestBidder.toFront();
            auctionError.toBack();
        } else {
            urHighestBidder.setText("");
        }
    }

    public void placeBidAction() throws IOException, JSONException {
        clickSound();
        if(timeRemaining.getText().equals("auction is over!")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Auction Over");
            alert.setContentText("The auction is over!");
            alert.showAndWait();
            clickSound();
            return;
        }

        String item = itemSelect.getSelectionModel().getSelectedItem();
        double bid;
        try{
            bid = Double.parseDouble(bidField.getText());
            if(bidField.getText().isEmpty()) throw new NumberFormatException();
        } catch (NumberFormatException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Invalid Bid");
            alert.setContentText("Please enter a valid bid.");
            alert.showAndWait();
            clickSound();
            return;
        }
        sendToServer(new Message("bid", username, item, bid));
        String response = input.readLine();
        JSONObject input = new JSONObject(response);
        if(input.getString("type").equals("bidSuccess")){
            currBid.setText("current bid: $" + dollarFormat.format(Double.parseDouble(input.getString("bid"))));
            urHighestBidder.setText("you are the highest bidder!");
            history.add(username + " placed a bid of $" + dollarFormat.format(bid) + " for item: " + item + "\n");
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Invalid Bid");
            alert.setContentText("Bid not between current bid or but it now price!");
            alert.showAndWait();
            clickSound();
        }

    }

    public void buyItNowAction() throws IOException, JSONException {
        clickSound();
        try {
            String selection = itemSelect.getSelectionModel().getSelectedItem();
            sendToServer(new Message("buyitnow", username, selection));
            String response = input.readLine();
            JSONObject input = new JSONObject(response);
            String buyStatus = input.getString("type");
            if (buyStatus.equals("buySuccess")) {
                currBid.setText("current bid: bought out!");
                itemOwner.setText("owner: " + username);
                topBidder.setText("top bidder: " + username);
                ownedItems.add(selection);
                urHighestBidder.setText("congrats! you own this item!");
                urHighestBidder.toFront();
                auctionError.toBack();
                placeBidButton.setDisable(true);
                buyitnowButton.setDisable(true);
                bidField.setDisable(true);
                history.add(username + " bought out" + selection + "\n");
            } else throw new RuntimeException();
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Buy it now failed");
            alert.setContentText("Another customer has won this item");
            alert.showAndWait();
            clickSound();
        }
    }

    public void historyButtonAction() throws IOException, JSONException {
        clickSound();
        historyStack.toFront();
        historycornerUsername.setText("welcome, " + Client.username + "!");
        HistoryHandler();
    }

    public void infoButtonAction() {
        clickSound();
        infoStack.toFront();
        infocornerUsername.setText("welcome, " + Client.username + "!");
        InfoHandler();
    }

    public void exitButtonAction() throws IOException {
        clickSound();
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("Logging Out");
        alert.setContentText("You are about to log out, are you sure?");
        alert.showAndWait();
        clickSound();
        loggedInGuest = false;
        loggedInUser = false;
        username = "";
        EHillsLogin();
    }

    public void passwordShowAction() {
        clickSound();
        if(loggedInUser) {
            if (!passwordVisible) {
                passwordVisible = true;
                showPasswordButton.setText("hide");
                passwordLabel.setText("password: " + Client.password);
            } else {
                passwordVisible = false;
                showPasswordButton.setText(" \uD83D\uDC40 ");
                passwordLabel.setText("password: ●●●●●●●●●");
            }
        }
    }

    private void LoginHandler(String username, String password){
        try {
            if(! password.equals("null-guest0")) sendToServer(new Message("login", username, encrypt(password)));
            else sendToServer(new Message("login", username, "null-guest0"));
            String response = input.readLine();
            if (response.equals("Logged In")) {
                loggedInUser = true;
            }
            else if (response.equals("New Guest")) {
                loggedInGuest = true;
            }
            else if (response.equals("exists")){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Username belongs to account");
                alert.setContentText("Please log in or try a new username.");
                alert.showAndWait();
                clickSound();
                EHillsLogin();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Invalid username or password");
                alert.setContentText("Please check and try again.");
                alert.showAndWait();
                clickSound();
                EHillsLogin();
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private void HistoryHandler() throws IOException, JSONException {
        sendToServer(new Message("getHistory"));
        String response = input.readLine();
        JSONObject jsonObj = new JSONObject(response);
        JSONArray jsonArray = jsonObj.getJSONArray("history");
        List<String> stringList = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++){
            stringList.add(jsonArray.getString(i));
        }
        List<String> chronological = new ArrayList<String>();
        for(int i = 0; i < stringList.size(); i++){
            chronological.add(stringList.get(stringList.size()-i-1));
        }
        String toPrint = "";
        for(int i = 0; i < chronological.size(); i++){
            toPrint += chronological.get(i) + "\n";
        }
        auctionHistoryLabel.setText(toPrint);
    }

    private void InfoHandler(){
        usernameLabel.setText("username: " + Client.username);
        if(loggedInGuest){
            passwordLabel.setText("no password");
            showPasswordButton.setText("-");
            guestLabel.setText("guest: yes");
        } else guestLabel.setText("guest: no");
        StringBuilder inventory = new StringBuilder();
        for(int i = 0; i < ownedItems.size(); i++){
            if(i == ownedItems.size() - 1) inventory.append(ownedItems.get(i));
            else inventory.append(ownedItems.get(i)).append(", ");
        }
        if(inventory.length() == 0){inventory.append("empty :(");}
        myItems.setText(inventory.toString());
    }

    private void EnterEHills() throws IOException, JSONException {
        loginButton.setDefaultButton(false);
        placeBidButton.setDisable(true);
        buyitnowButton.setDisable(true);
        bidField.setDisable(true);
        auctionStack.toFront();
        itemCurtain.toFront();
        auctioncornerUsername.setText("welcome, " + Client.username + "!");
        sendToServer(new Message("getItems"));
        String itemList = input.readLine();
        JSONObject itemJson = new JSONObject(itemList);
        JSONArray itemNamesArray = itemJson.getJSONArray("itemNames");
        String[] itemNames = new String[itemNamesArray.length()];
        for(int i = 0; i < itemNamesArray.length(); i++) {
            itemNames[i] = itemNamesArray.getString(i);
        }
        itemSelect.setItems(FXCollections.observableArrayList(itemNames));
        itemSelect.setStyle("-fx-font-size: 20px; -fx-text-fill: #000017; -fx-background-color: transparent; " +
                "-fx-border-color: #000017; -fx-border-radius: 0px; -fx-border-width:  0px 0px 2px 0px");
        customerInfo = new Customer(username);
        currencySelect.setItems(FXCollections.observableArrayList("$","£","€","¥","A$","C$","CHF","₹","₩","R$","₺","HK$","R"));
        currencySelect.setValue("$");
    }

    private void EHillsLogin() throws IOException {
        primaryStage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("UI.fxml"))), 1250, 700));
        loginStack.toFront();
    }

    public void currencySelectAction(){
        clickSound();
        currencyLabel1.setText(currencySelect.getSelectionModel().getSelectedItem());
        currencyLabel2.setText(currencySelect.getSelectionModel().getSelectedItem());
    }

    class IncomingReader extends Thread {
        public IncomingReader() {
        }
        @Override
        public void run() {
            String incomingString;
            try {
                while(true){
                    if((incomingString = input.readLine()) != null) {
                        System.out.println("Received from server: " + incomingString + " " + username);
                    }
                }
            } catch (Exception e) {
                System.out.println("Server Disconnection Error: run server and try again");
                System.exit(1);
            }
        }
    }

    class countdown extends Thread  implements Runnable {
        Integer time;
        public countdown(Integer time) {
            this.time = time;
        }
        @Override
        public void run() {
            while(time > 0){
                Platform.runLater(() -> timeRemaining.setText(time + " sec"));
                time--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Platform.runLater(() -> timeRemaining.setText("auction is over!"));
        }
    }

    public static String encrypt(String in) throws NoSuchFieldError, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(in.getBytes());
        BigInteger bigInt = new BigInteger(1, messageDigest);
        return bigInt.toString(16);
    }

}