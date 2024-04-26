package Model;

public class Product {
    private int price;
    private int productionYear;
    private String color;
    private Condition condition;
    private Status status;
    private String name;

    public Product(String name, int price, int productionYear, String color, Condition condition, Status status){
    this.price = price;
    this.productionYear = productionYear;
    this.color = color;
    this.condition = condition;
    this.status = status;
    this.name = name;
    }

    public Product(){

    }
    public int getPrice(){
    return price;
    }
    public void setPrice(int price){
        this.price = price;
    }
    public int getProductionYear(){
        return productionYear;
    }
    public void setProductionYear(int productionYear){
        this.productionYear = productionYear;
    }
    public String getColor(){
        return color;
    }
    public void setColor(String color){
        this.color = color;
    }
    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }
}
