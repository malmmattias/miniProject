package Model.Requests;

import Model.ItemCondition;
import Model.Request;
import Model.Status;

public class SearchProductRequest extends Request {
private String productName ="";
private int min = 0, max = 20000000;
private ItemCondition itemCondition = ItemCondition.USED;
private Boolean filtered;
    public SearchProductRequest(String productName, int min, int max, ItemCondition condition, Boolean filtered, String username) {
super(username);
        System.out.println("AAA" + username);
        this.productName = productName;
        this.min = min;
        this.max = max;
        this.itemCondition = condition;
        this.filtered = filtered;

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

    public boolean getFiltered(){
        return filtered;
    }

    public ItemCondition getItemCondition(){
        return itemCondition;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
}

