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

    //Change this later
    private final int port = 1441;

    public Server() {


        addUser("mary", "abc");
        addUser("john", "abc");


        start();
        testProductArray();


        extendMap("john", "iphone", purchaseHistory);
        extendMap("john", "macBook", purchaseHistory);


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

                        //Gson gson = new Gson();
                        //Request request = gson.fromJson(jsonMessage.toString(), Request.class);
                        Request request = (Request) is.readObject();

                        if (request instanceof SellProductRequest spr) {
                            System.out.println("Server: SellProductRequest");
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

                            checkForMatchingInterests(product);

                        }

                        if (request instanceof RegisterInterestRequest){
                            System.out.println("Server: RegisterInterestRequest");
                            String username = request.getUsername();
                            String interest = request.getInterest();
                            extendMap(username, interest, userInterests);
                            System.out.println("Server: "+username+" has registered interest "+interest);
                        }

                        if (request instanceof SearchProductRequest spr) {
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
                            } else {
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

            private void checkForMatchingInterests(Product product) {
                String name = product.getName().toUpperCase();
                List<String> list = getUsernamesWithInterest(name);
                System.out.println("Server: Selling a new product, here's all users with matching interests");
                for (String s : list){
                    System.out.println("-" + s);
                }
            }

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
                        System.out.println("Product found: " + product.getName());
                        productsList.add(product);
                    }
                }
                return productsList;
            }

            private ArrayList<Product> createFilteredSearch(SearchProductRequest spr, String productName){
                for (int i = 0; i < products.size(); i++) {
                    Product product = products.get(i);
                    if (productName.contains(product.getName().toUpperCase())
                            && (product.getPrice() >= spr.getMin() && product.getPrice() <= spr.getMax())
                            && (spr.getItemCondition().equals(product.getItemCondition()))) {
                        System.out.println("Product found: " + product.getName());
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
        System.out.println();
        for (int i = 0; i < 100; i++) {
            System.out.print(".");
        }
        System.out.println();
    }
}

