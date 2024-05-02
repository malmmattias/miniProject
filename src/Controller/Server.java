package Controller;

import Model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import Model.Requests.*;
import com.google.gson.Gson;

public class Server extends Thread {
    private Map<String, String> loginCredentials = new HashMap<>();
    private Map<String, ArrayList<String>> purchaseHistory = new HashMap<>();
    private final ResizableProductsArray<Product> products = new ResizableProductsArray<>();
    private HashMap<String, ArrayList<Product>> purchaseReq= new HashMap<>();

    //Change this later
    private int port = 1441;

    public Server() {


        addUser("mary", "abc");
        addUser("john", "abc");


        start();
        testProductArray();


        addToPurchaseHistory("john", "iphone");
        addToPurchaseHistory("john", "macBook");


    }
    // To test that the products can be found
    public void testProductArray() {
        Product product1 = new Product.Builder("iphone", 1000, 2022, "john")
                .color("Black")
                .itemCondidtion(ItemCondition.NEW)
                .status(Status.AVAILABLE)
                .build();

        Product product2 = new Product.Builder("mac", 2000, 2021, "john")
                .color("Silver")
                .itemCondidtion(ItemCondition.USED)
                .status(Status.SOLD)
                .build();

        products.add(product1);
        products.add(product2);
        System.out.println("Products in the array:");
        for (int i = 0; i < products.size(); i++) {
            System.out.println(products.get(i));
        }
    }

    private void addUser(String name, String password) {
        loginCredentials.put(name, password);
        purchaseHistory.put(name, new ArrayList<>());
    }

    private void addToPurchaseHistory(String name, String newItem) {
        ArrayList<String> purchases = purchaseHistory.get(name);
        purchases.add(newItem);
        purchaseHistory.put(name, purchases);
    }

    public ArrayList<String> getPurchaseHistory(String usrName) {
        return purchaseHistory.get(usrName);
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server: skapad");
            while (true) {
                Socket socket = serverSocket.accept();

                ClientThread ch = new ClientThread(socket);
                new Thread(ch).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public class ClientThread implements Runnable {
        public ObjectInputStream is = null;
        public ObjectOutputStream os = null;
        private final Socket clientSocket;

        private Writer writer;


        private ClientThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                this.os = new ObjectOutputStream(clientSocket.getOutputStream());
                this.is = new ObjectInputStream(clientSocket.getInputStream());

                writer = new Writer();

                new Reader(writer);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        /**
         * The Reader class is a Runnable class that reads messages from the clients.
         * The class checks for messages from a client and sends them to the writer.
         */
        private class Reader {
            private final Writer writer;
            boolean isRunning = true;

            public Reader(Writer writer) {
                this.writer = writer;

                new Thread(writer).start();
                reading();
            }

            /**
             * Method that checks for messages from a client and sends them to the writer.
             * Also checks if a client has disconnected from the server.
             */
            private synchronized void reading() {
                try {
                    while (isRunning) {
                        System.out.println("Server: Client connected");

                        Object request = is.readObject();
                        //Gson gson = new Gson();
                        //Request request = gson.fromJson(jsonMessage.toString(), Request.class);
                        //Request request = (Request) jsonMessage;

                        if (request instanceof SellProductRequest) {
                            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                            SellProductRequest spr = (SellProductRequest) request;
                            int sizeBfr = products.size();
                            Product product = spr.getProduct();
                            products.add(product);

                            products.toStringMethod();
                            System.out.println("Update from sellrequest");

                            int sizeAftr = products.size();
                            if (sizeAftr > sizeBfr) {
                                os.writeObject(true);
                            } else {
                                os.writeObject(false);
                            }

                        }

                        if (request instanceof SearchProductRequest) {
                            SearchProductRequest spr = (SearchProductRequest) request;
                            String productName = spr.getProductName().toUpperCase();
                            ArrayList<Product> productsList = new ArrayList<>();
                            boolean productFound = false;

                            for (int i = 0; i < products.size(); i++) {
                                Product product = products.get(i);
                                if (productName.contains(product.getName().toUpperCase())) {
                                    System.out.println("Product found: " + product.getName());
                                    productsList.add(product);
                                    productFound = true;
                                }
                            }

                            if(productFound){
                                System.out.println("NU SKICKAR JAG LISTFAN");
                                os.writeObject(productFound);
                                os.writeObject(productsList);
                            } else {
                                System.out.println("NU BLEV DET FALSE");
                                os.writeObject(productFound);
                            }

                            productsList.clear();
                            os.flush();

                            products.toStringMethod();
                            System.out.println("Update from search request");





                        }

                        if (request instanceof BuyProductRequest) {
                            String buyer = request.getUsername();
                            ArrayList<Product> itemsToBuy = ((BuyProductRequest) request).getProducts();
                            purchaseReq.put(buyer, itemsToBuy);
                        }

                        if (request instanceof AddUserRequest) {
                            String username = request.getUsername();
                            String password = request.getPassWord();
                            addUser(username, password);
                        }

                        if (request instanceof PurchaseHistoryRequest) {
                            String name = request.getUsername();
                            ArrayList<String> returnData = getPurchaseHistory(name);
                            os.writeObject(returnData);
                        }

                        if (request instanceof VerifyUserRequest) {
                            String usrName = ((VerifyUserRequest) request).getUsrName();
                            String psWord = ((VerifyUserRequest) request).getPsWord();

                            boolean verification = Objects.equals(loginCredentials.get(usrName), psWord);

                            os.writeObject(verification);
                        }


                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        /**
         * The Writer class is a Runnable class that sends messages to the clients. The run method calls the updateConnections
         * and checkUnsentMessages methods every 5 seconds. The updateConnections method sends the connectedUsers array to all
         * clients if it has changed. The checkUnsentMessages method sends any unsent messages to the receivers.
         */
        private class Writer implements Runnable {
            /**
             * The run method calls the updateConnections and checkUnsentMessages methods every 5 seconds.
             */
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


            public void sendMessage() throws IOException {

            }
        }

    }
}

