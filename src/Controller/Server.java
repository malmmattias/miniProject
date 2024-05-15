package Controller;

import Model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import Model.Requests.*;

public class Server extends AbstractServer {
    //User specific properties
    private final Map<String, String> loginCredentials = new HashMap<>();
    private final Map<String, ArrayList<String>> purchaseHistory = new HashMap<>();
    private final Observer interestsObserver = new Observer();

    //Generic properties
    public final ResizableProductsArray<Product> products = new ResizableProductsArray<>();
    private final HashMap<String, ArrayList<Product>> purchaseReq = new HashMap<>();
    final Map<String, ObjectOutputStream> notification_oos = new HashMap<>();
    private final Map<String, ObjectInputStream> notification_ois = new HashMap<>();
    private final ServerHelper helper = new ServerHelper(this);

    public Server() {
        //add preconfigured users
        addUser("mary", "abc");
        addUser("john", "abc");

        //add idiosyncratic properties
        helper.extendMap("john", "anItemJohnBought", purchaseHistory);
        helper.extendMap("john", "macBook", purchaseHistory);
        ServerTestData std = new ServerTestData();
        std.addData(this);

        //Run two client threads: notifications thread & client thread
        runNT();
        runCT();
    }

    private void addUser(String name, String password) {
        loginCredentials.put(name, password);
        purchaseHistory.put(name, new ArrayList<>());
    }

    private void runNT() {
        Thread notificationThread = new Thread(() -> {
            try {
                int port = 2000;
                ServerSocket nServerSocket = new ServerSocket(port);

                while(true){
                    Socket nSocket = nServerSocket.accept();
                    NotificationThread nt = new NotificationThread(nSocket);
                    new Thread(nt).start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        notificationThread.start(); // Start the thread
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

                notification_oos.put(username, nos);
                notification_ois.put(username, nis);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void runCT() {
        Thread notificationThread = new Thread(() -> {
            try {
                int port = 1441;
                ServerSocket serverSocket;
                serverSocket = new ServerSocket(port);

                while (true) {
                    Socket socket = serverSocket.accept();

                    ClientThread ch = new ClientThread(socket, null);
                    new Thread(ch).start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        notificationThread.start(); // Start the thread
    }


    public class ClientThread implements Runnable {
        public ObjectInputStream is = null;
        public ObjectOutputStream os = null;

        private final Socket clientSocket;
        private final Socket notificationSocket;

        private ClientThread(Socket clientSocket, Socket socket2) {
            this.clientSocket = clientSocket;
            this.notificationSocket = socket2;
        }

        public void run() {
            try {
                this.os = new ObjectOutputStream(clientSocket.getOutputStream());
                this.is = new ObjectInputStream(clientSocket.getInputStream());

                while (true) {
                    Request request = (Request) is.readObject();

                    if (request instanceof SellProductRequest sellpr) {
                        String result = helper.sell(sellpr, products, os, notification_oos, interestsObserver);
                        System.out.println(result);
                    }

                    if (request instanceof RegisterInterestRequest){
                        String username = request.getUsername();
                        String interest = request.getInterest();
                        interestsObserver.subscribe(username, interest);
                    }

                    if (request instanceof SearchProductRequest spr) {
                        String result = helper.searchProduct(spr, os, is);
                        System.out.println(result);
                    }

                    if (request instanceof CheckPurchaseRequests cpr) {
                        String result = helper.checkPurchaseRequests(cpr, products, os, is, notification_oos, purchaseHistory);


                    }

                    if (request instanceof BuyProductRequest) {
                        String buyer = request.getUsername();
                        System.out.println("buyer: " + buyer);
                        ArrayList<Product> itemsToBuy = ((BuyProductRequest) request).getProducts();
                        for(Product item : itemsToBuy) {
                            System.out.println(item.toString2());
                        }
                        purchaseReq.put(buyer, itemsToBuy);
                    }

                    if (request instanceof AddUserRequest) {
                        String username = request.getUsername();
                        String password = request.getPassWord();
                        addUser(username, password);
                    }

                    if (request instanceof PurchaseHistoryRequest) {
                        String name = request.getUsername();
                        ArrayList<String> returnData = purchaseHistory.get(name);
                        os.writeObject(returnData);
                    }

                    if (request instanceof VerifyUserRequest vur) {
                        String usrName = ((VerifyUserRequest) request).getUsrName();
                        String psWord = ((VerifyUserRequest) request).getPsWord();

                        boolean verification = Objects.equals(loginCredentials.get(usrName), psWord);

                        os.writeObject(verification);
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }




}

