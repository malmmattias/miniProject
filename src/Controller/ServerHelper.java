package Controller;

import Model.Product;
import Model.Requests.CheckPurchaseRequests;
import Model.Requests.SearchProductRequest;
import Model.Requests.SellProductRequest;
import Model.Status;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;

public class ServerHelper extends AbstractServer {
    private Server server;

    public ServerHelper(Server server) {
        this.server = server;
    }

    public ArrayList<Product> createFilteredSearch(SearchProductRequest spr2,
                                                          String productName,
                                                          ResizableProductsArray<Product> products,
                                                          ArrayList<Product> productsList){
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            if (productName.contains(product.getName().toUpperCase())
                    && (product.getPrice() >= spr2.getMin() && product.getPrice() <= spr2.getMax())
                    && (spr2.getItemCondition().equals(product.getItemCondition()))) {
                productsList.add(product);
            }
        }
        return productsList;
    }


    public String sell(SellProductRequest sellpr,
                              ResizableProductsArray<Product> products,
                              ObjectOutputStream os,
                              Map<String, ObjectOutputStream> notification_oos, Observer interestsObserver) throws IOException {

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
        String notification = "Notification: "+ productName + " has been added to the products list";

        ArrayList<String> usernames = interestsObserver.notify(productName);
        for (String username : usernames) {
            ObjectOutputStream channel = notification_oos.get(username);
            channel.writeObject(notification);
            channel.flush();
        }

        return "Server: Product added";
    }

    public String searchProduct(SearchProductRequest spr, ObjectOutputStream os, ObjectInputStream is) throws IOException, ClassNotFoundException {

        String userNameImportant = spr.getUsername();
        //System.out.println("SEARCH " + spr.getUsername());

        String productName = spr.getProductName().toUpperCase();
        productsList = new ArrayList<>();
        boolean productFound = false;

        if (!spr.getFiltered()) {
            productsList = createUnfilteredSearch(productName, productsList, server.products);
            productFound = !productsList.isEmpty();
        } else if (spr.getFiltered()){
            productsList = createFilteredSearch(spr, productName, server.products, productsList);
            productFound = !productsList.isEmpty();
        }


        if (productFound) {
            os.writeObject(productFound);
            os.writeObject(productsList);

            os.flush();


            //Product p = (Product) is.readObject();
            //p.setBuyer(userNameImportant);

            int clientChoiceIndexed = (int) is.readObject();
            //System.out.println("serverhelper: " + clientChoiceIndexed);

            Product newProduct = productsList.get(clientChoiceIndexed);

            int i = server.products.findAndReplace(newProduct);
            System.out.println(i+"Q" + userNameImportant);
            newProduct.setBuyer(userNameImportant);
            server.products.overwrite(i, newProduct);
            //productsList.get(clientChoiceIndexed).setBuyer(userNameImportant);
        } else {
            os.writeObject(productFound);
        }

        productsList.clear();
        os.flush();
        return "Server: Product searched";
    }


    private ArrayList<Product> createUnfilteredSearch(String productName, ArrayList<Product> productsList, ResizableProductsArray<Product> products) {
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            if (productName.contains(product.getName().toUpperCase())) {
                //System.out.println("Product found: " + product.getName());
                productsList.add(product);
            }
        }
        return productsList;
    }

    public String checkPurchaseRequests(CheckPurchaseRequests cpr, ResizableProductsArray<Product> products,
                                        ObjectOutputStream os, ObjectInputStream is,
                                        Map<String, ObjectOutputStream> notification_oos,
                                        Map<String, ArrayList<String>> purchaseHistory) throws IOException, ClassNotFoundException {
        String username = cpr.getUsername();

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

        if(!response.isEmpty()) {
            for (Product product : response) {
                if (product.getStatus().equals(Status.PENDING)) {

                    String buyerName = product.getBuyer();
                    String sellerName = product.getSeller();

                    completeTransaction(buyerName, sellerName, product, notification_oos);

                    extendMap(buyerName, product.getName(), purchaseHistory);

                    product.setStatus(Status.SOLD);
                }

            }
        }
        return "Server: Purchase requests checked";
    }

    private void completeTransaction(String buyerName, String sellerName, Product product, Map<String, ObjectOutputStream> notification_oos) throws IOException {
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

    /**
     * This can be used to modify a map whenever the key is a string and the value is an ArrayList of strings.
     * @param name the key
     * @param newListItem the ArrayList
     * @param map the map to extend
     */
    public void extendMap(String name, String newListItem, Map<String, ArrayList<String>> map) {
        ArrayList<String> purchases = map.get(name);
        if (purchases == null) {
            purchases = new ArrayList<>();
        }
        purchases.add(newListItem);
        map.put(name, purchases);
    }


}
