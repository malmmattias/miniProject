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
    private final Map<String, String> loginCredentials = new HashMap<>();
    private final Map<String, ArrayList<String>> userInterests = new HashMap<>();
    private final Map<String, ArrayList<String>> purchaseHistory = new HashMap<>();
    private final ResizableProductsArray<Product> products = new ResizableProductsArray<>();
    private final HashMap<String, ArrayList<Product>> purchaseReq = new HashMap<>();
    private ArrayList<Product> productsList;
    private Observer interestsObserver = new Observer();
    final Map<String, ObjectOutputStream> notification_oos = new HashMap<>();
    private final Map<String, ObjectInputStream> notification_ois = new HashMap<>();
    private static final ServerHelper helper = new ServerHelper();

    //Change this later
    private final int port = 1441;

    public Server() {
        addUser("mary", "abc");
        addUser("john", "abc");

        helper.extendMap("john", "anItemJohnBought", purchaseHistory);
        helper.extendMap("john", "macBook", purchaseHistory);

        ServerTestData testData = new ServerTestData();
        testData.addData(products);

        runNT(); //Run notifications thread
        start();
    }

    private void addUser(String name, String password) {
        loginCredentials.put(name, password);
        purchaseHistory.put(name, new ArrayList<>());
    }

    private void runNT() {
        Thread notificationThread = new Thread(() -> {
            try {
                ServerSocket nServerSocket = new ServerSocket(8000);

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

    public ArrayList<String> getPurchaseHistory(String usrName) {
        return purchaseHistory.get(usrName);
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                Socket socket = serverSocket.accept();

                ClientThread ch = new ClientThread(socket, null);
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

                notification_oos.put(username, nos);
                notification_ois.put(username, nis);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public class ClientThread implements Runnable {
        public ObjectInputStream is = null;
        public ObjectOutputStream os = null;
        public ObjectOutputStream notiOS = null;

        private final Socket clientSocket;
        private final Socket notificationSocket;

        private Writer writer;


        private ClientThread(Socket clientSocket, Socket socket2) {
            this.clientSocket = clientSocket;
            this.notificationSocket = socket2;
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

            private synchronized void reading() {
                try {
                    while (isRunning) {
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
                            String result = helper.searchProduct(spr, productsList, os, products, is);
                            System.out.println(result);
                        }

                        if (request instanceof CheckPurchaseRequests cpr) {
                            String result = helper.checkPurchaseRequests(cpr, products, os, is, notification_oos, purchaseHistory);


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

                        if (request instanceof VerifyUserRequest vur) {
                            String usrName = ((VerifyUserRequest) request).getUsrName();
                            String psWord = ((VerifyUserRequest) request).getPsWord();

                            boolean verification = Objects.equals(loginCredentials.get(usrName), psWord);

                            if(verification) {
                                //notification_oos.put(usrName, notiOS); //Save oos for notifications channel
                            }

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



        }

    }


}

