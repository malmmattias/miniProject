package Model.Requests;

import Model.ItemCondition;
import Model.Request;
import Model.Status;

public class SearchProductRequest extends Request {
private String productName ="";
private int min = 0, max = 20000000;
private ItemCondition itemCondition = ItemCondition.USED;
    public SearchProductRequest(String productName, int min, int max, ItemCondition condition) {

        this.productName = productName;
        this.min = min;
        this.max = max;
        this.itemCondition = condition;

    }


    public String getProductName() {
        return productName;
    }

    public int getMin() {
        return min;
    }


    public int getMax() {
        return max;
    }

    public ItemCondition getItemCondition(){
        return itemCondition;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
}

