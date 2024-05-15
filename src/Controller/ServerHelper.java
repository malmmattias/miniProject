package Controller;

import Model.Product;
import Model.Requests.SearchProductRequest;

import java.util.ArrayList;

public class ServerHelper {


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


}
