package Controller;

import Model.ItemCondition;
import Model.Product;
import Model.Status;

public class ServerTestData extends AbstractServer {

    public void addData(Server server) {
        Product product1 = new Product.Builder("iphone", 1000, 2022, "john", "none")
                .color("Black")
                .itemCondidtion(ItemCondition.NEW)
                .status(Status.AVAILABLE)
                .build();

        Product product2 = new Product.Builder("mac", 2000, 2021, "john", "none")
                .color("Silver")
                .itemCondidtion(ItemCondition.USED)
                .status(Status.AVAILABLE)
                .build();

        server.products.add(product1);
        server.products.add(product2);

    }
}
