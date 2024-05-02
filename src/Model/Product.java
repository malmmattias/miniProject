package Model;

import java.io.Serializable;

public class Product implements Serializable {
    private int price;
    private int productionYear;
    private String color;
    private Condition condition;
    private Status status;
    private String name;


    private Product(Builder builder) {
        this.price = builder.price;
        this.productionYear = builder.productionYear;
        this.color = builder.color;
        this.condition = builder.condition;
        this.status = builder.status;
        this.name = builder.name;
    }

    public void setPrice(int newPrice) {
        price = newPrice;
    }

    // Getters for all attributes

    public static class Builder {
        // Required parameters
        private int price;
        private int productionYear;
        private String name;

        // Optional parameters - initialized to default values
        private String color = "";
        private Condition condition = Condition.NEW;
        private Status status = Status.AVAILABLE;

        public Builder(String name, int price, int productionYear) {
            this.name = name;
            this.price = price;
            this.productionYear = productionYear;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Builder condition(Condition condition) {
            this.condition = condition;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public Product build() {
            return new Product(this);
        }
    }

    @Override
    public String toString() {
        return "Product{" +
                "price=" + price +
                ", productionYear=" + productionYear +
                ", color='" + color + '\'' +
                ", condition=" + condition +
                ", status=" + status +
                ", name='" + name + '\'' +
                '}';
    }
}
