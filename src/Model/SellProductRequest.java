package Model;

public class SellProductRequest extends Request {
    private Product product;

    public SellProductRequest(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}
