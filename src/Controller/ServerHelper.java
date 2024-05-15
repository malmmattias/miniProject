package Controller;

import Model.Product;
import Model.Requests.SearchProductRequest;
import Model.Requests.SellProductRequest;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;

public class ServerHelper {

    public static ArrayList<Product> createFilteredSearch(SearchProductRequest spr2,
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


    public static String sell(SellProductRequest sellpr,
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
}
