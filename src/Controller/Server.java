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
    private final Map<String, ObjectOutputStream> notification_oos = new HashMap<>();
    private final Map<String, ObjectInputStream> notification_ois = new HashMap<>();

    //Change this later
    private final int port = 1441;

    public Server() {




        addUser("mary", "abc");
        addUser("john", "abc");


        run2();

        start();
        testProductArray();


        extendMap("john", "anItemJohnBought", purchaseHistory);
        //extendMap("john", "macBook", purchaseHistory);


    }

    // To test that the products can be found
    public void testProductArray() {
        Product product1 = new Product.Builder("iphone", 1000, 2022, "john", "none")
                .color("Black")
                .itemCondidtion(ItemCondition.NEW)
                .status(Status.AVAILABLE)
                .build();

        Product product2 = new Product.Builder("mac", 2000, 2021, "john", "none")
                .color("Silver")
                .itemCondidtion(ItemCondition.USED)
                .status(Status.AVAILABLE)
                .build();

        products.add(product1);
        products.add(product2);
        //System.out.println("Products in the array:");
        for (int i = 0; i < products.size(); i++) {
            //System.out.println(products.get(i));
        }
    }

    private void addUser(String name, String password) {
        loginCredentials.put(name, password);
        purchaseHistory.put(name, new ArrayList<>());
    }

    /**
     * This can be used to modify a map whenever the key is a string and the value is an ArrayList of strings.
     * @param name the key
     * @param newListItem the ArrayList
     * @param map the map to extend
     */
    private void extendMap(String name, String newListItem, Map<String, ArrayList<String>> map) {
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
                //this.notiOS = new ObjectOutputStream(notificationSocket.getOutputStream());

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
                        //System.out.println("Server: Client connected");

                        //Gson gson = new Gson();
                        //Request request = gson.fromJson(jsonMessage.toString(), Request.class);
                        Request request = (Request) is.readObject();

                        if (request instanceof SellProductRequest sellpr) {
                            //System.out.println("Server: SellProductRequest");
                            int sizeBfr = products.size();
                            Product product = sellpr.getProduct();
                            products.add(product);

                            products.toStringMethod();
                            //System.out.println("Update from sellrequest");

                            int sizeAftr = products.size();
                            if (sizeAftr > sizeBfr) {
                                os.writeObject(true);
                            } else {
                                os.writeObject(false);
                            }

                            String productName = sellpr.getProduct().getName();
                            String notification = "Notification: " + productName + " has been added to the products list";

                            //checkForMatchingInterests(product);
                            ArrayList<String> usernames = interestsObserver.notify(productName);
                            for (String username : usernames) {
                                ObjectOutputStream channel = notification_oos.get(username);
                                channel.writeObject(notification);
                                channel.flush();
                            }

                        }

                        if (request instanceof RegisterInterestRequest) {
                            //System.out.println("Server: RegisterInterestRequest");
                            String username = request.getUsername();
                            String interest = request.getInterest();
                            interestsObserver.subscribe(username, interest);
                            //System.out.println("Server: "+username+" has registered interest "+interest);
                            /*
                            ArrayList<String> usernames = interestsObserver.notify("mac");
                            for (String user : usernames) {
                                //System.out.println(user);
                            }*/
                        }

                        if (request instanceof SearchProductRequest spr) {
                            String userNameImportant = spr.getUsername();
                            //System.out.println("SEARCH " + spr.getUsername());

                            String productName = spr.getProductName().toUpperCase();
                            productsList = new ArrayList<>();
                            boolean productFound = false;

                            if (!spr.getFiltered()) {
                                productsList = createUnfilteredSearch(productName);
                                productFound = !productsList.isEmpty();
                            } else if (spr.getFiltered()) {
                                productsList = createFilteredSearch(spr, productName);
                                productFound = !productsList.isEmpty();
                            }


                            if (productFound) {
                                os.writeObject(productFound);
                                os.writeObject(productsList);

                                os.flush();


                                //Product p = (Product) is.readObject();
                                //p.setBuyer(userNameImportant);

                                int clientChoiceIndexed = (int) is.readObject();

                                int i = products.findAndReplace(productsList.get(clientChoiceIndexed));
                                //System.out.println(i+"Q");
                                productsList.get(clientChoiceIndexed).setBuyer(userNameImportant);


                                //int clientChoice = Integer.parseInt (data) - 1;
                                //System.out.println(p.toString2());


                                //System.out.println("CCC" + clientChoice);

                                /*
                                if (clientChoice > -1) { //above zero means the client decided to buy a product

                                    Product purchase = productsList.get(clientChoice);
                                    purchase.toString2();
                                    purchase.setBuyer(userNameImportant);

                                    //int i = products.findIndex(purchase);

                                    //products.get(i).setBuyer(userNameImportant);

                                    //String username = request.getUsername();
                                    //purchase.setBuyer(username);

                                    //products.overwrite(i, purchase);
                                }*/

                                    /*
                                    boolean permissionGranted = askPermission(request.getUsername(), purchase);

                                    //System.out.println("PQ " + permissionGranted);
                                    int i = products.findIndex(purchase);

                                    purchase.setStatus(Status.SOLD);

                                    products.overwrite(i, purchase);
                                    }*/


                            } else {
                                os.writeObject(productFound);
                            }

                            productsList.clear();
                            os.flush();


                        }

                        if (request instanceof CheckPurchaseRequests cpr) {

                            //if (!cpr.isVerified()) {

                            String username = request.getUsername();

                            ArrayList<Product> buyerRequests = new ArrayList<>();

                            for (int i = 0; i < products.size(); i++) {
                                Product product = products.get(i);

                                if (product.getBuyer() != null
                                        && product.getBuyer() != "none"
                                        && product.getSeller().equals(username)) {
                                    //System.out.println("LK" + product.getBuyer());
                                    buyerRequests.add(product);

                                }
                            }


                            os.writeObject(buyerRequests);
                            os.flush();

                            ArrayList<Product> response = (ArrayList<Product>) is.readObject();

                            if (!response.isEmpty()) {
                                for (Product product : response) {
                                    if (product.getStatus().equals(Status.PENDING)) {

                                        String buyerName = product.getBuyer();
                                        String sellerName = product.getSeller();

                                        completeTransaction(buyerName, sellerName, product);

                                        extendMap(buyerName, product.getName(), purchaseHistory);

                                        product.setStatus(Status.SOLD);
                                    }

                                }
                            }

                            //}

                            if (11 == 28) {

                                for (Product product : cpr.getBuyerRequests()) {
                                    product.toString2();

                                    if (product.getStatus().equals(Status.PENDING)) {

                                        String buyerName = product.getBuyer();
                                        String sellerName = product.getSeller();

                                        completeTransaction(buyerName, sellerName, product);
                                    }
                                }
                            }
                        }

                        if (request instanceof BuyProductRequest) {
                            String buyer = request.getUsername();
                            ArrayList<Product> itemsToBuy = ((BuyProductRequest) request).getProducts();
                            purchaseReq.put(buyer, itemsToBuy);

                            for (Product product : itemsToBuy) {
                                String seller = product.getSeller();
                                try {
                                    sendNotificationToSeller(buyer, seller, product);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
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

                            if (verification) {
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

            private void sendNotificationToSeller(String buyerName, String sellerName, Product product) throws IOException {
                String sellerNotification = STR."Notification: Buyer \{buyerName} has added \{product.getName()} to their cart";

                ObjectOutputStream channel = notification_oos.get(sellerName);
                if (channel != null) {
                    channel.writeObject(sellerNotification);
                    channel.flush();
                }
            }

            private void completeTransaction(String buyerName, String sellerName, Product product) throws IOException {
                String salesConfirmation = STR."Congratulations transaction has been completed. From \{sellerName} to \{buyerName} for \{product.getName()} for \{product.getPrice()} kr";

                //System.out.println(salesConfirmation);

                ObjectOutputStream channel = notification_oos.get(buyerName);
                if (channel!=null) {
                    channel.writeObject(salesConfirmation);
                    channel.flush();
                }

                ObjectOutputStream channel2 = notification_oos.get(sellerName);
                if (channel2!=null) {
                    channel2.writeObject(salesConfirmation);
                    channel2.flush();
                }

            }

            private boolean askPermission(String usernameBuyer, Product purchase) {
                String usernameSeller = purchase.getSeller();

                String permission = STR."PERMISSION: Do you, \{usernameSeller}, agrre to sell product \{purchase.getName()} for \{purchase.getPrice()}to \{usernameBuyer}? y/n";

                boolean permissionGranted = false;


                try {
                    ObjectOutputStream objectz = notification_oos.get(usernameSeller);
                    objectz.writeObject(permission);
                    objectz.flush();

                    String response = (String) notification_ois.get(usernameSeller).readObject();

                    if(response.equals("y")){
                        permissionGranted = true;
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return permissionGranted;
            }

            /*
            private boolean productNameExists(String newProductName) {
                for(int i = 0; i < products.size(); i++) {
                    String productName = products.get(i).getName();
                    if (Objects.equals(productName, newProductName)) {
                        return true;
                    } //Detta är en tillfällig lösning som söker används för att
                    //verifigera om ny sell request matchar någons intressen
                    //Den bör slås ihop med SearchProductRequest s
                    //Martin
                }
                return false;
            }*/

            /*
            private void checkForMatchingInterests(Product product) {
                String name = product.getName().toUpperCase();
                List<String> list = getUsernamesWithInterest(name);
                //System.out.println("Server: Selling a new product, here's all users with matching interests");
                for (String s : list){
                    //System.out.println("-" + s);
                }
            }*/

            public List<String> getUsernamesWithInterest(String interest) {
                List<String> matchingUsernames = new ArrayList<>();

                for (Map.Entry<String, ArrayList<String>> entry : userInterests.entrySet()) {
                    String username = entry.getKey();
                    ArrayList<String> userInterestsList = entry.getValue();

                    if (userInterestsList.contains(interest)) {
                        matchingUsernames.add(username);
                    }
                }
                //Fortsätta här imorgon
                return matchingUsernames;
            }

            private ArrayList<Product> createUnfilteredSearch(String productName) {
                for (int i = 0; i < products.size(); i++) {
                    Product product = products.get(i);
                    if (productName.contains(product.getName().toUpperCase())) {
                        //System.out.println("Product found: " + product.getName());
                        productsList.add(product);
                    }
                }
                return productsList;
            }

            private ArrayList<Product> createFilteredSearch(SearchProductRequest spr2, String productName){
                for (int i = 0; i < products.size(); i++) {
                    Product product = products.get(i);
                    if (productName.contains(product.getName().toUpperCase())
                            && (product.getPrice() >= spr2.getMin() && product.getPrice() <= spr2.getMax())
                            && (spr2.getItemCondition().equals(product.getItemCondition()))) {
                        ////System.out.println("Product found: " + product.getName());
                        productsList.add(product);
                    }
                }
                return productsList;
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

    public void clearConsole() {
        //System.out.println();
        for (int i = 0; i < 100; i++) {
            //System.out.print(".");
        }
        //System.out.println();
    }
}

