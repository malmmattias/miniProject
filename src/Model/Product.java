package Model;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable {
    //Mandatory
    private String name;
    private int price;
    private int yearOfProduction;
    private final String seller;
    private String buyer;
    private int id;
    //Optional
    private String color;
    private ItemCondition itemCondition;
    private Status status;

    public Product(ProductBuilder productBuilder) {
        this.name = productBuilder.name;
        this.price = productBuilder.price;
        this.yearOfProduction = productBuilder.yearOfProduction;
        this.seller = productBuilder.seller;
        this.buyer = productBuilder.buyer;
        this.id = productBuilder.id;

        this.color = productBuilder.color;
        this.itemCondition = productBuilder.itemCondition;
        this.status = productBuilder.status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setYearOfProduction(int yearOfProduction) {
        this.yearOfProduction = yearOfProduction;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public ItemCondition getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(ItemCondition itemCondition) {
        this.itemCondition = itemCondition;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getSeller() {
        return seller;
    }

    public String getBuyer() {
        return buyer;
    }

    public boolean setBuyer(String b) {
        if (Objects.equals(b, seller)){
            System.out.println("Buyer can be the same as seller");
            return false;
        } else {
            this.buyer = b;
            return true;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toStringVertical() {
        return "Your product +\n" +
                "   price: " + price + "\n" +
                "   year of production: " + yearOfProduction + "\n" +
                "   color: " + color + "\n" +
                "   itemCondition: " + itemCondition + "\n" +
                "   status: " + status + "\n" +
                "   name: " + name + "\n";
    }

    public String toStringHorizontal(){
        return name + ", " + price + "kr, " + "year "
                + yearOfProduction + ", " + color + ", itemCondition " + itemCondition + ", status: "
                + status + "  " + ", buyer " + buyer + ",seller " + seller + "\n" ;
    }

    public static class ProductBuilder{
        private final String name;
        private final int price;
        private final int yearOfProduction;
        private final String seller;
        private final String buyer;
        private final int id;

        private String color;
        private ItemCondition itemCondition;
        private Status status;

        public ProductBuilder(String name, int price, int yearOfProduction, String seller, String buyer, int id){
            this.name = name;
            this.price = price;
            this.yearOfProduction = yearOfProduction;
            this.seller = seller;
            this.buyer = buyer;
            this.id = id;
        }

        public ProductBuilder color(String color){
            this.color = color;
            return this;
        }

        public ProductBuilder itemCondition(ItemCondition itemCondition){
            this.itemCondition = itemCondition;
            return this;
        }

        public ProductBuilder status(Status status){
            this.status = status;
            return this;
        }

        public Product build(){
            return new Product(this);
        }

    }
}
