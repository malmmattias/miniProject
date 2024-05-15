package Controller;

import Model.ItemCondition;
import Model.Product;
import Model.Status;

public class ServerTestData {
    private Server server;

    public ServerTestData(Server server) {
        this.server = server;
    }

    // To test that the products can be found
    public void addData() {
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
        //System.out.println("Products in the array:");
        for (int i = 0; i < server.products.size(); i++) {
            //System.out.println(products.get(i));
        }
    }
}
