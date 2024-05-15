package Controller;

import Model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import Model.Requests.*;

import static Model.Status.SOLD;

public class Server extends Thread {
    private final Map<String, ArrayList<String>> purchaseHistory = new HashMap<>();
    private final Observer interestsObserver = new Observer();
    private final Map<String, String> loginCredentials = new HashMap<>();
    private final ResizableProductsArray<Product> products = new ResizableProductsArray<>();
    private final HashMap<String, ArrayList<Product>> purchaseReq = new HashMap<>();
    private ArrayList<Product> productsList;
    private final Map<String, ObjectOutputStream> notification_oos = new HashMap<>();
    private final Map<String, ObjectInputStream> notification_ois = new HashMap<>();
    private int currId = 100;

    public Server() {
        addUser("mary", "abc");
        addUser("john", "abc");

        run2();

        start();
        testProductArray();

        extendMap("john", "anItemJohnBought", purchaseHistory);
        extendMap("john", "macBook", purchaseHistory);


    }

    // To test that the products can be found
    public void testProductArray() {
        Product product1 = new Product.Builder("iphone", 1000, 2022, "john", "none")
                .color("Black")
                .itemCondidtion(ItemCondition.NEW)
                .status(Status.AVAILABLE)
                .build();
        product1.setId(currId++);

        Product product2 = new Product.Builder("mac", 2000, 2021, "john", "none")
                .color("Silver")
                .itemCondidtion(ItemCondition.USED)
                .status(Status.AVAILABLE)
                .build();
        product2.setId(currId++);

        products.add(product1);
        products.add(product2);

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
            int port = 1441;
            ServerSocket serverSocket = new ServerSocket(port);

            //System.out.println("Server: skapad");
            while (true) {
                Socket socket = serverSocket.accept();


                ClientThread ch = new ClientThread(socket);
                new Thread(ch).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public class NotificationThread implements Runnable {
        public ObjectInputStream nis;
        public ObjectOutputStream nos;

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
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public class ClientThread implements Runnable {
        public ObjectInputStream is = null;
        public ObjectOutputStream os = null;

        private final Socket clientSocket;


        private ClientThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                this.os = new ObjectOutputStream(clientSocket.getOutputStream());
                this.is = new ObjectInputStream(clientSocket.getInputStream());
                //this.notiOS = new ObjectOutputStream(notificationSocket.getOutputStream());

                //writer = new Writer();

                new Reader();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        /**
         * The Reader class is a Runnable class that reads messages from the clients.
         * The class checks for messages from a client and sends them to the writer.
         */
        private class Reader {
            boolean isRunning = true;

            public Reader() {
                //this.writer = writer;

                new Thread().start();
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

                        if(request instanceof ExitRequest){
                            isRunning =false;
                        }

                        if (request instanceof SellProductRequest sellpr) {
                            //System.out.println("Server: SellProductRequest");
                            int sizeBfr = products.size();
                            Product product = sellpr.getProduct();
                            product.setId(currId++);
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
                            String notification = "Notification: "+ productName + " has been added to the products list";

                            //checkForMatchingInterests(product);
                            ArrayList<String> usernames = interestsObserver.notify(productName);
                            for (String username : usernames) {
                                ObjectOutputStream channel = notification_oos.get(username);
                                channel.writeObject(notification);
                                channel.flush();
                            }

                        }

                        if (request instanceof RegisterInterestRequest){
                            //System.out.println("Server: RegisterInterestRequest");
                            String username = request.getUsername();
                            String interest = request.getInterest();
                            interestsObserver.subscribe(username, interest);
                            //System.out.println("Server: "+username+" has registered interest "+interest);

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
                            } else if (spr.getFiltered()){
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

                                if(clientChoiceIndexed>-1) {
                                    //System.out.println(i+"Q");
                                    productsList.get(clientChoiceIndexed).setBuyer(userNameImportant);
                                    String sellerName = productsList.get(clientChoiceIndexed).getSeller();
                                    sendNotification("You received a request, ", sellerName);
                                }

                            } else {
                                os.writeObject(productFound);
                            }

                            productsList.clear();
                            os.flush();


                        }

                        if (request instanceof CheckPurchaseRequests cpr) {

                            //if (!cpr.isVerified()) {

                                String username = cpr.getUsername();

                                ArrayList<Product> buyerRequests = new ArrayList<>();

                                for (int i = 0; i < products.size(); i++) {
                                    Product product = products.get(i);

                                    if (product.getBuyer() != null
                                            && !Objects.equals(product.getBuyer(), "none")
                                            && product.getSeller().equals(username)) {
                                        //System.out.println("LK" + product.getBuyer());
                                        buyerRequests.add(product);

                                    }
                                }



                                os.writeObject(buyerRequests);
                                os.flush();

                                ArrayList<Product> response = (ArrayList<Product>) is.readObject();

                                if(!response.isEmpty()) {
                                    for (Product product : response) {
                                        if (product.getStatus().equals(Status.PENDING)) {

                                            String buyerName = product.getBuyer();
                                            String sellerName = product.getSeller();
                                            int id = product.getId();

                                            completeTransaction(buyerName, sellerName, product);

                                            extendMap(buyerName, product.getName(), purchaseHistory);

                                            System.out.println(products.findAndReplace(product) + "id" + id);

                                            for (int i =0; i<products.size(); i++){
                                                if (products.get(i).getId() == id){
                                                    product.setStatus(SOLD);
                                                    products.overwrite(i, product);
                                                }
                                            }

                                        }

                                    }
                                }

                        }

                        if (request instanceof BuyProductRequest) {
                            String buyer = request.getUsername();
                            ArrayList<Product> itemsToBuy = ((BuyProductRequest) request).getProducts();
                            purchaseReq.put(buyer, itemsToBuy);
                        }

                        if (request instanceof AddUserRequest aur) {
                            String username = aur.getUsername();
                            String password = aur.getPassWord();
                            addUser(username, password);
                        }

                        if (request instanceof PurchaseHistoryRequest) {
                            String name = request.getUsername();
                            ArrayList<String> returnData = getPurchaseHistory(name);
                            os.writeObject(returnData);
                        }

                        if (request instanceof VerifyUserRequest vur) {
                            String usrName = vur.getUsrName();
                            String psWord = vur.getPsWord();

                            boolean verification = Objects.equals(loginCredentials.get(usrName), psWord);
                            //System.out.println(verification);
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

            private void sendNotification(String s, String sellerName) throws IOException {
                ObjectOutputStream os = notification_oos.get(sellerName);
                if(os!=null){
                    os.writeObject(s);
                }
            }

            private void completeTransaction(String buyerName, String sellerName, Product product) throws IOException {
                String salesConfirmation = STR."Congratulations transaction has been completed. From \{sellerName} to \{buyerName} for \{product.getName()} for \{product.getPrice()} kr";

                //System.out.println(salesConfirmation);
                sendNotification(salesConfirmation, buyerName);
                sendNotification(salesConfirmation, sellerName);

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

    }

}

