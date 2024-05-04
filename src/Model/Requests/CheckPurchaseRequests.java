package Model.Requests;

import Model.Product;
import Model.Request;

import java.io.Serializable;
import java.util.ArrayList;

public class CheckPurchaseRequests extends Request implements Serializable {
    private ArrayList<Product> buyerRequests;
    private boolean verified = false;
    private ArrayList<Product> sellerResponse = null;

    public CheckPurchaseRequests(String username) {
        super(username);
    }

    public void setBuyerRequests(ArrayList<Product> buyerRequests) {
    }

    public ArrayList<Product> getBuyerRequests() {
        return buyerRequests;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setSellerResponse(ArrayList<Product> sellerResponse) {
        this.sellerResponse = sellerResponse;
    }

    public ArrayList<Product> getSellerResponse() {
        return sellerResponse;
    }
}
