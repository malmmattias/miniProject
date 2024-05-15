package Model.Requests;

import Model.Request;

import java.io.Serializable;


public class CheckPurchaseRequests extends Request implements Serializable {
    public CheckPurchaseRequests(String username) {
        super(username);
    }


}
