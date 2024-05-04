package Model.Requests;

import Model.Product;
import Model.Request;

import java.util.ArrayList;

public class BuyProductRequest extends Request {
    private ArrayList<Product> products;

    public BuyProductRequest(ArrayList<Product> cart, String username) {
        super(username);
        products = cart;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }
}
