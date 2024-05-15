package Controller;

import Model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import Model.Requests.*;

public class Server extends Thread {
    public final Map<String, String> loginCredentials = new HashMap<>();
    public final Map<String, ArrayList<String>> userInterests = new HashMap<>();
    public final Map<String, ArrayList<String>> purchaseHistory = new HashMap<>();
    public final ResizableProductsArray<Product> products = new ResizableProductsArray<>();
    public final HashMap<String, ArrayList<Product>> purchaseReq = new HashMap<>();
    public ArrayList<Product> productsList;
    public Observer interestsObserver = new Observer();
    public final Map<String, ObjectOutputStream> notification_oos = new HashMap<>();
    public final Map<String, ObjectInputStream> notification_ois = new HashMap<>();

    //Change this later
    private final int port = 1441;

    public Server() {




        addUser("mary", "abc");
        addUser("john", "abc");


        run2();

        start();

        ServerTestData std = new ServerTestData(this);
        std.addData();

        extendMap("john", "anItemJohnBought", purchaseHistory);
        extendMap("john", "macBook", purchaseHistory);
    }

    public void addUser(String name, String password) {
        loginCredentials.put(name, password);
        purchaseHistory.put(name, new ArrayList<>());
    }

    public void extendMap(String name, String newListItem, Map<String, ArrayList<String>> map) {
        ArrayList<String> purchases = map.get(name);
        if (purchases == null) {
            purchases = new ArrayList<>();
        }
        purchases.add(newListItem);
        map.put(name, purchases);
    }

    private void run2() {
        Thread notificationThread = new Thread(() -> {
            try {
                ServerSocket nServerSocket = new ServerSocket(8000);

                //System.out.println("Server2: skapad");

                while(true){
                    Socket nSocket = nServerSocket.accept();
                    //System.out.println("Server2: accept");

                    NotificationThread nt = new NotificationThread(nSocket);
                    new Thread(nt).start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        notificationThread.start(); // Start the thread

    }

    public ArrayList<String> getPurchaseHistory(String usrName) {
        return purchaseHistory.get(usrName);
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            //System.out.println("Server: skapad");
            while (true) {
                Socket socket = serverSocket.accept();


                ClientThread ch = new ClientThread(socket, null, this);
                new Thread(ch).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class NotificationThread implements Runnable {
        public ObjectInputStream nis = null;
        public ObjectOutputStream nos = null;

        public NotificationThread(Socket nSocket) throws IOException {
            this.nos = new ObjectOutputStream(nSocket.getOutputStream());
            this.nis = new ObjectInputStream(nSocket.getInputStream());
        }

        public void run() {
            try {
                String username = (String) nis.readObject();

                //System.out.println("YEEreeeeeEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEESSSS!");
                notification_oos.put(username, nos);
                notification_ois.put(username, nis);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

