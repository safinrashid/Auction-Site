/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Student Name> Safin Rashid
 * <Student EID> srr3288
 * <5-digit Unique No.> 17155
 * Spring 2023
 */

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import javafx.application.Application;
import java.security.MessageDigest;
import javafx.fxml.FXMLLoader;
import java.net.ServerSocket;
import java.math.BigInteger;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Scanner;
import java.net.Socket;
import java.util.*;
import java.io.*;

public class Server extends Application {
    public static HashMap<String, String> customerMap = new HashMap<>();
    public static Server server;
    public static HashMap<String, Customer> activeCustomerSet = new HashMap<>();
    public static HashMap<String, Item> itemSet = new HashMap<>();
    public static ArrayList<Socket> connectedClients = new ArrayList<>();
    public static List<String> history = new ArrayList<>();
    public static final int PORT = 5000;

    public static void main(String[] args) throws URISyntaxException {
        setupUsers();setupItems();
        //parseUsersTxt();parseItemsTxt();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread in = new IncomingReader();
        in.start();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ServerUI.fxml")));
        primaryStage.setTitle("eHills Server");
        primaryStage.setOnCloseRequest(event -> System.exit(0));
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    static class IncomingReader extends Thread {
        public IncomingReader() {
        }
        @Override
        public void run() {
            server = new Server();
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server started on port " + PORT);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    if(! connectedClients.contains(clientSocket)) {
                        new Thread(new ClientHandler(server, clientSocket)).start();
                    }
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public static synchronized boolean canPlaceBid(String username, String selection, Double bid) {
        Item item = itemSet.get(selection);
        if(bid <= item.currentBid || bid >= item.buyItNowPrice || item.sold) return false;
        else{
            item.topBidderUsername = username;
            item.currentBid = bid;
            return true;
        }
    }

    public static synchronized void addToHistory(String log){
        history.add(log);
        System.out.println("Added to history: " + log);
    }

    public static synchronized void addItemToServer(Item item, String username) {
        itemSet.put(item.name, item);
        addToHistory(username + " added new auction item: " + item.name);
    }

    public static String encrypt(String in) throws NoSuchFieldError, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(in.getBytes());
        BigInteger bigInt = new BigInteger(1, messageDigest);
        return bigInt.toString(16);
    }

    private static void setupUsers(){
        customerMap.put("saf", "d84642aaa21638432d6e6b83a0a8962a");
        customerMap.put("test", "98f6bcd4621d373cade4e832627b4f6");
        customerMap.put("josh", "882c355bc689d1cc6d2dc061220f1bf3");
        customerMap.put("swagnow", "aa43aded60533d0eada983cd1e2a1d52");
        customerMap.put("meher", "4237ac6455790a11bee468dc8dc415f1");
        customerMap.put("halal", "1fc1e18f4e180479fc5225d791f80a57");
        customerMap.put("1", "c4ca4238a0b923820dcc509a6f75849b");
    }

    private static void setupItems(){
        itemSet.put("computer", new Item("computer", "01101100 01101111 01101100", 100.00, 1000.00, 30, "saf"));
        new Thread(new auctionThread("computer", 30)).start();
        itemSet.put("shoes", new Item("shoes", "the jeffersons, its got an air bubble", 10.00, 50.00, 60, "swagnow"));
        new Thread(new auctionThread("shoes", 60)).start();
        itemSet.put("burger", new Item("burger", "borgor with secret borgor sauce", 2.00, 10.00, 90, "josh"));
        new Thread(new auctionThread("burger", 90)).start();
        itemSet.put("plane-ticket", new Item("plane-ticket", "destination undisclosed bring a passport", 75.00, 750.00, 120, "test"));
        new Thread(new auctionThread("plane-ticket", 120)).start();
        itemSet.put("bicycle", new Item("bicycle", "i love gears and chains and stuff", 40.00, 300.00, 150, "meher"));
        new Thread(new auctionThread("bicycle", 150)).start();
        itemSet.put("microwave", new Item("microwave", "aluminum foil's worst nightmare", 37.00, 175.00, 180, "halal"));
        new Thread(new auctionThread("microwave", 180)).start();
    }

    private static void parseUsersTxt() throws URISyntaxException {
        File file = new File(Objects.requireNonNull(Server.class.getClassLoader().getResource("database/users.txt")).toURI());
        //File file = new File("database/users.txt");
        try (Scanner scanner = new Scanner(file)) {
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(" __ ");
                String username = parts[0];
                String password = parts[1];
                customerMap.put(username, password);
            }
        } catch (Exception e){e.printStackTrace();}
    }

    private static void parseItemsTxt() throws URISyntaxException {
        File file = new File(Objects.requireNonNull(Server.class.getClassLoader().getResource("database/items.txt")).toURI());
        //File file = new File("database/items.txt");
        try (Scanner scanner = new Scanner(file)) {
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(" __ ");
                String itemName = parts[0];
                String description = parts[1];
                Double startBid = Double.valueOf(parts[2]);
                Double buyItNow = Double.valueOf(parts[3]);
                Integer bidLength = Integer.valueOf(parts[4]);
                String ownerUsername = parts[5];
                Item item = new Item(itemName, description, startBid, buyItNow, bidLength, ownerUsername);
                itemSet.put(itemName, item);
                new Thread(new auctionThread(itemName, bidLength)).start();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static class auctionThread implements Runnable {
        String itemName;
        Integer duration;
        auctionThread(String itemName, Integer duration){
            this.itemName = itemName;
            this.duration = duration;
        }
        public void run() {
            while (duration > 0) {
                duration--;
                Server.itemSet.get(itemName).timeLeft--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Server.itemSet.get(itemName).ownerUsername = Server.itemSet.get(itemName).topBidderUsername;
            Server.itemSet.get(itemName).sold = true;
        }
    }

}