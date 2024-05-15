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

import static Model.Status.SOLD;
import static java.lang.Thread.sleep;

public class ClientThread implements Runnable {
    public ObjectInputStream is = null;
    public ObjectOutputStream os = null;
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

                            if(clientChoiceIndexed>-1) {
                                int i = server.products.findAndReplace(server.productsList.get(clientChoiceIndexed));

                                server.productsList.get(clientChoiceIndexed).setBuyer(userNameImportant);
                                String seller = server.productsList.get(clientChoiceIndexed).getSeller();

                                sendNotification("You have a purchase request", seller);
                            }


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

                                    for(int i=0; i<server.products.size(); i++){
                                        if (Objects.equals(product.getName(), server.products.get(i).getName())){
                                            Product udpate = server.products.get(i);
                                            udpate.setStatus(SOLD);
                                            System.out.println(udpate.getStatus());
                                            server.products.overwrite(i, udpate);
                                        }
                                    }
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

                        os.writeObject(verification);
                    }

                    if(request instanceof ExitRequest){
                        isRunning=false;
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

        private void sendNotification(String message, String receiver) throws IOException {
            ObjectOutputStream channel = server.notification_oos.get(receiver);
            if(channel!=null) {
                channel.writeObject(message);
                channel.flush();
            }
        }

        private void completeTransaction(String buyerName, String sellerName, Product product) throws IOException {
            String salesConfirmation = STR."Congratulations transaction has been completed. From \{sellerName} to \{buyerName} for \{product.getName()} for \{product.getPrice()} kr";

            sendNotification(salesConfirmation, buyerName);
            sendNotification(salesConfirmation, sellerName);
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

    private class Writer implements Runnable {

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
