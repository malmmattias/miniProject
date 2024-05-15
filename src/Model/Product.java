package Model;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable {
    private int price;
    private int yearOfProduction;
    private String color;
    private ItemCondition itemCondition;
    private Status status;
    private String name;
    private final String seller;
    private String buyer;
    private int id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSeller() {
        return seller;
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

    private Product(Builder builder) {
        this.price = builder.price;
        this.yearOfProduction = builder.productionYear;
        this.color = builder.color;
        this.itemCondition = builder.itemCondition;
        this.status = builder.status;
        this.name = builder.name;
        this.seller = builder.seller;
        this.buyer = builder.buyer;
    }

    public void setPrice(int newPrice) {
        price = newPrice;
    }

    public int getPrice(){
        return this.price;
    }

    public ItemCondition getItemCondition() {
        return this.itemCondition;
    }

    public String getBuyer() {
        return buyer;
    }

    public Status getStatus() {
        return status;
    }


    public static class Builder {
        // Required parameters
        private int price;
        private int productionYear;
        private String name;

        // Optional parameters - initialized to default values
        private String color = "";
        private ItemCondition itemCondition = ItemCondition.NEW;
        private Status status = Status.AVAILABLE;
        private String buyer;
        private String seller;

        public Builder(String name, int price, int productionYear, String seller, String buyer) {
            this.name = name;
            this.price = price;
            this.productionYear = productionYear;
            this.seller = seller;
            this.buyer = buyer;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Builder itemCondidtion(ItemCondition itemCondition) {
            this.itemCondition = itemCondition;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }


        public Product build() {
            return new Product(this);
        }
    }

    public void setYearOfProduction(int yearOfProduction) {
        this.yearOfProduction = yearOfProduction;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setItemCondition(ItemCondition itemCondition) {
        this.itemCondition = itemCondition;
    }

    public void setStatus(Status status) {
        this.status = status;
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
}