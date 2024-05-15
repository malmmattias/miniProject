package Controller;

import Model.Product;
import Model.Request;
import Model.Requests.*;
import Model.Status;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.Thread.sleep;

public class ClientThread implements Runnable {
    public ObjectInputStream is = null;
    public ObjectOutputStream os = null;
    public ObjectOutputStream notiOS = null;
    private final Server server;

    private final Socket clientSocket;
    private final Socket notificationSocket;

    private Writer writer;


    public ClientThread(Socket clientSocket, Socket socket2, Server server) {
        this.clientSocket = clientSocket;
        this.notificationSocket = socket2;
        this.server = server;
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
                        int sizeBfr = server.products.size();
                        Product product = sellpr.getProduct();
                        server.products.add(product);

                        server.products.toStringMethod();
                        //System.out.println("Update from sellrequest");

                        int sizeAftr = server.products.size();
                        if (sizeAftr > sizeBfr) {
                            os.writeObject(true);
                        } else {
                            os.writeObject(false);
                        }

                        String productName = sellpr.getProduct().getName();
                        String notification = "Notification: "+ productName + " has been added to the products list";

                        //checkForMatchingInterests(product);
                        ArrayList<String> usernames = server.interestsObserver.notify(productName);
                        for (String username : usernames) {
                            ObjectOutputStream channel = server.notification_oos.get(username);
                            channel.writeObject(notification);
                            channel.flush();
                        }

                    }

                    if (request instanceof RegisterInterestRequest){
                        //System.out.println("Server: RegisterInterestRequest");
                        String username = request.getUsername();
                        String interest = request.getInterest();
                        server.interestsObserver.subscribe(username, interest);
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
                        server.productsList = new ArrayList<>();
                        boolean productFound = false;

                        if (!spr.getFiltered()) {
                            server.productsList = createUnfilteredSearch(productName);
                            productFound = !server.productsList.isEmpty();
                        } else if (spr.getFiltered()){
                            server.productsList = createFilteredSearch(spr, productName);
                            productFound = !server.productsList.isEmpty();
                        }


                        if (productFound) {
                            os.writeObject(productFound);
                            os.writeObject(server.productsList);

                            os.flush();


                            //Product p = (Product) is.readObject();
                            //p.setBuyer(userNameImportant);

                            int clientChoiceIndexed = (int) is.readObject();

                            int i = server.products.findAndReplace(server.productsList.get(clientChoiceIndexed));
                            //System.out.println(i+"Q");
                            server.productsList.get(clientChoiceIndexed).setBuyer(userNameImportant);



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

                        server.productsList.clear();
                        os.flush();


                    }

                    if (request instanceof CheckPurchaseRequests cpr) {

                        //if (!cpr.isVerified()) {

                        String username = request.getUsername();

                        ArrayList<Product> buyerRequests = new ArrayList<>();

                        for (int i = 0; i < server.products.size(); i++) {
                            Product product = server.products.get(i);

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

                        if(!response.isEmpty()) {
                            for (Product product : response) {
                                if (product.getStatus().equals(Status.PENDING)) {

                                    String buyerName = product.getBuyer();
                                    String sellerName = product.getSeller();

                                    completeTransaction(buyerName, sellerName, product);

                                    server.extendMap(buyerName, product.getName(), server.purchaseHistory);

                                    product.setStatus(Status.SOLD);
                                }

                            }
                        }

                        //}

                        if(11==28) {

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
                        server.purchaseReq.put(buyer, itemsToBuy);
                    }

                    if (request instanceof AddUserRequest) {
                        String username = request.getUsername();
                        String password = request.getPassWord();
                        server.addUser(username, password);
                    }

                    if (request instanceof PurchaseHistoryRequest) {
                        String name = request.getUsername();
                        ArrayList<String> returnData = server.getPurchaseHistory(name);
                        os.writeObject(returnData);
                    }

                    if (request instanceof VerifyUserRequest vur) {
                        String usrName = ((VerifyUserRequest) request).getUsrName();
                        String psWord = ((VerifyUserRequest) request).getPsWord();

                        boolean verification = Objects.equals(server.loginCredentials.get(usrName), psWord);

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

        private void completeTransaction(String buyerName, String sellerName, Product product) throws IOException {
            String salesConfirmation = STR."Congratulations transaction has been completed. From \{sellerName} to \{buyerName} for \{product.getName()} for \{product.getPrice()} kr";

            //System.out.println(salesConfirmation);

            ObjectOutputStream channel = server.notification_oos.get(buyerName);
            if (channel!=null) {
                channel.writeObject(salesConfirmation);
                channel.flush();
            }

            ObjectOutputStream channel2 = server.notification_oos.get(sellerName);
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
                ObjectOutputStream objectz = server.notification_oos.get(usernameSeller);
                objectz.writeObject(permission);
                objectz.flush();

                String response = (String) server.notification_ois.get(usernameSeller).readObject();

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

            for (Map.Entry<String, ArrayList<String>> entry : server.userInterests.entrySet()) {
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
            for (int i = 0; i < server.products.size(); i++) {
                Product product = server.products.get(i);
                if (productName.contains(product.getName().toUpperCase())) {
                    //System.out.println("Product found: " + product.getName());
                    server.productsList.add(product);
                }
            }
            return server.productsList;
        }

        private ArrayList<Product> createFilteredSearch(SearchProductRequest spr2, String productName){
            for (int i = 0; i < server.products.size(); i++) {
                Product product = server.products.get(i);
                if (productName.contains(product.getName().toUpperCase())
                        && (product.getPrice() >= spr2.getMin() && product.getPrice() <= spr2.getMax())
                        && (spr2.getItemCondition().equals(product.getItemCondition()))) {
                    ////System.out.println("Product found: " + product.getName());
                    server.productsList.add(product);
                }
            }
            return server.productsList;
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
