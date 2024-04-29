package Model.Requests;

import Model.Product;
import Model.Request;

public class SellProductRequest extends Request {
    private Product product;

    public SellProductRequest(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}