package Model.Requests;

import Model.Request;

public class SearchProductRequest extends Request {
private String productName;
    public SearchProductRequest(String productName) {

        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}

